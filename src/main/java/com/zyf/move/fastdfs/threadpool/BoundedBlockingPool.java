package com.zyf.move.fastdfs.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class BoundedBlockingPool<T>
        extends AbstractPool<T>
        implements BlockingPool<T>
{
    private int size;
    private BlockingQueue<T>  objectsQueue;
    private Validator<T>  validator;
    private ObjectFactory<T>  objectFactory;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean shutdownCalled;

    
	public BoundedBlockingPool(
            int size,
            Validator  validator,
            ObjectFactory  objectFactory)
    {
        super();
        this.objectFactory = objectFactory;
        this.size = size;
        this.validator = validator;
        objectsQueue = new LinkedBlockingQueue (size);
        initializeObjects();
        shutdownCalled = false;
    }

    public T get(long timeOut, TimeUnit unit)
    {
        if(!shutdownCalled)
        {
            T t = null;
            try
            {
                t = objectsQueue.poll(timeOut, unit);
                return t;
            }
            catch(InterruptedException ie)
            {
                Thread.currentThread().interrupt();
            }
            return t;
        }
        throw new IllegalStateException("Object pool is already shutdown");
    }

    public T get()
    {
        if(!shutdownCalled)
        {
            T t = null;
            try
            {
                t = objectsQueue.take();//取不到则一直阻塞
            }

            catch(InterruptedException ie)
            {
                Thread.currentThread().interrupt();
            }
            return t;
        }

        throw new IllegalStateException("Object pool is already shutdown");
    }

    public void shutdown()
    {
        shutdownCalled = true;
        executor.shutdownNow();
        clearResources();
    }

    private void clearResources()
    {
        for(T t : objectsQueue)
        {
            validator.invalidate(t);
        }
    }

    @Override
    protected void returnToPool(T t)
    {
        if(validator.isValid(t))
        {
            executor.submit(new ObjectReturner(objectsQueue, t));
        }
    }

    @Override
    protected void handleInvalidReturn(T t)
    {
    }

    @Override
    protected boolean isValid(T t)
    {
        return validator.isValid(t);
    }

    private void initializeObjects()
    {
        for(int i = 0; i < size; i++)
        {
            objectsQueue.add(objectFactory.createNew());
        }
    }

    private class ObjectReturner<E>
            implements Callable
    {
        private BlockingQueue  queue;
        private E e;
        public ObjectReturner(BlockingQueue  queue, E e)
        {
            this.queue = queue;
            this.e = e;
        }

        public Void call()
        {
            while(true)
            {
                try
                {
                    queue.put(e);
                    break;
                }
                catch(InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        }

    }

}