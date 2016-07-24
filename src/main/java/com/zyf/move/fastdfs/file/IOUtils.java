package com.zyf.move.fastdfs.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.SimpleFormatter;

import com.zyf.move.fastdfs.sqlddl.MysqlUtils;

public class IOUtils {
	
	@SuppressWarnings("unused")
	private static String getTableRecordByStr(String tableName, String... excludeField) throws IOException, SQLException {
	    String sql = "select * from " + tableName;
        ResultSet rs = MysqlUtils.query(sql);
        ResultSetMetaData metaData = rs.getMetaData();
		int col = metaData.getColumnCount();
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= col; i++) {
			if(!Arrays.asList(excludeField).contains(metaData.getColumnName(i))){
				if(i==1)
					sb.append(metaData.getColumnName(i) + "");
				else
					sb.append("," + metaData.getColumnName(i) + "");
			}
		}
		sb.append("\r\n");
        while (rs.next()) {
            for (int i = 1; i <= col; i++) {
            	if(!Arrays.asList(excludeField).contains(metaData.getColumnName(i))){
            		String val = rs.getString(i);
            		val = val ==null? "null" : "".equals(val)? "''" : val;
            			if(i==1)
            				sb.append(val + "");
	            		else
	            			sb.append("," + val + "");
            	}
             }
            sb.append(");");
            sb.append("\r\n");
        }
        sb.append("\r\n");
        sb.append("\r\n");
        sb.append("\r\n");
	    return sb.toString();
	}
	
	public static String getFilesByInsertBatchSql(String filesPath) throws IOException, SQLException {
		Map<String, String> files = FileUtils.getFiles(new File(Contans.source_path));
		StringBuilder sb = new StringBuilder();
		int id = 1;
		for (Entry<String, String> entry : files.entrySet()) {
			String absolutePath = entry.getKey();
			String fileName = entry.getValue();
			sb.append("INSERT INTO " + Contans.table_name + "(id,fileName,absolutePath,"
					+ "url,targetPath,createDate) values(\r\n");
			sb.append(id);
			sb.append(",'" + fileName + "'");
			sb.append(",'" + absolutePath + "'");
			sb.append(",'" + absolutePath.replace(Contans.source_path, "") + "'");
			sb.append(",'fastdfsPath'");
			sb.append(",'" + new Date().toLocaleString() + "'");
			sb.append(");\r\n");
			id++;
		}
		sb.append("\r\n");
		sb.append("\r\n");
		return sb.toString();
	}
	
	public static String getTableRecordByInsertBatchSql(String tableName, String... excludeField) throws IOException, SQLException {
		String sql = "select * from " + tableName;
		ResultSet rs = MysqlUtils.query(sql);
		ResultSetMetaData metaData = rs.getMetaData();
		int col = metaData.getColumnCount();
		StringBuilder sb = new StringBuilder();
		while (rs.next()) {
			sb.append("INSERT INTO " + tableName + "(");
			for (int i = 1; i <= col; i++) {
				if(!Arrays.asList(excludeField).contains(metaData.getColumnName(i))){
					if(i==1)
						sb.append(metaData.getColumnName(i) + "");
					else
						sb.append("," + metaData.getColumnName(i) + "");
				}
			}
			sb.append(") values(\r\n");
			for (int i = 1; i <= col; i++) {
				if(!Arrays.asList(excludeField).contains(metaData.getColumnName(i))){
					String val = rs.getString(i);
            		val = val ==null? "null" : "".equals(val)? "''" : val;
					if(metaData.getColumnTypeName(i).contains("INT")){
						if(i==1)
							sb.append(val + "");
						else
							sb.append("," + val + "");
					}else{
						if(i==1)
							sb.append("'" + val + "'");
						else
							sb.append(", '" + val + "'");
					}
				}
			}
			sb.append(");");
			sb.append("\r\n");
		}
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("\r\n");
		return sb.toString();
	}
	
	public static String getTableRecordByDeleteBatchSql(String tableName, String... excludeField) throws IOException, SQLException {
		String sql = "select * from " + tableName;
		ResultSet rs = MysqlUtils.query(sql);
		ResultSetMetaData metaData = rs.getMetaData();
		StringBuilder sb = new StringBuilder();
		while (rs.next()) {
			sb.append("delete from " + tableName + " where ");
			String val = rs.getString(1);
			if(metaData.getColumnTypeName(1).contains("VARCHAR")){
				val = "'" + val + "'";
			}
			sb.append(metaData.getColumnName(1) + " = " + val + ";");
			sb.append("\r\n");
		}
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("\r\n");
		return sb.toString();
	}
	
	public static List<String> queryAllTableNames(String... excludeTableNames) {
		List<String> tableNames = new ArrayList<String>();
		String sql = "select table_name from information_schema.tables where table_schema=?";
		try {
			ResultSet rs = MysqlUtils.query(sql, new Object[]{Contans.database_name});
			int col = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= col; i++) {
					if(!Arrays.asList(excludeTableNames).contains(rs.getString(i)))
						tableNames.add(rs.getString(i));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tableNames;
	}
	
	/**
	 * 写出到硬盘
	 * @param files
	 * @param filePath
	 * @throws IOException
	 */
	public static void writeByMap(Map<String, String> files, File fileOutPath) throws IOException{
		createFile(fileOutPath);
		if(fileOutPath.isFile()){
			writeOutByMap(files, fileOutPath);
		}else{
			throw new NullPointerException("不是文件");
		}
	}
	
	/**
	 * 写出到硬盘
	 * @param files
	 * @param filePath
	 * @throws IOException
	 */
	public static void writeStr(String strs, File fileOutPath) throws IOException{
		createFile(fileOutPath);
		if(fileOutPath.isFile()){
			write(strs, fileOutPath);
		}else{
			throw new NullPointerException("不是文件");
		}
	}

	@SuppressWarnings("resource")
	private static void write(String strs, File fileOutPath) throws FileNotFoundException, IOException {
		OutputStream outputStream = new FileOutputStream(fileOutPath);
		BufferedOutputStream bos = new BufferedOutputStream(outputStream);
		bos.write(strs.getBytes());
		bos.flush();
	}
	@SuppressWarnings("resource")
	private static void writeOutByMap(Map<String, String> files, File fileOutPath) throws FileNotFoundException, IOException {
		OutputStream outputStream = new FileOutputStream(fileOutPath);
		BufferedOutputStream bos = new BufferedOutputStream(outputStream);
		for (Entry<String, String> enrty : files.entrySet()) {
			bos.write((enrty.getKey() + "\r\n").getBytes());
		}
		bos.flush();
	}

	private static void createFile(File fileOutPath) throws IOException {
		if(fileOutPath.exists()){
			fileOutPath.delete();
			fileOutPath.createNewFile();
		}else{
			fileOutPath.createNewFile();
		}
	}
}
