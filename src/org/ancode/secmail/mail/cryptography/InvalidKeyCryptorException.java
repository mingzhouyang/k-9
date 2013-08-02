package org.ancode.secmail.mail.cryptography;

public class InvalidKeyCryptorException extends CryptorException {

	public InvalidKeyCryptorException(Exception e) {
		super(e);
	}
	
	public InvalidKeyCryptorException(String message){
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8876166464873441717L;

}
