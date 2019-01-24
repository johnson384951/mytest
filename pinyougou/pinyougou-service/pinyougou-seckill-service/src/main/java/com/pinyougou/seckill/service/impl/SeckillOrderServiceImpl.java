package com.pinyougou.seckill.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 秒杀订单服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-21<p>
 */
@Service(interfaceName = "com.pinyougou.service.SeckillOrderService")
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    public void save(SeckillOrder seckillOrder) {

    }

    @Override
    public void update(SeckillOrder seckillOrder) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public SeckillOrder findOne(Serializable id) {
        return null;
    }

    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    @Override
    public List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows) {
        return null;
    }

    /**
     * 把订单保存Redis数据库
     * synchronized: 线程锁 (单进程)
     * 分布式锁  进程锁(多进程)
     * 解决方案：
     * -- Redis实现 (加锁 解锁)
     * -- MySQL实现
     * -- zookeeper实现
     *
     * MQ消息队列 (点对点)
     * */
    public synchronized void  saveOrderToRedis(Long id, String userId){
        try{
            // 1. 从Redis中获取该秒杀商品，判断剩余库存数量
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate
                    .boundHashOps("seckillGoodsList").get(id);

//            if (seckillGoods.getStockCount() > 0){
//                // 发送消息到MQ中间件
//                // MapMessage mm id 与 userId
//                //jmsTemplate.send();
//
//
//                /** ########### 消息消费者处理下面业务 #############  */
//                // 减库存
//                seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
//                // 把该秒杀商品的剩余库存数量同步到mysql数据库(同步一次)
//                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
//                // 判断剩余库存数
//                if (seckillGoods.getStockCount() == 0){ // 秒光了
//                    // 从Redis数据库中删除该秒杀商品
//                    redisTemplate.boundHashOps("seckillGoodsList").delete(id);
//                }else{
//                    // 把秒杀商品更新到Redis数据库
//                    redisTemplate.boundHashOps("seckillGoodsList").put(id, seckillGoods);
//                }
//
//
//                // 创建秒杀订单
//                SeckillOrder seckillOrder = new SeckillOrder();
//                // 主键id
//                seckillOrder.setId(idWorker.nextId());
//                // 秒杀商品id
//                seckillOrder.setSeckillId(id);
//                // 支付金额
//                seckillOrder.setMoney(seckillGoods.getCostPrice());
//                // 用户id
//                seckillOrder.setUserId(userId);
//                // 商家id
//                seckillOrder.setSellerId(seckillGoods.getSellerId());
//                // 创建时间
//                seckillOrder.setCreateTime(new Date());
//                // 支付状态：未支付
//                seckillOrder.setStatus("0");
//
//                // 把秒杀订单存入Redis
//                redisTemplate.boundHashOps("seckillOrderList").put(userId, seckillOrder);
//            }

            // 减库存
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            // 判断剩余库存数
            if (seckillGoods.getStockCount() == 0){ // 秒光了 Redis挂掉了怎么办
                // 把该秒杀商品的剩余库存数量同步到mysql数据库(同步一次)
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                // 从Redis数据库中删除该秒杀商品
                redisTemplate.boundHashOps("seckillGoodsList").delete(id);
            }else{
                // 把秒杀商品更新到Redis数据库
                redisTemplate.boundHashOps("seckillGoodsList").put(id, seckillGoods);
            }


            // 创建秒杀订单
            SeckillOrder seckillOrder = new SeckillOrder();
            // 主键id
            seckillOrder.setId(idWorker.nextId());
            // 秒杀商品id
            seckillOrder.setSeckillId(id);
            // 支付金额
            seckillOrder.setMoney(seckillGoods.getCostPrice());
            // 用户id
            seckillOrder.setUserId(userId);
            // 商家id
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            // 创建时间
            seckillOrder.setCreateTime(new Date());
            // 支付状态：未支付
            seckillOrder.setStatus("0");

            // 把秒杀订单存入Redis
            redisTemplate.boundHashOps("seckillOrderList").put(userId, seckillOrder);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据用户到Redis数据库中查询秒杀订单 */
    public SeckillOrder findSeckillOrderFromRedis(String userId){
        try{
            return (SeckillOrder)redisTemplate.
                    boundHashOps("seckillOrderList").get(userId);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 保存订单到数据库 */
    public void saveOrder(String userId, String transactionId){
        try{
            // 1. 从Redis获取该用户的秒杀订单
            SeckillOrder seckillOrder = findSeckillOrderFromRedis(userId);

            // 2. 把秒杀订单同步到数据库
            seckillOrder.setTransactionId(transactionId);
            seckillOrder.setPayTime(new Date());
            seckillOrder.setStatus("1");
            seckillOrderMapper.insertSelective(seckillOrder);

            // 3. 从Redis中删除该秒杀订单
            redisTemplate.boundHashOps("seckillOrderList").delete(userId);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 查询超时未支付的订单 */
    public List<SeckillOrder> findOrderByTimeout(){
        try{
            // 定义集合放超时未支付的订单
            List<SeckillOrder> seckillOrders = new ArrayList<>();

            // 1. 从Redis数据库中查询所有的未支付的订单
            List<SeckillOrder> seckillOrderList = redisTemplate
                    .boundHashOps("seckillOrderList").values();
            // 2. 迭代未支付的订单集合，找超出5分钟还没支付的订单
            for (SeckillOrder seckillOrder : seckillOrderList){
                // 订单的创建时间与当前系统时间比较
                long date = new Date().getTime() - 5 * 60 * 1000;
                if (seckillOrder.getCreateTime().getTime() < date){
                    // 把超时未支付的订单放到新的集合
                    seckillOrders.add(seckillOrder);
                }

            }
            return seckillOrders;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**  删除Redis中的秒杀订单，增加秒杀商品的库存 */
    public void deleteOrderFromRedis(SeckillOrder seckillOrder){
        try{
            // 1.  删除Redis中的秒杀订单
            redisTemplate.boundHashOps("seckillOrderList")
                    .delete(seckillOrder.getUserId());


            // 2. 增加秒杀商品的库存
            // 从Redis数据库获取秒杀商品
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate
                    .boundHashOps("seckillGoodsList")
                    .get(seckillOrder.getSeckillId());
            if (seckillGoods != null){
                // 增加库存
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            }else{
               // 从mysql数据库查询秒杀商品
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
                seckillGoods.setStockCount(1);
            }
            // 把秒杀商品同步到Redis
            redisTemplate.boundHashOps("seckillGoodsList")
                    .put(seckillGoods.getId(), seckillGoods);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
