package com.dy.travel.comm;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	private MD5() {}
	
	public final static String getMessageDigest(byte[] buffer) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(buffer);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	public static String getMD5ForWX(String s) {

		StringBuffer sbuf = new StringBuffer();
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(s.getBytes("utf-8"));
			byte[] buf = mdTemp.digest();
			for (byte b : buf) {
				sbuf.append(Integer.toHexString(b >> 4 & 0xF).toUpperCase());
				sbuf.append(Integer.toHexString(b & 0xF).toUpperCase());
			}
		} catch (NoSuchAlgorithmException ex) {
			//Logger.getLogger(ComUtil.class.getName()).log(Level.SEVERE, null,ex);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sbuf.toString();
	}
}
