package com.pinyougou.mapper;

import com.pinyougou.pojo.Goods;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * GoodsMapper 数据访问接口
 * @date 2018-12-27 15:34:57
 * @version 1.0
 */
public interface GoodsMapper extends Mapper<Goods>{

    /** 多条件查询商品 */
    List<Map<String,Object>> findAll(Goods goods);

    /** 审核商品(修改商品的状态) */
    void updateStatus(@Param("cloumnName") String cloumnName,
                      @Param("ids") Long[] ids,
                      @Param("status")String status);
}