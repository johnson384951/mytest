package com.pinyougou.cart.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-17<p>
 */
@Service(interfaceName = "com.pinyougou.service.CartService")
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 把SKU商品加入购物车
     * @param carts 购物车
     * @param itemId SKU的id
     * @param num 购买数量
     * @return 修改后的购物车
     */
    public List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num){
        try{
            // 根据itemId从tb_item表查询SKU商品对象
            Item item = itemMapper.selectByPrimaryKey(itemId);
            // 获取商家的id
            String sellerId = item.getSellerId();

            // 根据sellerId 到用户的购物车集合中查询 商家的购物车
            Cart cart = searchCartBySellerId(carts, sellerId);

            // 判断商家的购物车
            if (cart == null){ // 代表该用户没有买过该商家的商品
                // 创建商家的购物车
                cart = new Cart();
                // 设置商家的id
                cart.setSellerId(sellerId);
                // 设置商家的名称
                cart.setSellerName(item.getSeller());


                // 创建该商家购物车集合(商家的商品集合)
                List<OrderItem> orderItems = new ArrayList<>();
                // 创建购买的商品
                OrderItem orderItem = createOrderItem(item, num);
                // 添加购买的商品
                orderItems.add(orderItem);

                // 设置购物车集合
                cart.setOrderItems(orderItems);
                // 用户的购物车集合添加商家的购物车
                carts.add(cart);
            }else{ // 代表该用户购买过该商家的商品
                // 根据SKU商品的id从该商家的购物车集合中查询对应的商品
                OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItems(), itemId);
                // 判断该用户是否购买过该商家的同样的商品
                if (orderItem == null){  // 没有买过同样的商品
                    // 创建购买的商品
                    orderItem = createOrderItem(item, num);
                    // 添加到商家的购物车集合中
                    cart.getOrderItems().add(orderItem);

                }else{ // 买过同样的商品
                    // 购买数量相加
                    orderItem.setNum(orderItem.getNum() + num);
                    // 购买金额(小计)
                    orderItem.setTotalFee(new BigDecimal(orderItem.getPrice()
                            .doubleValue() * orderItem.getNum()));

                    // 判断商品的购买数量
                    if (orderItem.getNum() < 1){
                        // 从商家的购物车集合中删除该商品
                        cart.getOrderItems().remove(orderItem);
                    }
                    // 判断商家的购物车集合大小
                    if (cart.getOrderItems().size() == 0){
                        // 从用户的购物车集合删除该商家的购物车
                        carts.remove(cart);
                    }
                }
            }
            return carts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据SKU商品的id从商家的购物车集合中查询对应的商品 */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItems, Long itemId) {
        // 迭代商家的购物车集合
        for (OrderItem orderItem : orderItems){
            if (orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    /** 创建购买的商品 */
    private OrderItem createOrderItem(Item item, Integer num) {
        OrderItem orderItem = new OrderItem();
        // 设置SKU的id
        orderItem.setItemId(item.getId());
        // 设置SPU的id
        orderItem.setGoodsId(item.getGoodsId());
        // 设置SKU的标题
        orderItem.setTitle(item.getTitle());
        // 设置SKU商品的价格
        orderItem.setPrice(item.getPrice());
        // 设置购买数量
        orderItem.setNum(num);
        // 设置购买的金额(小计)
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * num));
        // 设置商品的图片
        orderItem.setPicPath(item.getImage());
        // 设置商家的id
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    /** 根据sellerId 到用户的购物车集合中查询 商家的购物车 */
    private Cart searchCartBySellerId(List<Cart> carts, String sellerId) {
        // 迭代用户的购物车集合
        for (Cart cart : carts){
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }


    /**
     * 把购物车存储到Redis数据库
     * @param userId 用户id
     * @param carts  购物车数据
     */
    public void saveCartRedis(String userId, List<Cart> carts){
        try{
            redisTemplate.boundValueOps("cart_" + userId).set(carts);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 从Redis数据库获取用户的购物车
     * @param userId 用户id
     * @return 购物车数据
     */
    public List<Cart> findCartRedis(String userId){
        try{
            List<Cart> carts = (List<Cart>)redisTemplate
                    .boundValueOps("cart_" + userId).get();
            if (carts == null){
                // 创建新的购物车
                carts = new ArrayList<>();
            }
            return carts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 把Cookie中的购物车数据合并到Redis，得到合并后购物车
     * @param cookieCarts Cookie中购物车
     * @param redisCarts Redis中购物车
     * @return 合并后的购物车数据
     */
    public List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> redisCarts){
        try{
            // 迭代Cookie中的购物车集合
            for (Cart cookieCart : cookieCarts){
                // cookieCart:一个商家
                // 迭代该商家购物车中的商品
                for (OrderItem orderItem : cookieCart.getOrderItems()){
                    // 把orderItem添加到redisCarts购物车中
                    redisCarts = addItemToCart(redisCarts, orderItem.getItemId(), orderItem.getNum());
                }
            }
            return redisCarts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
