package com.pinyougou.service;

import java.util.Map;

/**
 * 微信支付服务接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-20<p>
 */
public interface WeixinPayService {

    /**
     * 调用微信支付系统的“统一下单”接口，
     * 获取支付URL: code_url
     */
    Map<String,String> genPayCode(String outTradeNo, String totalFee);

    /**
     * 调用微信支付系统的“查询订单”接口，
     * 获取支付状态: trade_state
     */
    Map<String,String> queryPayStatus(String outTradeNo);

    /**
     * 调用微信支付系统的“关闭订单”接口，
     * 获取关闭状态: return_code
     */
    Map<String,String> closePayTimeout(String outTradeNo);
}
