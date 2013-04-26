package com.fsck.k9.mail.cryptography;


/**
 * Perform AES-128 encryption.
 * @author Hai Tao
 *
 */
public class AESCommonCryptoImpl implements ICrypto {

	@Override
	public String encrypto(String content, String password) throws CryptorException {
		return AESEncryptor.encrypt(content, password); 
	}

	@Override
	public String decrypto(String content, String password) throws CryptorException {
	    return AESEncryptor.decrypt(content, password);
	}

}
