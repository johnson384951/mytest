package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.*;

/**
 * 商品服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-02<p>
 */
@Service(interfaceName = "com.pinyougou.service.GoodsService")
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SellerMapper sellerMapper;

    @Override
    public void save(Goods goods) {
        try{
            // 往tb_goods表 (SPU 标准商品表)
            // 设置商品审核状态: 未审核
            goods.setAuditStatus("0");
            goodsMapper.insertSelective(goods);

            // 往tb_goods_desc表 (商品描述表)
            goods.getGoodsDesc().setGoodsId(goods.getId());
            goodsDescMapper.insertSelective(goods.getGoodsDesc());

            // 判断用户是否启用了规格
            if ("1".equals(goods.getIsEnableSpec())) { // 启用规格

                // 往tb_item表 (SKU 库存量表)
                for (Item item : goods.getItems()) {
                    // item: {"spec":{"网络":"移动3G","机身内存":"32G"},
                    // "price":"2000","num":"1000","status":0,"isDefault":0}
                    // 商品的标题
                    // Apple iPhone XS Max (A2103) 256GB 金色 全网通（移动4G优先版） 双卡双待
                    // SPU的商品名称 + 规格选项
                    StringBuilder sb = new StringBuilder(goods.getGoodsName());
                    // 获取spec {"网络":"移动3G","机身内存":"32G"}
                    Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
                    for (String value : specMap.values()) {
                        sb.append(" " + value);
                    }
                    item.setTitle(sb.toString());

                    // 设置商品的其它信息
                    setItemInfo(goods, item);

                    // 往tb_item表插入数据
                    itemMapper.insertSelective(item);
                }
            }else{ // 没有启用规格
                // 往tb_item表中插入一条数据 SPU就是SKU
                Item item = new Item();
                //  {"spec":{"网络":"移动3G","机身内存":"32G"},
                // "price":"2000","num":"1000","status":0,"isDefault":0}
                // 设置商品的标题
                item.setTitle(goods.getGoodsName());
                // 规格
                item.setSpec("{}");
                // 价格
                item.setPrice(goods.getPrice());
                // 库存数量
                item.setNum(100);
                // 状态码
                item.setStatus("1");
                // 是否为默认
                item.setIsDefault("1");

                // 设置商品的其它信息
                setItemInfo(goods, item);

                // 往tb_item表插入数据
                itemMapper.insertSelective(item);
            }

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 设置商品的其它信息 */
    private void setItemInfo(Goods goods, Item item) {

        // 获取该商品所有的图片
        // [{"color":"金色","url":"http://image.pinyougou.com/jd/wKgMg1qtKEOATL9nAAFti6upbx4132.jpg"},
        // {"color":"深空灰色","url":"http://image.pinyougou.com/jd/wKgMg1qtKHmAFxj7AAFZsBqChgk725.jpg"},
        // {"color":"银色","url":"http://image.pinyougou.com/jd/wKgMg1qtKJyAHQ9sAAFuOBobu-A759.jpg"}]
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList != null && imageList.size() > 0) {
            // 商品图片
            item.setImage(imageList.get(0).get("url").toString());
        }
        // 商品三级分类的id
        item.setCategoryid(goods.getCategory3Id());
        // 创建时间
        item.setCreateTime(new Date());
        // 修改时间
        item.setUpdateTime(item.getCreateTime());
        // 关联的SPU的id
        item.setGoodsId(goods.getId());
        // 商家的id
        item.setSellerId(goods.getSellerId());

        // 商品三级分类名称(三级分类)
        // 查询三级商品分类对象
        ItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
        item.setCategory(itemCat != null ? itemCat.getName() : "");

        // 品牌名称
        // 查询品牌对象
        Brand brand = brandMapper.selectByPrimaryKey(goods.getBrandId());
        item.setBrand(brand != null ? brand.getName() : "");

        // 店铺名称
        // 查询商家对象
        Seller seller = sellerMapper.selectByPrimaryKey(goods.getSellerId());
        item.setSeller(seller != null ? seller.getNickName() : "");
    }

    @Override
    public void update(Goods goods) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Goods findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Goods> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Goods goods, int page, int rows) {
        try{
            // 开始分页
            PageInfo<Map<String,Object>> pageInfo = PageHelper.startPage(page, rows)
                    .doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    goodsMapper.findAll(goods);
                }
            });
            // 获取分页数据
            List<Map<String,Object>> goodsList = pageInfo.getList();
            for (Map<String, Object> map : goodsList ){
                // 查询一级分类的名称
                ItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(map.get("category1Id"));
                // entity.category1Name}}
                map.put("category1Name", itemCat1 != null ? itemCat1.getName() : "");

                // 查询二级分类的名称
                ItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(map.get("category2Id"));
				// entity.category2Name}}
                map.put("category2Name", itemCat2 != null ? itemCat2.getName() : "");

                // 查询二级分类的名称
                ItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(map.get("category3Id"));
				// entity.category3Name}}
                map.put("category3Name", itemCat3 != null ? itemCat3.getName() : "");
            }

            return new PageResult(pageInfo.getTotal(), goodsList);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 审核商品(修改商品的状态) */
    public void updateStatus(String cloumnName, Long[] ids, String status){
        try{
            goodsMapper.updateStatus(cloumnName, ids, status);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据goodsId查询商品详情信息 */
    public Map<String,Object> getGoods(Long goodsId){
        try{
            Map<String,Object> dataModel = new HashMap<>();

            // 1. 查询SPU表中的数据
            Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);

            // 2. 查询商品描述表中的数据
            GoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);

            // 3. 查询商品的三级分类名称
            if (goods.getCategory3Id() != null){
                // 获取一级分类的名称
                dataModel.put("itemCat1", itemCatMapper.
                        selectByPrimaryKey(goods.getCategory1Id()).getName());
                // 获取二级分类的名称
                dataModel.put("itemCat2", itemCatMapper.
                        selectByPrimaryKey(goods.getCategory2Id()).getName());
                // 获取三级分类的名称
                dataModel.put("itemCat3", itemCatMapper.
                        selectByPrimaryKey(goods.getCategory3Id()).getName());

            }

            // 3. 查询SKU表中的数据
            Example example = new Example(Item.class);
            // 创建条件对象
            Example.Criteria criteria = example.createCriteria();
            // 添加条件 goods_id = ?
            criteria.andEqualTo("goodsId", goodsId);
            // 把默认的SKU排在最新面
            example.orderBy("isDefault").desc();
            // 查询SKU数据
            List<Item> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", JSON.toJSONString(itemList));

            return dataModel;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据多个goodsId到tb_item表查询SKU商品数据 */
    public List<Item> findItemByGoodsId(Long[] goodsIds){
        try{
            // 创建Example对象 select * from tb_item where goods_id in (?,?,?)
            Example example = new Example(Item.class);
            // 创建条件对象
            Example.Criteria criteria = example.createCriteria();
            // in 条件
            criteria.andIn("goodsId", Arrays.asList(goodsIds));
            // 条件查询
            return itemMapper.selectByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
