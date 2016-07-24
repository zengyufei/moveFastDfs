package com.zyf.move.fastdfs.threadpool;

import java.sql.Connection;
import java.sql.SQLException;

import com.zyf.move.fastdfs.threadpool.Pool.Validator;

public final class JDBCConnectionValidator implements Validator<Connection> {
	@Override
	public boolean isValid(Connection con) {
		if (con == null) {
			return false;
		}
		try {
			return !con.isClosed();
		} catch (SQLException se) {
			return false;
		}

	}

	@Override
	public void invalidate(Connection con) {
		try {
			con.close();
		} catch (SQLException se) {
		}
	}

}