package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品搜索服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-07<p>
 */
@Service(interfaceName = "com.pinyougou.service.ItemSearchService")
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;


    /** 添加或修改索引 */
    public void saveOrUpdate(List<SolrItem> solrItems){
        try{
            // 添加或修改索引库
            UpdateResponse updateResponse = solrTemplate.saveBeans(solrItems);
            if (updateResponse.getStatus() == 0){
                solrTemplate.commit();
            }else{
                solrTemplate.rollback();
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 删除商品的索引 */
    public void delete(Long[] goodsIds){
        try{
            // 创建查询对象
            Query query = new SimpleQuery();
            // 创建条件对象 goodsId in (?,?,?)
            Criteria criteria = new Criteria("goodsId")
                    .in(Arrays.asList(goodsIds));
            // 添加条件对象
            query.addCriteria(criteria);

            // 删除索引库中的索引
            UpdateResponse updateResponse = solrTemplate.delete(query);
            if (updateResponse.getStatus() == 0){
                solrTemplate.commit();
            }else{
                solrTemplate.rollback();
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 全文搜索的方法 */
    public Map<String,Object> search(Map<String, Object> params){
        try{
            Map<String,Object> data = new HashMap<>();


            // 获取查询关键字
            String keywords = (String)params.get("keywords");

            // 获取当前页码
            Integer page = (Integer) params.get("page");
            if (page == null && page <= 1){
                page = 1;
            }
            // 设置默认的页大小
            Integer rows = 10;

            // 判断查询关键字是否为空
            if (StringUtils.isNoneBlank(keywords)){ // 高亮查询
                // 创建高亮查询对象
                HighlightQuery highlightQuery = new SimpleHighlightQuery();
                // 创建条件对象
                Criteria criteria = new Criteria("keywords").is(keywords);
                // 添加条件对象
                highlightQuery.addCriteria(criteria);

                // 创建高亮选项对象
                HighlightOptions highlightOptions = new HighlightOptions();
                // 设置高亮的域Field
                highlightOptions.addField("title");
                // 设置高亮格式器前缀
                highlightOptions.setSimplePrefix("<font color='red'>");
                // 设置高亮格式器后缀
                highlightOptions.setSimplePostfix("</font>");

                // 添加高亮选项对象
                highlightQuery.setHighlightOptions(highlightOptions);


                // {"keywords":"","category":"手机","brand":"苹果",
                // "spec":{"网络":"移动3G","机身内存":"64G"},"price":"2000-3000"}
                /** 1. 按商品分类过滤 */
                // 判断商品分类
                String category = (String)params.get("category");
                if (StringUtils.isNoneBlank(category)){
                    // 定义条件对象
                    Criteria criteria1 = new Criteria("category").is(category);
                    // 创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                    // 添加过滤查询
                    highlightQuery.addFilterQuery(filterQuery);
                }

                /** 2. 按商品品牌过滤 */
                // 判断商品品牌
                String brand = (String)params.get("brand");
                if (StringUtils.isNoneBlank(brand)){
                    // 定义条件对象
                    Criteria criteria1 = new Criteria("brand").is(brand);
                    // 创建过滤查询对象
                    FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                    // 添加过滤查询
                    highlightQuery.addFilterQuery(filterQuery);
                }

                /** 3. 按商品规格过滤(spec_* 动态域) */
                // 判断商品规格 "spec":{"网络":"移动3G","机身内存":"64G"}
                Map<String,String> specMap = (Map<String,String>)params.get("spec");
                if (specMap != null && specMap.size() > 0){
                    // spec_网络
                    for (String key : specMap.keySet()){
                        // 定义条件对象
                        Criteria criteria1 = new Criteria("spec_" + key).is(specMap.get(key));
                        // 创建过滤查询对象
                        FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                        // 添加过滤查询
                        highlightQuery.addFilterQuery(filterQuery);
                    }
                }


                /** 4. 按商品价格区间过滤 */
                // 判断商品价格区间 0-500 1000-1500 3000-*
                String priceStr = (String)params.get("price");
                if (StringUtils.isNoneBlank(priceStr)){
                    // 得到价格的数组
                    String[] priceArr = priceStr.split("-");

                    // 价格的起始不是零
                    if (!"0".equals(priceArr[0])){
                        // 定义条件对象
                        Criteria criteria1 = new Criteria("price").greaterThanEqual(priceArr[0]);
                        // 创建过滤查询对象
                        FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                        // 添加过滤查询
                        highlightQuery.addFilterQuery(filterQuery);
                    }
                    // 价格的结束不是星号
                    if (!"*".equals(priceArr[1])){
                        // 定义条件对象
                        Criteria criteria1 = new Criteria("price").lessThanEqual(priceArr[1]);
                        // 创建过滤查询对象
                        FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                        // 添加过滤查询
                        highlightQuery.addFilterQuery(filterQuery);
                    }
                }


                // 设置分页参数
                // 1. 设置起始记录数
                highlightQuery.setOffset((page - 1) * rows);
                // 2. 设置每页显示的记录数
                highlightQuery.setRows(rows);


                // 排序 sortField : '', sortValue : ''
                String sortField = (String)params.get("sortField");
                String sortValue = (String)params.get("sortValue");
                if (StringUtils.isNoneBlank(sortField) && StringUtils.isNoneBlank(sortValue)){
                    // 创建排序对象
                    Sort sort = new Sort("ASC".equals(sortValue) ? Sort.Direction.ASC
                            : Sort.Direction.DESC, sortField);
                    // 添加排序
                    highlightQuery.addSort(sort);
                }


                // 高亮分页查询，得到高亮分页对象
                // 第一个参数：高亮查询对象
                // 第二个参数：文档需要转化成哪一个实体类
                // highlightPage: 检索的数据、分页数据、高亮内容
                HighlightPage<SolrItem> highlightPage = solrTemplate
                        .queryForHighlightPage(highlightQuery, SolrItem.class);

                // 获取高亮选项集合
                List<HighlightEntry<SolrItem>> highlighted = highlightPage.getHighlighted();
                // 循环高亮选项集合
                for (HighlightEntry<SolrItem> highlightEntry : highlighted){
                    // 获取SolrItem对象(对应一篇文档)
                    SolrItem solrItem = highlightEntry.getEntity();

                    // 判断高亮内容集合是否为空
                    if (highlightEntry.getHighlights() != null
                            && highlightEntry.getHighlights().size() > 0){
                        // 获取标题的高亮内容
                        String title = highlightEntry.getHighlights()
                                .get(0).getSnipplets().get(0);
                        System.out.println(title);
                        // 设置标题为高亮后的内容
                        solrItem.setTitle(title);
                    }
                }

                // 获取分页数据结果
                List<SolrItem> solrItems = highlightPage.getContent();
                data.put("rows", solrItems);
                // 总记录数
                data.put("total", highlightPage.getTotalElements());
                // 总页数
                data.put("totalPages", highlightPage.getTotalPages());


            }else{ // 简单查询

                // 创建简单查询对象
                Query simpleQuery = new SimpleQuery("*:*");
                // 设置分页参数
                // 1. 设置起始记录数
                simpleQuery.setOffset((page - 1) * rows);
                // 2. 设置每页显示的记录数
                simpleQuery.setRows(rows);

                // 分页搜索商品数据，得到分数分页对象
                ScoredPage<SolrItem> scoredPage = solrTemplate.queryForPage(simpleQuery, SolrItem.class);
                System.out.println("总记录数：" + scoredPage.getTotalElements());

                // 获取分页数据结果
                List<SolrItem> solrItems = scoredPage.getContent();
                data.put("rows", solrItems);
                // 总记录数
                data.put("total", scoredPage.getTotalElements());
                // 总页数
                data.put("totalPages", scoredPage.getTotalPages());
            }

            return data;

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
