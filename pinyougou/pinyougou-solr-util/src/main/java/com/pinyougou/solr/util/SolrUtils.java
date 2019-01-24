package com.pinyougou.solr.util;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.solr.SolrItem;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把tb_item表中的数据导入到Solr服务器的collection1索引库
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-07<p>
 */
@Component
public class SolrUtils {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;


    /** 导入数据到索引库 */
    public void importDataToSolr(){
        // select * from tb_item where status = 1
        Item item = new Item();
        // 正常的商品
        item.setStatus("1");
        // 条件查询
        List<Item> itemList = itemMapper.select(item);

        System.out.println("=====开始======");
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

        // 添加或修改索引库
        UpdateResponse updateResponse = solrTemplate.saveBeans(solrItems);
        if (updateResponse.getStatus() == 0){
            solrTemplate.commit();
        }else {
            solrTemplate.rollback();
        }

        System.out.println("=====结束======");
    }

    public static void main(String[] args){
        // 创建Spring容器
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        // 获取SolrUtils
        SolrUtils solrUtils = ac.getBean(SolrUtils.class);
        solrUtils.importDataToSolr();
    }
}
