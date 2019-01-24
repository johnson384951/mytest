package com.pinyougou.sms;

import com.pinyougou.common.util.HttpClientUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * SmsTest
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-14<p>
 */
public class SmsTest {

    public static void main(String[] args){
        // 利用HttpClinet调用http接口(短信发送接口)
        HttpClientUtils httpClientUtils = new HttpClientUtils(false);

        // 定义Map集合封装请求参数
        Map<String, String> params = new HashMap<>();
        params.put("phone", "15768998003");
        params.put("signName", "五子连珠");
        params.put("templateCode", "SMS_11480310");
        params.put("templateParam","{'number' : '888888'}");

        // 发送post请求
        String res = httpClientUtils.sendPost("http://sms.pinyougou.com/sms/sendSms", params);
        System.out.println(res);
    }
}
