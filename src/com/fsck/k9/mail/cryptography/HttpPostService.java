package com.fsck.k9.mail.cryptography;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class HttpPostService {
	private static String REG_REQUEST = "https://www.han2011.com/secmail/reg_request";
	private static String REG_CONFIRM = "https://www.han2011.com/secmail/reg_confirm";
	private static String SEND_EMAIL = "https://www.han2011.com/secmail/send";
	private static String RECEIVE_EMAIL = "https://www.han2011.com/secmail/receive";

	
	
	private static PostResult post(String action, List<NameValuePair> params){
		HttpPost httpRequest=null;     
		HttpResponse httpResponse;
		httpRequest=new HttpPost(action);         
		try {             
			httpRequest.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));  
			httpResponse=new DefaultHttpClient().execute(httpRequest);  
			if(httpResponse.getStatusLine().getStatusCode()==200){    
				return parsePostResult(httpResponse.getEntity().getContent());
			}         
		} catch (Exception e) {  
			//Add exception process logic here
		} 
		return new PostResult();
	}
	
	private static PostResult parsePostResult(InputStream is){
		PostResult pr = new PostResult();
		XmlPullParser parser = Xml.newPullParser();  
		try {
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();  
		    while (eventType != XmlPullParser.END_DOCUMENT) {  
		    	switch (eventType) {  
		            case XmlPullParser.START_DOCUMENT:  
		                break;  
		            case XmlPullParser.START_TAG:  
		            	String tagName = parser.getName();
		                if (tagName.equals("r")) {  
		                	eventType = parser.next();  	
		                    pr.setRetCode(parser.getText()); 
		                }else if(tagName.equals("p")){
		                	eventType = parser.next();  	
		                    pr.setPassword(parser.getText()); 
		                }
		                else if (tagName.startsWith("uuid")) {  
		                    eventType = parser.next();  
		                    pr.getUuidMap().put(tagName, parser.getText());
		                } 
		                break;  
		            case XmlPullParser.END_TAG:  
		                break;  
		            }  
		            eventType = parser.next();  
		        } 
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return pr;
	}
	
	/**
	 * Do post to send reg request
	 * @param mailAddress
	 * @return
	 */
	public static PostResult postRegRequest(String mailAddress){
		List <NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mail", mailAddress));
		return post(REG_REQUEST, params);
	}
	
	/**
	 * Do post to send reg confirmation
	 * @param mailAddress
	 * @param password
	 * @param regcode
	 * @return
	 */
	public static PostResult postRegConfirm(String mailAddress, String regcode){
		List <NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mail", mailAddress));
//		params.add(new BasicNameValuePair("passwd", HashIDGenerator.SHA256(password + regCode)));
		params.add(new BasicNameValuePair("regCode", regcode));
		return post(REG_CONFIRM, params);
	}
	
	/**
	 * Do post to send email encrypt information
	 * @param from
	 * @param to
	 * @param password
	 * @param regcode
	 * @param aesKeys
	 * @return
	 */
	public static PostResult postSendEmail(String from, String to, String password, String regcode, List<AESKEYObject> aesKeys){
		List <NameValuePair> params = new ArrayList<NameValuePair>();
		String passwd = HashIDGenerator.SHA256(password + regcode);
		params.add(new BasicNameValuePair("from", from));
		params.add(new BasicNameValuePair("to", to));
		params.add(new BasicNameValuePair("verify", HashIDGenerator.SHA256(passwd + aesKeys.get(0).getUuid())));
		for(int i = 0; i < aesKeys.size(); i++){
			AESKEYObject aesKey = aesKeys.get(i);
			params.add(new BasicNameValuePair("uuid" + (i + 1), aesKey.getUuid()));
			String pwd = passwd + HashIDGenerator.SHA256(aesKey.getUuid());
			String key;
			try {
				key = AESEncryptor.encrypt(aesKey.getAesKey(), pwd);
			} catch (CryptorException e) {
				return new PostResult();
			}
			params.add(new BasicNameValuePair("key" + (i + 1), key));
		}
		return post(SEND_EMAIL, params);
	}
	
	/**
	 * Do post to get email encrypt information
	 * @param owner
	 * @param password
	 * @param regcode
	 * @param uuidList
	 * @return
	 */
	public static List<String> postReceiveEmail(String owner, String password, String regcode, List<String> uuidList){
		List <NameValuePair> params = new ArrayList<NameValuePair>();
		String passwd = HashIDGenerator.SHA256(password + regcode);
		params.add(new BasicNameValuePair("owner", owner));
		params.add(new BasicNameValuePair("verify", HashIDGenerator.SHA256(passwd + uuidList.get(0))));
		for(int i = 0; i < uuidList.size(); i++){
			params.add(new BasicNameValuePair("uuid" + (i + 1), uuidList.get(i)));
		}
		PostResult pr = post(RECEIVE_EMAIL, params);
		List<String> aesKeyList = new ArrayList<String>();
		if(pr.isSuccess()){
			if(!pr.getUuidMap().isEmpty()){
				for(int i = 0; i < uuidList.size(); i++){
					String fromUuid = pr.getUuidMap().get("uuid" + (i + 1));
					try {
						aesKeyList.add(AESEncryptor.decrypt(fromUuid, passwd + HashIDGenerator.SHA256(uuidList.get(i))));
					} catch (CryptorException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return aesKeyList;
	}
	
	
}
