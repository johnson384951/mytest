package com.pinyougou.service;

import com.pinyougou.pojo.SeckillOrder;
import java.util.List;
import java.io.Serializable;
/**
 * SeckillOrderService 服务接口
 * @date 2018-12-27 15:38:18
 * @version 1.0
 */
public interface SeckillOrderService {

	/** 添加方法 */
	void save(SeckillOrder seckillOrder);

	/** 修改方法 */
	void update(SeckillOrder seckillOrder);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	SeckillOrder findOne(Serializable id);

	/** 查询全部 */
	List<SeckillOrder> findAll();

	/** 多条件分页查询 */
	List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows);

	/** 把订单保存Redis数据库 */
    void saveOrderToRedis(Long id, String userId);

    /** 根据用户到Redis数据库中查询秒杀订单 */
    SeckillOrder findSeckillOrderFromRedis(String userId);

    /** 保存订单到数据库 */
	void saveOrder(String userId, String transactionId);

	/** 查询超时未支付的订单 */
    List<SeckillOrder> findOrderByTimeout();

    /**  删除Redis中的秒杀订单，增加秒杀商品的库存 */
	void deleteOrderFromRedis(SeckillOrder seckillOrder);
}