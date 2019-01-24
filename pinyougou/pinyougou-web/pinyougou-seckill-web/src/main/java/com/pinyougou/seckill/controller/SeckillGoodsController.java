package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 秒杀商品控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-21<p>
 */
@RestController
@RequestMapping("/seckill")
public class SeckillGoodsController {

    @Reference(timeout = 10000)
    private SeckillGoodsService seckillGoodsService;

    /** 查询秒杀商品(正在秒杀) */
    @GetMapping("/findSeckillGoodsList")
    public List<SeckillGoods> findSeckillGoodsList(){
        long start = System.currentTimeMillis();
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.findSeckillGoodsList();
        long end = System.currentTimeMillis();
        System.out.println("共费的时间：" + (end - start));
        return seckillGoodsList;
    }

    /** 根据秒杀商品id查询秒杀商品对象 */
    @GetMapping("/findOne")
    public SeckillGoods findOne(Long id){
        return seckillGoodsService.findSeckillGoodsFromRedis(id);
    }


}
