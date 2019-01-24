package com.pinyougou.order.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-18<p>
 */
@Service(interfaceName = "com.pinyougou.service.OrderService")
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public void save(Order order) {
        try{
            // 1. 往订单表插入数据
            // 根据用户的购物车集合生成订单
            // 1.1 根据用户的id从Redis数据库获取购物车数据
            List<Cart> carts = (List<Cart>)redisTemplate
                    .boundValueOps("cart_" + order.getUserId()).get();

            // 定义支付总金额
            double totalMoney = 0;
            // 定义关联的订单id
            StringBuilder orderIds = new StringBuilder();

            // 1.2 List<Cart> 一个cart产生一个订单
            for (Cart cart : carts){
                // 创建新的订单
                Order order1 = new Order();
                // 生成主键id
                long orderId = idWorker.nextId();

                // 拼接订单id
                orderIds.append(orderId + ",");

                // 订单id
                order1.setOrderId(orderId);
                // 支付方式
                order1.setPaymentType(order.getPaymentType());
                // 订单的状态 1、未付款
                order1.setStatus("1");
                // 订单的创建时间
                order1.setCreateTime(new Date());
                // 订单的修改时间
                order1.setUpdateTime(order1.getCreateTime());
                // 购买的用户id
                order1.setUserId(order.getUserId());
                // 收货人地址
                order1.setReceiverAreaName(order.getReceiverAreaName());
                // 收货人手机号码
                order1.setReceiverMobile(order.getReceiverMobile());
                // 收货人姓名
                order1.setReceiver(order.getReceiver());
                // 订单来源
                order1.setSourceType(order.getSourceType());
                // 商家的id
                order1.setSellerId(cart.getSellerId());

                // 定义该订单总金额
                double money = 0;

                // 迭代商家购物车中的商品往订单明细表插入数据
                for (OrderItem orderItem : cart.getOrderItems()){
                    // 设置主键id
                    orderItem.setId(idWorker.nextId());
                    // 设置关联的订单编号
                    orderItem.setOrderId(orderId);

                    // 累计商品的金额
                    money += orderItem.getTotalFee().doubleValue();

                    // 往tb_order_item表添加数据
                    orderItemMapper.insertSelective(orderItem);
                }

                // 订单总金额
                order1.setPayment(new BigDecimal(money));

                // 累加
                totalMoney += money;

                // 往tb_order表添加数据
                orderMapper.insertSelective(order1);
            }

            // 往支付日志表插入数据
            if ("1".equals(order.getPaymentType())){ // 在线支付
                // 创建支付日志对象
                PayLog payLog = new PayLog();
                // 交易订单号
                payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
                // 创建时间
                payLog.setCreateTime(new Date());
                // 订单总金额 (分)
                payLog.setTotalFee((long)(totalMoney * 100));
                // 用户id
                payLog.setUserId(order.getUserId());
                // 支付状态
                payLog.setTradeState("0");
                // 关联订单id
                payLog.setOrderList(orderIds.substring(0, orderIds.length() - 1));
                // 支付类型
                payLog.setPayType(order.getPaymentType());
                // 插入数据
                payLogMapper.insertSelective(payLog);

                // 为了生成支付二维码方便(把支付日志对象存储到Redis数据库)
                redisTemplate.boundValueOps("payLog_" + order.getUserId()).set(payLog);
            }

            // 从Redis数据库中删除该用户的购物车数据
            redisTemplate.delete("cart_" + order.getUserId());

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Order order) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Order findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        return null;
    }

    @Override
    public List<Order> findByPage(Order order, int page, int rows) {
        return null;
    }

    /** 根据用户到Redis数据库中查询支付日志对象 */
    public PayLog findPayLoyFromRedis(String userId){
        try{
            return (PayLog) redisTemplate.boundValueOps("payLog_" + userId).get();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 修改支付状态 */
    public void updatePayStatus(String outTradeNo, String transactionId){
        try{
            // 1. 修改支付日志表
            PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
            // 设置支付时间
            payLog.setPayTime(new Date());
            // 设置交易状态码
            payLog.setTradeState("1");
            // 设置微信支付订单号
            payLog.setTransactionId(transactionId);
            // 修改 tb_pay_log
            payLogMapper.updateByPrimaryKeySelective(payLog);


            // 2. 修改关联的订单表
            // 获取多个订单id
            String[] orderIds = payLog.getOrderList().split(",");
            for (String orderId : orderIds){
                Order order = new Order();
                // 订单id
                order.setOrderId(Long.valueOf(orderId));
                // 2、已付款
                order.setStatus("2");
                // 支付时间
                order.setPaymentTime(new Date());
                // 修改 tb_order
                orderMapper.updateByPrimaryKeySelective(order);
            }

            // 3. 从Redis数据库删除支付日志对象
            redisTemplate.delete("payLog_" + payLog.getUserId());

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
