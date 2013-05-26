package com.fsck.k9.mail.cryptography;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to generate random key
 * @author Hai Tao
 *
 */
public class RandomKeyGenerator {

	public static String getRandomKey(int length){
		char[] ss = new char[length];
		int i=0;
		while(i<length) {
		    int f = (int) (Math.random()*3%3);
		    if(f==0)  
		    	ss[i] = (char) ('A'+Math.random()*26);
		    else if(f==1)  
		    	ss[i] = (char) ('a'+Math.random()*26);
		    else 
		    	ss[i] = (char) ('0'+Math.random()*10);    
		    i++;
		 }
		 return new String(ss);
	}
	
	public static List<String> getRandomKeyList(int length, int size){
		List<String> list = new ArrayList<String>();
		for(int i=0; i<size; i++){
			list.add(getRandomKey(length));
		}
		return list;
	}
	
	public static void main(String[] args){
		System.out.println(getRandomKey(16));
		System.out.println(getRandomKey(1));
		System.out.println(getRandomKey(0));
	}
}
