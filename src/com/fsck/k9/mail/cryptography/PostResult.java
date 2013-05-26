package com.fsck.k9.mail.cryptography;

import java.util.Map;
import java.util.TreeMap;

public class PostResult {
	private String retCode;
	private String password;
	private Map<String, String> uuidMap;
	public PostResult(){
		this.uuidMap = new TreeMap<String, String>();
	}
	public String getRetCode() {
		return retCode;
	}
	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Map<String, String> getUuidMap() {
		return uuidMap;
	}
	public void setUuidMap(Map<String, String> uuidMap) {
		this.uuidMap = uuidMap;
	}
	
	public boolean isSuccess(){
		return retCode != null && "ok".equalsIgnoreCase(retCode);
	}
}
