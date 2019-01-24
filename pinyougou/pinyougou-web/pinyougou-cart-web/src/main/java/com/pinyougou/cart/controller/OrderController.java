package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-18<p>
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference(timeout = 10000)
    private OrderService orderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /** 保存订单 */
    @PostMapping("/saveOrder")
    public boolean saveOrder(@RequestBody Order order,
                             HttpServletRequest request){
        try{
            // 获取登录用户名
            String userId = request.getRemoteUser();
            // 设置用户名
            order.setUserId(userId);
            // 保存订单
            orderService.save(order);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 生成微信支付二维码 */
    @GetMapping("/genPayCode")
    public Map<String, String> genPayCode(HttpServletRequest request){
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 根据用户到Redis数据库中查询支付日志对象
        PayLog payLog = orderService.findPayLoyFromRedis(userId);
        // 调用微信支付服务接口
        return weixinPayService.genPayCode(payLog.getOutTradeNo(), payLog.getTotalFee().toString());
    }

    /** 检测支付状态 */
    @GetMapping("/queryPayStatus")
    public Map<String, Integer> queryPayStatus(String outTradeNo){
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);
        try{
            // 调用微信支付服务接口
            Map<String,String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            // 判断交易状态
            if (resMap != null && resMap.size() > 0){

                if ("SUCCESS".equals(resMap.get("trade_state"))){
                    // 后台业务处理
                    // 1. 修改支付状态 2. 修改订单的状态
                    orderService.updatePayStatus(outTradeNo, resMap.get("transaction_id"));
                    // SUCCESS—支付成功
                    data.put("status", 1);
                }

                if ("NOTPAY".equals(resMap.get("trade_state"))){
                    // NOTPAY—未支付
                    data.put("status", 2);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }
}
