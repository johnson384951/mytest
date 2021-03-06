package com.pinyougou.seckill.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-17<p>
 */
@RestController
public class LoginController {

    /** 获取登录用户名 */
    @GetMapping("/user/showName")
    public Map<String, String> showName(HttpServletRequest request){
        // 获取当前登录用户名
        String loginName = request.getRemoteUser();
        System.out.println("登录用户名：" + loginName);

        System.out.println(request.getRequestURL().toString());

        Map<String, String> data = new HashMap<>();
        data.put("loginName", loginName);
        return data;
    }

}
