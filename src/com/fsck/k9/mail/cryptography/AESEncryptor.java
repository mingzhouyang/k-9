package com.fsck.k9.mail.cryptography;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * This class is from:
 * 
 * Encryptor.java
 * Copyright 2008 Zach Scrivena
 * zachscrivena@gmail.com
 * http://zs.freeshell.org/
 */

/**
 * Perform AES-128 encryption.
 */
public final class AESEncryptor {
	/**
	 * name of the character set to use for converting between characters and
	 * bytes
	 */
	private static final String CHARSET_NAME = "UTF-8";

	/** random number generator algorithm */
	private static final String RNG_ALGORITHM = "SHA1PRNG";

	/**
	 * message digest algorithm (must be sufficiently long to provide the key
	 * and initialization vector)
	 */
	private static final String DIGEST_ALGORITHM = "SHA-256";

	/** key algorithm (must be compatible with CIPHER_ALGORITHM) */
	private static final String KEY_ALGORITHM = "AES";

	/** cipher algorithm (must be compatible with KEY_ALGORITHM) */
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

	private static final byte[] salt = new byte[] { 'u', 'f', 't', 'a', 'e',
			's', 0, 1 };

	private static final int iterations = 3;

	/**
	 * Private constructor that should never be called.
	 */
	private AESEncryptor() {
	}
	
	private static String inputStream2String(InputStream is) throws IOException{ 
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        int   i=-1; 
        while((i=is.read())!=-1){ 
        baos.write(i); 
        } 
       return baos.toString(); 
	} 
	
	public static InputStream encrypt(InputStream is, String password) throws UnsupportedEncodingException, CryptorException, IOException{
		return new ByteArrayInputStream(encrypt(inputStream2String(is), password).getBytes(CHARSET_NAME));
	}
	
	public static String encrypt(String content, String password) throws CryptorException{
		try {
			byte[] byteContent = content.getBytes(CHARSET_NAME);
			return DecimalParser.bytes2Hex(encrypt(password, byteContent));
		} catch (UnsupportedEncodingException e) {
			throw new CryptorException(e);
		}
	}
	
	public static String decrypt(String content, String password) throws CryptorException {
		byte[] byteContent = DecimalParser.hex2Bytes(content); 
		return new String(decrypt(password, byteContent));
	}

	/**
	 * Encrypt the specified cleartext using the given password. With the
	 * correct salt, number of iterations, and password, the decrypt() method
	 * reverses the effect of this method. This method generates and uses a
	 * random salt, and the user-specified number of iterations and password to
	 * create a 16-byte secret key and 16-byte initialization vector. The secret
	 * key and initialization vector are then used in the AES-128 cipher to
	 * encrypt the given cleartext. By using default salt and defualt
	 * iterations(3).
	 * 
	 * @param password
	 *            password to be used for encryption
	 * @param cleartext
	 *            cleartext to be encrypted
	 * @return ciphertext
	 * @throws Exception
	 *             on any error encountered in encryption
	 */
	public static byte[] encrypt(final String password, final byte[] cleartext)
			throws CryptorException {
		return encrypt(salt, iterations, password, cleartext);
	}

	/**
	 * Encrypt the specified cleartext using the given password. With the
	 * correct salt, number of iterations, and password, the decrypt() method
	 * reverses the effect of this method. This method generates and uses a
	 * random salt, and the user-specified number of iterations and password to
	 * create a 16-byte secret key and 16-byte initialization vector. The secret
	 * key and initialization vector are then used in the AES-128 cipher to
	 * encrypt the given cleartext.
	 * 
	 * @param salt
	 *            salt that was used in the encryption (to be populated)
	 * @param iterations
	 *            number of iterations to use in salting
	 * @param password
	 *            password to be used for encryption
	 * @param cleartext
	 *            cleartext to be encrypted
	 * @return ciphertext
	 * @throws CryptorException
	 *             on any error encountered in encryption
	 */
	public static byte[] encrypt(final byte[] salt, final int iterations,
			final String password, final byte[] cleartext)
			throws CryptorException {
		/* generate salt randomly */
		try {
			SecureRandom.getInstance(RNG_ALGORITHM).nextBytes(salt);

			/* compute key and initialization vector */
			final MessageDigest shaDigest = MessageDigest
					.getInstance(DIGEST_ALGORITHM);
			byte[] pw = password.getBytes(CHARSET_NAME);

			for (int i = 0; i < iterations; i++) {
				/* add salt */
				final byte[] salted = new byte[pw.length + salt.length];
				System.arraycopy(pw, 0, salted, 0, pw.length);
				System.arraycopy(salt, 0, salted, pw.length, salt.length);
				Arrays.fill(pw, (byte) 0x00);

				/* compute SHA-256 digest */
				shaDigest.reset();
				pw = shaDigest.digest(salted);
				Arrays.fill(salted, (byte) 0x00);
			}

			/*
			 * extract the 16-byte key and initialization vector from the
			 * SHA-256 digest
			 */
			final byte[] key = new byte[16];
			final byte[] iv = new byte[16];
			System.arraycopy(pw, 0, key, 0, 16);
			System.arraycopy(pw, 16, iv, 0, 16);
			Arrays.fill(pw, (byte) 0x00);

			/* perform AES-128 encryption */
			final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key,
					KEY_ALGORITHM), new IvParameterSpec(iv));

			Arrays.fill(key, (byte) 0x00);
			Arrays.fill(iv, (byte) 0x00);

			return cipher.doFinal(cleartext);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptorException(e);
		} catch (NoSuchPaddingException e) {
			throw new CryptorException(e);
		} catch (UnsupportedEncodingException e) {
			throw new CryptorException(e);
		} catch (InvalidKeyException e) {
			throw new CryptorException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptorException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CryptorException(e);
		} catch (BadPaddingException e) {
			throw new CryptorException(e);
		}
	}

	/**
	 * Decrypt the specified ciphertext using the given password. With the
	 * correct salt, number of iterations, and password, this method reverses
	 * the effect of the encrypt() method. This method uses the user-specified
	 * salt, number of iterations, and password to recreate the 16-byte secret
	 * key and 16-byte initialization vector. The secret key and initialization
	 * vector are then used in the AES-128 cipher to decrypt the given
	 * ciphertext. By using default salt and defualt iterations(3).
	 * 
	 * @param password
	 *            password to be used for decryption
	 * @param ciphertext
	 *            ciphertext to be decrypted
	 * @return cleartext
	 * @throws Exception
	 *             on any error encountered in decryption
	 */
	public static byte[] decrypt(final String password, final byte[] ciphertext)
			throws CryptorException {
		return decrypt(salt, iterations, password, ciphertext);
	}

	/**
	 * Decrypt the specified ciphertext using the given password. With the
	 * correct salt, number of iterations, and password, this method reverses
	 * the effect of the encrypt() method. This method uses the user-specified
	 * salt, number of iterations, and password to recreate the 16-byte secret
	 * key and 16-byte initialization vector. The secret key and initialization
	 * vector are then used in the AES-128 cipher to decrypt the given
	 * ciphertext.
	 * 
	 * @param salt
	 *            salt to be used in decryption
	 * @param iterations
	 *            number of iterations to use in salting
	 * @param password
	 *            password to be used for decryption
	 * @param ciphertext
	 *            ciphertext to be decrypted
	 * @return cleartext
	 * @throws Exception
	 *             on any error encountered in decryption
	 */
	public static byte[] decrypt(final byte[] salt, final int iterations,
			final String password, final byte[] ciphertext)
			throws CryptorException {
		try {
			/* compute key and initialization vector */
			final MessageDigest shaDigest = MessageDigest
					.getInstance(DIGEST_ALGORITHM);
			byte[] pw = password.getBytes(CHARSET_NAME);

			for (int i = 0; i < iterations; i++) {
				/* add salt */
				final byte[] salted = new byte[pw.length + salt.length];
				System.arraycopy(pw, 0, salted, 0, pw.length);
				System.arraycopy(salt, 0, salted, pw.length, salt.length);
				Arrays.fill(pw, (byte) 0x00);

				/* compute SHA-256 digest */
				shaDigest.reset();
				pw = shaDigest.digest(salted);
				Arrays.fill(salted, (byte) 0x00);
			}

			/*
			 * extract the 16-byte key and initialization vector from the
			 * SHA-256 digest
			 */
			final byte[] key = new byte[16];
			final byte[] iv = new byte[16];
			System.arraycopy(pw, 0, key, 0, 16);
			System.arraycopy(pw, 16, iv, 0, 16);
			Arrays.fill(pw, (byte) 0x00);

			/* perform AES-128 decryption */
			Cipher cipher;

			cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key,
					KEY_ALGORITHM), new IvParameterSpec(iv));
			Arrays.fill(key, (byte) 0x00);
			Arrays.fill(iv, (byte) 0x00);

			return cipher.doFinal(ciphertext);
		} catch (InvalidKeyException e) {
			throw new CryptorException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptorException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptorException(e);
		} catch (NoSuchPaddingException e) {
			throw new CryptorException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CryptorException(e);
		} catch (BadPaddingException e) {
			throw new CryptorException(e);
		} catch (UnsupportedEncodingException e) {
			throw new CryptorException(e);
		}
	}
	
	public static void main(String[] args){
		String content = "test mail";  
		String password = "1234567890abcdef";
		try{
			String encrypt = AESEncryptor.encrypt(content, password);
			System.out.println(encrypt);
			System.out.println(AESEncryptor.decrypt(encrypt, password));
		}catch (CryptorException e){
			e.printStackTrace();
		}
	}
}