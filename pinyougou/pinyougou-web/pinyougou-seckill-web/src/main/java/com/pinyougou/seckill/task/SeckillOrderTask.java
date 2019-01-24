package com.pinyougou.seckill.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 秒杀订单任务调度类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-23<p>
 */
@Component
public class SeckillOrderTask {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /**
     * 任务调度方法(关闭超时未支付的订单)
     * cron: 时间表达式
     * 秒 分 小时 日 月  周
     *  */
    @Scheduled(cron = "0/3 * * * * ?")
    public void closeOrder(){
        System.out.println("当前时间: " + new Date());
        // 1. 查询超时未支付的订单
        List<SeckillOrder> seckillOrderList = seckillOrderService.findOrderByTimeout();

        // 2. 调用微信支付系统的"关闭订单"接口
        if (seckillOrderList.size() > 0){
            System.out.println("超时未支付的订单数量：" + seckillOrderList.size());
            for (SeckillOrder seckillOrder : seckillOrderList){
                // 关闭超时未支付的订单
                Map<String,String> resMap = weixinPayService
                        .closePayTimeout(seckillOrder.getId().toString());
                // 判断是否关单成功
                if (resMap != null && "SUCCESS".equals(resMap.get("return_code"))){
                    // 3. 删除Redis中的秒杀订单，增加秒杀商品的库存
                    seckillOrderService.deleteOrderFromRedis(seckillOrder);
                }
            }

        }

    }

}
