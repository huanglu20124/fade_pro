package com.fade.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;
/**
 * Created by LaiXiancheng on 2018/1/23.
 * Email: lxc.sysu@qq.com
 */

public class RongCloudHelper {
	static public String getRandNum(){
		Random random = new Random(System.currentTimeMillis());
		return String.valueOf(random.nextInt());
	}

	static public String getCurTime(){
		return String.valueOf(System.currentTimeMillis());
	}

	static public String getSignature(String randNum, String curTime){
		String hash = new String(Hex.encodeHex(DigestUtils.sha1("Sk52dbUr6eg"+randNum+curTime)));
		return hash;
	}
}
