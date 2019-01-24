package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 类型模板服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2018-12-30<p>
 */
@Service(interfaceName = "com.pinyougou.service.TypeTemplateService")
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public void save(TypeTemplate typeTemplate) {
        try{
            typeTemplateMapper.insertSelective(typeTemplate);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        try{
            typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {
        try{
            // 创建示范对象
            Example example = new Example(TypeTemplate.class);
            // 创建条件对象
            Example.Criteria criteria = example.createCriteria();
            // 添加in条件
            criteria.andIn("id", Arrays.asList(ids));
            // 批量删除
            typeTemplateMapper.deleteByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TypeTemplate findOne(Serializable id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<TypeTemplate> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(TypeTemplate typeTemplate, int page, int rows) {
        try{
            // 开始分页
            PageInfo<Object> pageInfo = PageHelper.startPage(page, rows)
                    .doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    typeTemplateMapper.findAll(typeTemplate);
                }
            });
            return new PageResult(pageInfo.getTotal(), pageInfo.getList());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据类型模板id查询规格选项 */
    public List<Map> findSpecByTemplateId(Long id){
        try{
            // 1. 根据id查询类型模板对象
            TypeTemplate typeTemplate = findOne(id);

            // 2. 获取specIds的数据: [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            String specIds = typeTemplate.getSpecIds();

            // 3. 把specIds（json字符串）转化成 List<Map<String,Object>> specLists
            List<Map> specLists = JSON.parseArray(specIds, Map.class);

            // 4. 迭代List集合，取元素Map<String,Object>
            for (Map map : specLists){
                // map: {"id":27,"text":"网络"}
                // 5. 从Map集合中取id的值
                Long specId = Long.valueOf(map.get("id").toString());

                // 6. 根据spec_id从tb_specification_option查询数据
                // SELECT * FROM `tb_specification_option` WHERE spec_id = 27
                SpecificationOption so = new SpecificationOption();
                so.setSpecId(specId);
                List<SpecificationOption> options = specificationOptionMapper.select(so);

                // 7. 把查询到得数据放入map中以options作为key
                map.put("options", options);
            }
            return specLists;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
