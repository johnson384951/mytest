package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.GoodsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-11<p>
 */
@Controller
public class ItemController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    /**
     * 获取商品的详情信息
     * https://item.pinyougou.com/32901296096.html
     * @PathVariable: 获取请求URL中变量
     * */
    @GetMapping("/{goodsId}")
    public String getGoods(@PathVariable("goodsId")Long goodsId, Model model){

        System.out.println("goodsId: " + goodsId);
        // 200/1s 吐的速度(线程)
        // dataModel 放到Redis 800-1000/1s
        // 静态的页面 1500-2000/1s
        // Model ： 数据模型，springmvc会根据视图解析器，把数据模型中变量放到不同的地方
        // jsp: Model中的数据放到request里面
        // freemarker: Model就是FreeMarker的数据模型
        // 根据goodsId查询商品详情信息
        Map<String,Object> dataModel = goodsService.getGoods(goodsId);
        // 往数据模型中放数据
        model.addAllAttributes(dataModel);

        return "item";
    }
}
