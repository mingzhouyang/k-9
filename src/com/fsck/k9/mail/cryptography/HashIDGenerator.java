package com.fsck.k9.mail.cryptography;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generator to generate hash code
 * @author Hai Tao
 *
 */
public class HashIDGenerator {
	/**
	 * Generate hashID by specified content string and digest algorithm. 
	 * If not specify digest algorithm, use SHA-256 as default. 
	 * @param content
	 * @param digestAlgorithm
	 * @return
	 */
	public static String generateHashID(String content, String digestAlgorithm){
		MessageDigest md = null;
        String hashID = null;
        byte[] bt = content.getBytes();
        try {
            if (digestAlgorithm == null || digestAlgorithm.equals("")) {
            	digestAlgorithm = "SHA-256";
            }
            md = MessageDigest.getInstance(digestAlgorithm);
//            md.update(bt);
            hashID = DecimalParser.bytes2Hex(md.digest(bt)); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return hashID;
	}
	
	public static void main(String[] args){
//		System.out.println("xxxx");
		String content = "This is a test.";
		System.out.println(generateHashID(content, null));
	}
}
