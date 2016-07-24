package com.zyf.move.fastdfs.threadpool;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

public class Main
{
    public static void main(String[] args) throws SQLException, InterruptedException
    {
        Pool < Connection > pool =
        PoolFactory.newBoundedBlockingPool(
        2,
        new JDBCConnectionFactory(
        		"com.mysql.jdbc.Driver", 
        		"jdbc:mysql://localhost:3306/core_zzfz?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8", 
        		"root", 
        		""),
        new JDBCConnectionValidator());
        for (int i = 0; i < 30; i++) {
        	Connection connection = pool.get();
        	System.out.println(connection);
        	pool.release(connection);
        	Thread.sleep(2000);
		}
        //do whatever you like
     }
    
    
    @Test
    public void noBlockingPool() throws InterruptedException{
        Pool < Connection > pool =
        PoolFactory.newBoundedNonBlockingPool(
        10,
        new JDBCConnectionFactory(
        		"com.mysql.jdbc.Driver", 
        		"jdbc:mysql://localhost:3306/core_zzfz?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8", 
        		"root", 
        		""),
        new JDBCConnectionValidator());
        for (int i = 0; i < 30; i++) {
        	Connection connection = pool.get();
        	System.out.println(connection);
        	pool.release(connection);
		}
        //do whatever you like
     }
}