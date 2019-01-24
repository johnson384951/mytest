package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀订单控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-21<p>
 */
@RestController
@RequestMapping("/order")
public class SeckillOrderController {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /** 秒杀下单 */
    @PostMapping("/saveOrder")
    public boolean saveOrder(Long id, HttpServletRequest request){
        try {
            // 获取登录用户名
            String userId = request.getRemoteUser();
            // 把订单保存Redis数据库 200/s
            seckillOrderService.saveOrderToRedis(id, userId);

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
        // 根据用户到Redis数据库中查询秒杀订单
        SeckillOrder seckillOrder = seckillOrderService
                .findSeckillOrderFromRedis(userId);

        // 获取支付金额
        long totalFee = (long)(seckillOrder.getMoney().doubleValue() * 100);
        // 调用微信支付服务接口
        return weixinPayService.genPayCode(seckillOrder.getId().toString(),
                String.valueOf(totalFee));
    }

    /** 检测支付状态 */
    @GetMapping("/queryPayStatus")
    public Map<String, Integer> queryPayStatus(String outTradeNo, HttpServletRequest request){
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);
        try{
            // 调用微信支付服务接口
            Map<String,String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            // 判断交易状态
            if (resMap != null && resMap.size() > 0){

                if ("SUCCESS".equals(resMap.get("trade_state"))){
                    // 后台业务处理
                    // 1. 把Redis中秒杀单订同步到数据库
                    String userId = request.getRemoteUser();
                    seckillOrderService.saveOrder(userId, resMap.get("transaction_id"));


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
