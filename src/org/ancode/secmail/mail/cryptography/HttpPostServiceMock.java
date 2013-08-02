package org.ancode.secmail.mail.cryptography;

public class HttpPostServiceMock extends HttpPostService {
	public static PostResult postRegRequest(String mailAddress){
		PostResult pr = new PostResult();
		pr.setRetCode("ok");
		return pr;
	}
}
