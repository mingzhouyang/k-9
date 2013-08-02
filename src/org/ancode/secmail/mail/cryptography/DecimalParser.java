package org.ancode.secmail.mail.cryptography;

import android.annotation.SuppressLint;

/**
 * Parse String between different decimal (binary, decimal, hexadecimal, octal...)
 * @author Hai Tao
 *
 */
public class DecimalParser {

	/**
	 * Parse byte to hex
	 * @param buf
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static String bytes2Hex(byte[] buf) {
        StringBuffer sb = new StringBuffer();  
        for (int i = 0; i < buf.length; i++) {  
                String hex = Integer.toHexString(buf[i] & 0xFF);  
                if (hex.length() == 1) {  
                        hex = '0' + hex;  
                }  
                sb.append(hex.toUpperCase());  
        }  
        return sb.toString();
    }
	
	/**
	 * Parse hex to byte
	 * @param hexStr
	 * @return
	 */
	public static byte[] hex2Bytes(String hexStr){
		if (hexStr.length() < 1)  
            return null;  
	    byte[] result = new byte[hexStr.length()/2];  
	    for (int i = 0;i< hexStr.length()/2; i++) {  
	            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);  
	            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);  
	            result[i] = (byte) (high * 16 + low);  
	    }  
	    return result;  
	}
}
