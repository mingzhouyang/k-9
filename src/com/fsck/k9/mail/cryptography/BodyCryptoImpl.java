package com.fsck.k9.mail.cryptography;
/**
 * Cryptography provides service to encrypto and decrypto mail body.
 * @author Hai Tao
 *
 */
public class BodyCryptoImpl implements ICrypto {

	@Override
	public String encrypto(String content) {
		return "Boss Yang is rich. " + content;
	}

	@Override
	public String decrypto(String content) {
		return "Boss Yang is rich. " + content;
	}

}
