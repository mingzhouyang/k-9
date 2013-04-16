package com.fsck.k9.mail.cryptography;

/**
 * Cryptography interface provides service to encrypto and decrypto mail subject and mail body.
 * @author Hai Tao
 *
 */
public interface ICrypto {
	public String encrypto(String content);
	public String decrypto(String content);
}
