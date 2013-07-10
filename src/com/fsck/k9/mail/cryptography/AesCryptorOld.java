package com.fsck.k9.mail.cryptography;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import com.fsck.k9.mail.filter.Base64;

public class AesCryptorOld {
	Cipher ecipher;
	Cipher dcipher;

	/**
	 * Input a string that will be md5 hashed to create the key.
	 * 
	 * @return void, cipher initialized
	 */

	public AesCryptorOld(String Key) throws CryptorException {
		try {
			SecretKeySpec skey = new SecretKeySpec(Hash.getInstance().SHA256(Key.getBytes()), "AES");
			this.setupCrypto(skey, "AES/CTR/NoPadding");
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	private void setupCrypto(SecretKey key, String Alg) throws CryptorException {
		// Create an 8-byte initialization vector
		byte[] iv = new byte[] { 0x14, 0x0a, 0x0a, 0x0b, 0x14, 0x08, 0x0c, 0x0b, 0x13, 0x4a, 0x0c, 0x05, 0x13, 0x50,
				0x02, 0x1c };

		Log.d("DB", byteToHex(iv));

		AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
		try {
			ecipher = Cipher.getInstance(Alg);
			dcipher = Cipher.getInstance(Alg);

			// CBC requires an initialization vector
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	// Buffer used to transport the bytes from one stream to another
	byte[] buf = new byte[1024];

	public void encrypt(InputStream in, OutputStream out) throws CryptorException {
		CipherOutputStream cout = null;
		try {
			// Bytes written to out will be encrypted
			cout = new CipherOutputStream(out, ecipher);

			// Read in the cleartext bytes and write to out to encrypt
			int numRead = 0;
			long progress = 0;
			while ((numRead = in.read(buf)) >= 0) {
				cout.write(buf, 0, numRead);
				progress += numRead;
				if (mEncryptProgressListener != null) {
					mEncryptProgressListener.OnEncryptUpdateProgress(progress);
				}
			}
			if (mEncryptProgressListener != null) {
				mEncryptProgressListener.OnEncryptFinished();
			}
		} catch (IOException e) {
			throw new CryptorException(e);
		} finally {
			if (cout != null)
				try {
					cout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Input is a string to encrypt.
	 * 
	 * @return a Hex string of the byte array
	 */
	public String encrypt(String plaintext) throws CryptorException {
		try {
			byte[] data = plaintext.getBytes("UTF-8");
			byte[] ciphertext = ecipher.doFinal(data, 0, data.length);
			return byteToHex(ciphertext);
		} catch (Exception e) {
			throw new CryptorException(e);
		}

	}

	/**
	 * Input is a string to encrypt.
	 * 
	 * @return a Hex string of the byte array
	 */
	public byte[] encrypt(byte[] plaintext) throws CryptorException {
		try {
			return ecipher.doFinal(plaintext, 0, plaintext.length);
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	/**
	 * Input is a string to encrypt.
	 * 
	 * @return a Hex string of the byte array
	 */
	public byte[] encrypt(byte[] plaintext, int lenth) throws CryptorException {
		try {
			return ecipher.doFinal(plaintext, 0, lenth);
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	/**
	 * 对给定的数据进行AES加密，并且将加密后的密文使用BASE64进行编码
	 * 
	 * @param plaintext
	 *            明文内容
	 * @return 经过BASE64编码后的字符串
	 */
	public String encryptBase64Encode(byte[] plaintext) throws CryptorException {
		try {
			return new String(Base64.encodeBase64(ecipher.doFinal(plaintext, 0, plaintext.length)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new CryptorException(e);
		}
	}

	public void decrypt(InputStream in, OutputStream out) throws CryptorException {
		CipherInputStream cin = null;
		try {
			// Bytes read from in will be decrypted
			cin = new CipherInputStream(in, dcipher);

			// Read in the decrypted bytes and write the cleartext to out
			int numRead = 0;
			long progress = 0;
			while ((numRead = cin.read(buf)) >= 0) {
				out.write(buf, 0, numRead);
				progress += numRead;
				if (mDecryptProgressListener != null) {
					mDecryptProgressListener.OnDecryptUpdateProgress(progress);
				}
			}
			if (mDecryptProgressListener != null) {
				mDecryptProgressListener.OnDecryptFinished();
			}
		} catch (IOException e) {
			throw new CryptorException(e);
		} finally {
			if (cin != null)
				try {
					cin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Input encrypted String represented in HEX
	 * 
	 * @return a string decrypted in plain text
	 */
	public String decrypt(String hexCipherText) throws CryptorException {
		try {
			String plaintext = new String(dcipher.doFinal(hexToByte(hexCipherText)), "UTF-8");
			return plaintext;
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	public byte[] decrypt(byte[] ciphertext, int length) throws CryptorException {
		try {
			return dcipher.doFinal(ciphertext, 0, length);
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	public byte[] decrypt(byte[] ciphertext) throws CryptorException {
		try {
			return dcipher.doFinal(ciphertext, 0, ciphertext.length);
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	public byte[] decryptBase64String(String base64String) throws CryptorException {
		try {
			byte[] data = Base64.decodeBase64(base64String.getBytes());
			return dcipher.doFinal(data, 0, data.length);
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	private static byte[] getMD5(String input) throws CryptorException {
		try {
			byte[] bytesOfMessage = input.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(bytesOfMessage);
		} catch (Exception e) {
			throw new CryptorException(e);
		}
	}

	static final String HEXES = "0123456789ABCDEF";

	public static String byteToHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	public static byte[] hexToByte(String hexString) {
		int len = hexString.length();
		byte[] ba = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			ba[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(
					hexString.charAt(i + 1), 16));
		}
		return ba;
	}

	private EncryptProgressListener mEncryptProgressListener;

	public void setEncryptProgressListener(EncryptProgressListener listener) {
		mEncryptProgressListener = listener;
	}

	/**
	 * 加密进度接口，用来触发加密数据进度的接口。
	 * 
	 * @author mada
	 * 
	 */
	public interface EncryptProgressListener {
		/**
		 * 当加密数据有更新时候更新进度
		 * 
		 * @param progress
		 *            当前进度，已加密的字节长度
		 */
		public void OnEncryptUpdateProgress(long progress);

		/**
		 * 所有数据加密完成
		 */
		public void OnEncryptFinished();
	}

	private DecryptProgressListener mDecryptProgressListener;

	public void setDecryptProgressListener(DecryptProgressListener listener) {
		mDecryptProgressListener = listener;
	}

	/**
	 * 解密进度接口，用来触发解密数据进度的接口。
	 * 
	 * @author mada
	 * 
	 */
	public interface DecryptProgressListener {
		/**
		 * 当解密数据有更新时候更新进度
		 * 
		 * @param progress
		 *            当前进度，已解密的字节长度
		 */
		public void OnDecryptUpdateProgress(long progress);

		/**
		 * 所有数据解密完成
		 */
		public void OnDecryptFinished();
	}

	public static void main(String args[]) throws CryptorException{
		AesCryptor enc = new AesCryptor("password");
		String key = enc.encrypt("key1");
		System.out.println(key);
	}
}
