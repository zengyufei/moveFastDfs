package com.zyf.move.fastdfs.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.csource.common.MyException;
import org.csource.fastdfs.Configuration;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件上传
 * 
 * @author song
 *
 */
public class FileUtil {
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	private static TrackerServer trackerServer;
	private static StorageClient storageClient;
	public static String host;
	static Configuration configuration;
	
	static {
		try {
			configuration = new Configuration();
			trackerServer = configuration.configrue("classpath:fdfs_client.properties").build();
			storageClient = new StorageClient(trackerServer);
			host = trackerServer.getInetSocketAddress().getAddress().getHostAddress();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		}
	}
	
	private FileUtil(){
	}

	/**
	 * 使用url去配置的地址中获取原文件名，如果上传时没有赋值，则取出为空
	 * @param groupNameAndFidUri 完整的url，会帮你解析
	 * @return
	 */
	public static String getFileName(String groupNameAndFidUri){
		String[] resolves = resolve(groupNameAndFidUri);
		return getFileName(resolves[0], resolves[1]);
	}
	
	/**
	 * 使用uri去配置的地址中获取原文件名，如果上传时没有赋值，则取出为空
	 * @param groupName
	 * @param fid
	 * @return
	 */
	public static String getFileName(String groupName, String fid){
		Map<String, String> metaMap = null;
		try {
			metaMap = storageClient.get_metadata_map(groupName, fid);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} 
		if(metaMap.get("fileName") == null || metaMap.get("fileName").toString() == "")
			return null;
		return metaMap.get("fileName").toString();
	}
	
	/**
	 * 使用url去配置的地址中获取所有附加信息
	 * @param groupName
	 * @param fid
	 * @return
	 */
	public static Map<String, String> getFileMap(String groupNameAndFidUri){
		String[] resolves = resolve(groupNameAndFidUri);
		return getFileMap(resolves[0], resolves[1]);
	}
	
	/**
	 * 使用uri去配置的地址中获取所有附加信息
	 * @param groupName upload
	 * @param fid M00/.....
	 * @return
	 */
	public static Map<String, String> getFileMap(String groupName, String fid){
		Map<String, String> metaMap = null;
		try {
			metaMap = storageClient.get_metadata_map(groupName, fid);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} 
		if(metaMap == null || metaMap.size() ==0 )
			return null;
		metaMap.put("groupName", groupName);
		metaMap.put("fid", fid);
		metaMap.put("uri", groupName + "/" + fid);
		metaMap.put("url", "http://" +  host + ":" + configuration.g_tracker_http_port + "/" + groupName + "/" + fid);
		return metaMap;
	}
	
	
	/**
	 * 上传文件到fastDfs
	 * @param java.io.File
	 * @return 返回类似 http://host/hash.后缀
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public static String uploadFile(File file) throws FileNotFoundException, Exception {
		Map<String, String> uploadFile_map = uploadFile_map(file);
		if(uploadFile_map!=null&& uploadFile_map.size()>0)
			return uploadFile_map.get("url");
		else
			return null;
	}
	
	/**
	 * 上传文件到fastDfs
	 * @param java.io.File
	 * @return map
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public static Map<String, String> uploadFile_map(File file) throws FileNotFoundException, Exception {
        Map<String, String> metaMap = new HashMap<String, String>();
        metaMap.put("fileName", file.getName());
        metaMap.put("size", file.length() + "");
       
        //上传文件  
        String[] fileIds = {};
	    try {  
	    	fileIds = storageClient.upload_file(file.getAbsolutePath(), metaMap);
	    } catch (IOException e) {  
	        logger.warn("Upload file \"" + file.getName() + "\"fails");
	        e.printStackTrace();
	    }  
	    trackerServer.close();
	    if(fileIds != null && fileIds.length>0){
	    	String groupName = fileIds[0];
		    String fid = fileIds[1];
	    	return getFileMap(groupName, fid);
	    }else{
	    	return null;
	    }
	}

	
	/**
	 * 使用流的方式上传文件
	 * @param inStream 输入流
	 * @param fileName 文件名
	 * @param fileLength 文件大小
	 * @return 返回类似 http://host/hash.后缀
	 * @throws IOException
	 * @throws MyException
	 */
	public static String uploadFile(InputStream inStream, String fileName, long fileLength) throws IOException, MyException {  
	    return uploadFile_map(inStream, fileName, fileLength).get("url");
	}  
	
	/**
	 * 使用流的方式上传文件
	 * @param inStream 输入流
	 * @param fileName 文件名
	 * @param fileLength 文件大小
	 * @return map
	 * @throws IOException
	 * @throws MyException
	 */
	public static Map<String, String> uploadFile_map(InputStream inStream, String fileName, long fileLength) throws IOException, MyException {  
	    byte[] fileBuff = getFileBuffer(inStream, fileLength);  
	    String fileExtName = "";  
	    if (fileName.contains(".")) {  
	        fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);  
	    } else {  
	        logger.warn("Fail to upload file, because the format of filename is illegal.");  
	        return null;  
	    }  
	  
	    //设置元信息  
	    Map<String, String> metaMap = new HashMap<String, String>();
	    metaMap.put("fileName", fileName);
	    metaMap.put("suffix", fileExtName);
	    metaMap.put("size", fileLength + "");
	    
	    //上传文件  
	    String[] fileIds = {};
	    try {  
	    	fileIds = storageClient.upload_file(fileBuff, fileExtName, metaMap);  
	    } catch (Exception e) {  
	        logger.warn("Upload file \"" + fileName + "\"fails");  
	    }  
	    Map<String, String> resultMap = storageClient.get_metadata_map(fileIds[0], fileIds[1]); 
	    trackerServer.close();
	    
	    String groupName = fileIds[0];
	    String fid = fileIds[1];
	    resultMap.put("groupName", groupName);
	    resultMap.put("fid", fid);
	    resultMap.put("url", "http://" +  host + ":" + configuration.g_tracker_http_port + "/" + groupName + "/" + fid);
	    return resultMap;
	}  

	/** 
     * Transfer java.io.InpuStream to byte array. 
     * @param inStream, input stream of the uploaded file. 
     * @param fileLength, the length of the file. 
     * @return the byte array transferred from java.io.Inputstream. 
     * @throws IOException occurred by the method read(byte[]) of java.io.InputStream. 
     */  
    private static byte[] getFileBuffer(InputStream inStream, long fileLength) throws IOException {  
        byte[] buffer = new byte[256 * 1024];  
        byte[] fileBuffer = new byte[(int) fileLength];  
      
        int count = 0;  
        int length = 0;  
      
        while((length = inStream.read(buffer)) != -1){  
            for (int i = 0; i < length; ++i)  
            {  
                fileBuffer[count + i] = buffer[i];  
            }  
            count += length;  
        }  
        return fileBuffer;  
    }  
    
    /**
     * 解析(group+fid) uri分割出group、fid
     * @param uri
     * @return
     */
    private static String[] resolve(String uri){
    	int start = uri.lastIndexOf("/", uri.indexOf("M00")-4) +1 ;
    	int end = uri.indexOf("M00")-1;
    	String fid = uri.substring(uri.indexOf("M00"));
		return new String[]{uri.substring(start,end),fid};
    }
	
	
	public static void main(String[] args) throws FileNotFoundException, Exception {
		File file = new File("e:/1.txt");
		String uploadFile1 = FileUtil.uploadFile(file);
		//API 1
		String fileName = FileUtil.getFileName(uploadFile1);
		System.out.println(uploadFile1);
		//API 2
		/*String uploadFile2 = FileUtil.uploadFile(new FileInputStream(file), file.getName(), file.length());
		System.out.println(uploadFile2);*/
		
		System.out.println(fileName);
		Map<String, String> fileMap = FileUtil.getFileMap(uploadFile1);
		fileMap.forEach((key,value) -> System.out.println(value));
	}
	
	
	
}
