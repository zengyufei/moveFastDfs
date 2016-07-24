package com.zyf.move.fastdfs.file;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {
	
	public final static String default_encoding = "UTF-8";
	public final static String default_windows_encoding = "GBK";

	//遍历获取文件
	public static Map<String, String> getFiles(File sourcesPathFile, String replaceStr) throws UnsupportedEncodingException{
		Map<String, String> files = new HashMap<String,String>();
		getFiles(files, sourcesPathFile, replaceStr);
		return files;
	}
	
	//遍历获取文件
	@SuppressWarnings("unused")
	public static Map<String, String> getFiles(File sourcesPathFile) throws UnsupportedEncodingException{
		Map<String, String> files = new HashMap<String,String>();
		getFiles(files, sourcesPathFile, "");
		return files;
	}
	
	//遍历获取文件
	@SuppressWarnings("unused")
	public static void getFiles(Map<String, String> files, File sourcesPathFile) throws UnsupportedEncodingException{
		getFiles(files, sourcesPathFile, null);
	}
	
	//遍历获取文件
	public static void getFiles(Map<String, String> files, File sourcesPathFile, String replaceStr) throws UnsupportedEncodingException{
		if(sourcesPathFile.isFile()){
			files.put(sourcesPathFile.getAbsolutePath(), sourcesPathFile.getName());;
		}else{
			for(File oneFile : sourcesPathFile.listFiles()){
				if(oneFile.isFile()){
					String val = new String(oneFile.getAbsolutePath().getBytes(default_windows_encoding)).replace("\\", "/");
					if(replaceStr!=null&&!"".equals(replaceStr))
						val = val.replace(replaceStr, "");
					files.put(val, oneFile.getName());
				}else{
					getFiles(files, oneFile, replaceStr);
				}
			}
		}
	}
}
