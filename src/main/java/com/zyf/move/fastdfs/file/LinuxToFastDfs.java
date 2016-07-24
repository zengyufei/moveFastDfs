package com.zyf.move.fastdfs.file;

public class LinuxToFastDfs{
	
	private int id;
	private String fileName;
	private String absolutePath;
	private String absoluteUriPath;
	private String url;
	private String uri;
	private String groupName;
	private String fid;
	private String createDate;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAbsolutePath() {
		return absolutePath;
	}
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getAbsoluteUriPath() {
		return absoluteUriPath;
	}
	public void setAbsoluteUriPath(String absoluteUriPath) {
		this.absoluteUriPath = absoluteUriPath;
	}
	@Override
	public String toString() {
		return "insert into LinuxToFastDfs"
				+ "(fileName,url,absolutePath,absoluteUriPath,createDate,groupName,fid,uri) values('"
				+ this.fileName +"','" + this.url+"','" + this.absolutePath+"','" + this.absoluteUriPath +"','"
				+ this.createDate +"','" + this.groupName+"','"+ this.fid+"','"+ this.uri+"')";
	}
	
}
