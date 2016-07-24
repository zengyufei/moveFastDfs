package com.zyf.move.fastdfs.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.zyf.move.fastdfs.sqlddl.MysqlUtils;

/**
 * 文件上传
 * 
 * @author song
 *
 */
public class FileUtilTest {
	
	public static void main(String[] args) throws FileNotFoundException, Exception {
		int countRecord = 0;//总执行次数
		int tryCount = 0;//尝试次数
		int successCount = 0;//成功次数
		int failCount = 0;//失败次数
		//建表 fileName;absolutePath;url;uri;group;fid;createDate;
		String createTableSql = "create table if not exists " +Contans.table_name+ 
				   " (id INTEGER not NULL auto_increment, " +
                   " fileName VARCHAR(255), " + 
                   " absolutePath VARCHAR(255), " + 
                   " absoluteUriPath VARCHAR(255), " + 
                   " url VARCHAR(255), " + 
                   " createDate VARCHAR(255), " + 
                   " groupName VARCHAR(255), " + 
                   " fid VARCHAR(255), " + 
                   " uri VARCHAR(255), " + 
                   " PRIMARY KEY ( id )) ";
		MysqlUtils.createTable(createTableSql);
				
		//String[] excludeField = new String[]{"design_html", "featrues"};
		
		List<String> allTableName = IOUtils.queryAllTableNames(excludeTableNames);
		StringBuilder deletaSql = new StringBuilder();
		StringBuilder insertSql = new StringBuilder();
		allTableName.forEach(e -> {
			try {
				deletaSql.append(IOUtils.getTableRecordByDeleteBatchSql(e));
				insertSql.append(IOUtils.getTableRecordByInsertBatchSql(e));
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
		
		IOUtils.writeStr(deletaSql.toString(), new File(Contans.delete_sql_file_out));
		
		//需要迁移的文件夹（文件）
		List<String> noSuffixFiles = new ArrayList<String>();
		String insertStr = insertSql.toString().replace(Contans.source_path, "");
		Map<String, String> files =  FileUtils.getFiles(new File(Contans.source_path));
		for (Entry<String, String> entry : files.entrySet()) {
			countRecord++;
			String absolutePath = entry.getKey();
			String fileName = entry.getValue();
			String absoluteUriPath = absolutePath.replace(Contans.source_path, "");
			String isExistsSql = "select count(id) from " + Contans.table_name +" where absoluteUriPath='" +absoluteUriPath+"';";
			if(insertStr.contains(absoluteUriPath) && !MysqlUtils.isExists(isExistsSql)){
				Map<String, String> uploadFile_map = FileUtil.uploadFile_map(new File(absolutePath));
				tryCount++;
				if(uploadFile_map!=null&& uploadFile_map.size()>0){
					successCount++;
					String url = uploadFile_map.get("url");
					String groupName = uploadFile_map.get("groupName");
					String fid = uploadFile_map.get("fid");
					String uri = uploadFile_map.get("uri");
					insertStr = insertStr.replace(absoluteUriPath, url);
					LinuxToFastDfs linuxToFastDfs = new LinuxToFastDfs();
					linuxToFastDfs.setAbsolutePath(absolutePath);
					linuxToFastDfs.setAbsoluteUriPath(absoluteUriPath);
					linuxToFastDfs.setCreateDate(new Date().toLocaleString());
					linuxToFastDfs.setFileName(fileName);
					linuxToFastDfs.setUrl(url);
					linuxToFastDfs.setGroupName(groupName);
					linuxToFastDfs.setFid(fid);
					linuxToFastDfs.setUri(uri);
					MysqlUtils.insert(linuxToFastDfs.toString());
				}
				else{
					noSuffixFiles.add(absolutePath);
					failCount++;
				}
			}
		}
		IOUtils.writeStr(insertStr, new File(Contans.insert_sql_file_out));
		MysqlUtils.shutdownPool();
		System.out.println("总执行次数：" + countRecord);
		System.out.println("尝试次数：" + tryCount);
		System.out.println("成功次数：" + successCount);
		System.out.println("失败次数：" + failCount);
		System.out.println("以下文件无后缀格式为失败：" + noSuffixFiles);
	}
	
	//扫描本地文件导入sql
	@Test
	public void fromLocalImportMysql() throws IOException, SQLException{
		//建表
		String createTableSql = "create table if not exists " +Contans.table_name+ 
				   " (id INTEGER not NULL auto_increment, " +
                   " fileName VARCHAR(255), " + 
                   " absolutePath VARCHAR(255), " + 
                   " url VARCHAR(255), " + 
                   " targetPath VARCHAR(255), " + 
                   " createDate VARCHAR(255), " + 
                   " PRIMARY KEY ( id )) ";
		MysqlUtils.createTable(createTableSql);
		String filesByInsertBatchSql = IOUtils.getFilesByInsertBatchSql(Contans.source_path);
		IOUtils.writeStr(filesByInsertBatchSql, new File(Contans.insert_sql_file_out));
		MysqlUtils.shutdownPool();
	}
	
	
	@SuppressWarnings("unused")
	private static void moveLinuxPathToFastDfs(String sourcesPaths) throws IOException{
		//需要迁移的文件夹（文件）
		Map<String, String> files =  FileUtils.getFiles(new File(sourcesPaths), Contans.source_path);//遍历获取文件
		//writeByMap(files, new File(test_file_out));
	}
	
	
	private final static String[] excludeTableNames = new String[]{"act_package", "activity", "activity_payment_log", 
			"admin_option_log", "app_down_log", "area_code", "backup_simu_plan_detail", "black_member", 
			"bonus_scheme", "branch_bank", "branch_time_package", "brokerage", "car_brand",
			"coach_evaluate", "coach_plan", "coach_plan_detail", "coach_working_plan", 
			"coach_working_project", "coach_working_time", "commission_log", "coupon", "coupon_log", 
			"course_module", "course_plan", "custom_class", "direct_payment_log", "discount",
			"driver_train_flow", "driver_train_item", "driver_train_message", "driver_train_plan", 
			"driver_train_progress", "driving_progress", "exam_result", "exercises", "faq", "feedback",
			"finance_settled", "finger_info", "group_buy", "group_buy_fllow", "group_buy_plan", 
			"inviter_code", "inviter_member_log", "login_account", "machine_error_code", 
			"machine_function", "machine_schedule", "manual_record","member_act_pack", 
			"member_act_pack_log", "member_analysis_question", "member_app_log", "member_charge_log",
			"member_coupon_log", "member_course_progress", "member_course_record",
			"member_option_log", "member_phone_change_log", "member_position", "member_record", 
			"member_record_mistake", "member_wallet", "member_wallet_log", "message_advertising", 
			"message_record", "mistake","notice", "order_orderplan", "partner_account",
			"partner_reg_acount_log", "partner_reg_log","rate_standard", "rechange_order", 
			"recharge_combo", "remark", "role_join_resource","seckill_member_log", "simu_plan_detail",
			"simu_syllabus", "simulator", "simulator_plan", "sms", "stat_member_record",
			"stat_simu_plan_detail", "statistics","store_soft_boot_log", "store_tomcat_log",
			"student_rel_coach", "sys_member", "sys_organization", "sys_resource", "sys_role",
			"temp_experience_log","third_result_item", "thirdpay_order", "track_visit", 
			"training_ground", "training_monitor", "video", "video_play_record", "video_price", 
			"wallet_bill", "wallet_log", "wx_order", "wx_order_return",Contans.table_name.toLowerCase()
			};
	
}
