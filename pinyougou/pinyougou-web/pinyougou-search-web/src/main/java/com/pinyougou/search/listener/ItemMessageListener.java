package com.pinyougou.search.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.Item;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 消息监听器(创建商品的索引)
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-13<p>
 */
public class ItemMessageListener implements SessionAwareMessageListener<ObjectMessage> {

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Reference(timeout = 10000)
    private ItemSearchService itemSearchService;


    @Override
    public void onMessage(ObjectMessage objectMessage, Session session) throws JMSException {
        System.out.println("=========ItemMessageListener=========");
        try{
            // 获取消息内容
            Long[] goodsIds = (Long[])objectMessage.getObject();
            System.out.println("goodsIds: " + Arrays.toString(goodsIds));

            // 1. 根据多个goodsId到tb_item表查询SKU商品数据
            List<Item> itemList = goodsService.findItemByGoodsId(goodsIds);

            // 2. 把List<Item> 集合 转化成 List<SolrItem>
            // 定义SolrItem集合
            List<SolrItem> solrItems = new ArrayList<>();
            for (Item item1 : itemList){
                // 循环把Item对象转化成SolrItem
                SolrItem solrItem = new SolrItem();
                solrItem.setId(item1.getId());
                solrItem.setTitle(item1.getTitle());
                solrItem.setPrice(item1.getPrice());
                solrItem.setImage(item1.getImage());
                solrItem.setGoodsId(item1.getGoodsId());
                solrItem.setCategory(item1.getCategory());
                solrItem.setBrand(item1.getBrand());
                solrItem.setSeller(item1.getSeller());
                solrItem.setUpdateTime(item1.getUpdateTime());
                // 获取规格选项数据
                Map<String,String> specMap = JSON.parseObject(item1.getSpec(),Map.class);
                // 动态域
                solrItem.setSpecMap(specMap);
                solrItems.add(solrItem);
            }

            // 3. 调用搜索服务接口把商品数据同步到索引库
            itemSearchService.saveOrUpdate(solrItems);
            // 事交事务
            session.commit();
        }catch (Exception ex){
            // 回滚事务
            session.rollback();
            throw new RuntimeException(ex);
        }
    }
}