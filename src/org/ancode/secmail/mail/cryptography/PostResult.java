package org.ancode.secmail.mail.cryptography;

import java.util.Map;
import java.util.TreeMap;

public class PostResult {
	private static String OK = "ok";
	private static String INVALID_KEY = "invalid key";
	private String retCode;
	private String password;
	private String invalidKey;
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
		return retCode != null && retCode.equalsIgnoreCase(OK);
	}
	public String getInvalidKey() {
		return invalidKey;
	}
	public void setInvalidKey(String invalidKey) {
		this.invalidKey = invalidKey;
	}
	
	public boolean isInvalidKey(){
		return invalidKey != null && invalidKey.equalsIgnoreCase(INVALID_KEY);
	}
}
