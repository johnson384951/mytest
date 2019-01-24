package com.pinyougou.sms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.pinyougou.service.SmsService;
import org.springframework.beans.factory.annotation.Value;

/**
 * 短信服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-14<p>
 */
@Service(interfaceName = "com.pinyougou.service.SmsService")
public class SmsServiceImpl implements SmsService {

    // 产品名称: 云通信短信API产品,开发者无需替换
    private static final String PRODUCT  = "Dysmsapi";
    // 产品域名: 开发者无需替换
    private static final String DOMAIN = "dysmsapi.aliyuncs.com";

    // 此处需要替换成开发者自己的AK(在阿里云访问控制台寻找)
    // 签名的key
    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    // 签名的密钥
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;


    /**
     * 发送短信方法
     * @param phone 手机号码
     * @param signName 签名
     * @param templateCode 短信模板
     * @param templateParam 模板参数(json格式)
     * @return true 发送成功 false 发送失败
     */
    public boolean sendSms(String phone, String signName,
                    String templateCode, String templateParam){
        try {
            // 可自助调整超时时间
            System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
            System.setProperty("sun.net.client.defaultReadTimeout", "10000");

            // 初始化acsClient,暂不支持region化
            IClientProfile profile = DefaultProfile.
                    getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", PRODUCT, DOMAIN);
            IAcsClient acsClient = new DefaultAcsClient(profile);

            // 组装请求对象-具体描述见控制台-文档部分内容
            SendSmsRequest request = new SendSmsRequest();
            // 必填:待发送手机号
            request.setPhoneNumbers(phone);
            // 必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            // 必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(templateCode);
            // 可选:模板中的变量替换JSON串,如模板内容为
            // "验证码${number}，您正进行身份验证，打死不告诉别人！"时,此处的值为
            request.setTemplateParam(templateParam);

            // 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
            request.setOutId("yourOutId");

            // hint 此处可能会抛出异常，注意catch
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            // 判断短信是否发送成功
            return "OK".equals(sendSmsResponse.getCode());
        }catch (Exception ex){
            throw new RuntimeException("短信发送出现异常！", ex);
        }
    }
}
