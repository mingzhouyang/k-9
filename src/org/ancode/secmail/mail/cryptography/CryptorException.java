package org.ancode.secmail.mail.cryptography;

public class CryptorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7266196663599760501L;
	
	public CryptorException(Exception e){
		super.initCause(e.getCause());
	}
	
	public CryptorException(String message){
		super(message);
	}

}
