package com.pinyougou.service;

import com.pinyougou.solr.SolrItem;

import java.util.List;
import java.util.Map; /**
 * 商品搜索服务接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-07<p>
 */
public interface ItemSearchService {

    /** 全文搜索的方法 */
    Map<String,Object> search(Map<String, Object> params);

    /** 添加或修改索引 */
    void saveOrUpdate(List<SolrItem> solrItems);

    /** 删除商品的索引 */
    void delete(Long[] goodsIds);
}
