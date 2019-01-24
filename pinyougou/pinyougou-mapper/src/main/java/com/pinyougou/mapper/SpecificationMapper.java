package com.pinyougou.mapper;

import com.pinyougou.pojo.Specification;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * SpecificationMapper 数据访问接口
 * @date 2018-12-27 15:34:57
 * @version 1.0
 */
public interface SpecificationMapper extends Mapper<Specification>{

    /** 多条件查询规格 */
    List<Specification> findAll(Specification specification);

    /**  查询所有的规格(id与name) */
    @Select("select id, spec_name as text from tb_specification ORDER by id asc")
    List<Map<String,Object>> findAllByIdAndName();
}