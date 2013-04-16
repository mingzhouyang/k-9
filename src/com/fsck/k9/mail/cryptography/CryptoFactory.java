package com.fsck.k9.mail.cryptography;

/**
 * Factory to get cryptography service
 * @author Hai Tao
 *
 */
public class CryptoFactory {
	private static ICrypto subjectCrypto;
	private static ICrypto bodayCrypto;
	
	public static ICrypto getSubjectCryptor(){
		if(subjectCrypto == null)
			subjectCrypto = new SubjectCryptoImpl();
		return subjectCrypto;
	}
	
	public static ICrypto getBodyCryptor(){
		if(bodayCrypto == null)
			bodayCrypto = new BodyCryptoImpl();
		return bodayCrypto;
	}
}
