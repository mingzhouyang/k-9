package com.fsck.k9.mail.cryptography;
/**
 * Cryptography provides service to encrypto and decrypto mail subject.
 * @author Hai Tao
 *
 */
public class SubjectCryptoImpl implements ICrypto {

	@Override
	public String encrypto(String content) {
		return "Boss Yang is handsome. "+ content;
	}

	@Override
	public String decrypto(String content) {
		return "Boss Yang is handsome. " + content;
	}

}
