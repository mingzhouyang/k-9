package com.fsck.k9.mail.cryptography;

/**
 * Cryptography interface provides service to encrypto and decrypto mail subject and mail body.
 * @author Hai Tao
 *
 */
public interface ICrypto {
	/**
	 * Encrypt the specified content using the given password.
	 * @param content
	 * @param password
	 * @return
	 */
	public String encrypto(String content, String password) throws CryptorException;
	
	/**
	 * Decrypt the specified content using the given password.
	 * @param content
	 * @param password
	 * @return
	 */
	public String decrypto(String content, String password) throws CryptorException;
}
