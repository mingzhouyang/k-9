package com.fsck.k9.mail.cryptography;

public class AESKEYObject {
	private String aesKey;
	private String uuid;
	public AESKEYObject(String aesKey, String uuid){
		this.aesKey = aesKey;
		this.uuid = uuid;
	}
	public String getAesKey() {
		return aesKey;
	}
	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
