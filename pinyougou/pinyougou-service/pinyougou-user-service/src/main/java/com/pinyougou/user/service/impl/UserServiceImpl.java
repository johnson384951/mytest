package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-14<p>
 */
@Service(interfaceName = "com.pinyougou.service.UserService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Value("${sms.signName}")
    private String signName;
    @Value("${sms.templateCode}")
    private String templateCode;
    @Value("${sms.url}")
    private String smsUrl;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void save(User user) {
        try{
            // commons-codec.jar
            // 设置密码采用MD5加密
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            // 设置注册时间
            user.setCreated(new Date());
            // 设置修改时间
            user.setUpdated(user.getCreated());
            // 往tb_user表插入数据
            userMapper.insertSelective(user);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public User findOne(Serializable id) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public List<User> findByPage(User user, int page, int rows) {
        return null;
    }

    /** 发送短信验证码 */
    public boolean sendSmsCode(String phone){
        try{
            // 1. 生成随机的六位数字作为验证码
            // 12835c43-be77-464a-8d6f-d4e31e9be308
            String code = UUID.randomUUID().toString()
                    .replaceAll("-", "").replaceAll("[a-z|A-Z]", "").substring(0,6);
            System.out.println("验证码：" + code);

            // 2. 调用短信发送接口，把验证码发送到用户的手机
            // 利用HttpClinet调用http接口(短信发送接口)
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);

            // 定义Map集合封装请求参数
            Map<String, String> params = new HashMap<>();
            params.put("phone", phone);
            params.put("signName", signName);
            params.put("templateCode", templateCode);
            params.put("templateParam","{'number' : '"+ code +"'}");

            // 发送post请求
            String jsonStr = httpClientUtils.sendPost(smsUrl, params);
            System.out.println(jsonStr);

            // 3. 判断验证码是否发送成功，发送成功，需要把验证存储到Redis数据库(有效时间90秒)
            // 把jsonStr 字符串转化成 Map集合
            Map<String,Object> resMap = JSON.parseObject(jsonStr, Map.class);
            boolean success = (boolean)resMap.get("success");
            if (success){ // 发送成功
                // 需要把验证存储到Redis数据库(有效时间90秒)
                redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);
            }
            return success;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 检验验证码是否正确 */
    public boolean checkSmsCode(String phone, String smsCode){
        try{
            // 从Redis数据库获取短信验证码
            String code = (String)redisTemplate.boundValueOps(phone).get();
            return code != null && code.equals(smsCode);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
