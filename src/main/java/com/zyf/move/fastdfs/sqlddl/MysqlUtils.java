package com.zyf.move.fastdfs.sqlddl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.zyf.move.fastdfs.file.Contans;
import com.zyf.move.fastdfs.threadpool.BoundedBlockingPool;
import com.zyf.move.fastdfs.threadpool.JDBCConnectionFactory;
import com.zyf.move.fastdfs.threadpool.JDBCConnectionValidator;
import com.zyf.move.fastdfs.threadpool.Pool;

public class MysqlUtils {
	
	public static boolean isExists(String sql, Object... paramValue){
		boolean b=false;
		ResultSet rs = null;
		try {
			Connection conn = getConn();
			PreparedStatement ps = (PreparedStatement) conn.prepareStatement(sql);
			closeConn(conn);
			if(paramValue!=null && paramValue.length > 0){
				for (int i = 0; i < paramValue.length; i++) {
					Object object = paramValue[i];
					ps.setObject((i+1), object);
				}
			}
			rs = ps.executeQuery();
			while(rs.next()){
					String val = rs.getString(1);
					if(val!=null&& !"".equals(val)){
						try {
							return b = Integer.valueOf(val) > 0;
						} catch (NumberFormatException e) {
							return b=true;
						}
					}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return b;
	}
	
	public static ResultSet query(String sql, Object... paramValue){
		ResultSet rs = null;
		try {
			Connection conn = getConn();
			PreparedStatement ps = (PreparedStatement) conn.prepareStatement(sql);
			closeConn(conn);
			if(paramValue!=null && paramValue.length > 0){
				for (int i = 0; i < paramValue.length; i++) {
					Object object = paramValue[i];
					ps.setObject((i+1), object);
				}
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public static boolean createTable(String sql){
		return updataAndDelete(sql);
	}
	
	public static int insert(String sql){
		int id = 0;
		try {
			Connection conn = getConn();
			conn.prepareStatement(sql, id).execute();
			closeConn(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public static boolean updataAndDelete(String sql){
		boolean result = false;
		try {
			Connection conn = getConn();
			result = conn.prepareStatement(sql).execute();
			closeConn(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static Pool < Connection > pool;
	static {
		 JDBCConnectionValidator jdbcConnectionValidator = new JDBCConnectionValidator();
		 JDBCConnectionFactory jdbcConnectionFactory = new JDBCConnectionFactory(
         		"com.mysql.jdbc.Driver", 
         		"jdbc:mysql://" + Contans.host + ":"+ Contans.port +"/"+ Contans.database_name +"?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8", 
         		Contans.username, Contans.password);
		pool = new BoundedBlockingPool<Connection> (2, jdbcConnectionValidator, jdbcConnectionFactory);
	}
	
	public static void closeConn(Connection connection){
		pool.release(connection);
	}
	
	public static Connection getConn(){
		Connection connection = pool.get();
		return connection;
	}
	
	public static void shutdownPool(){
		pool.shutdown();
	}
}
