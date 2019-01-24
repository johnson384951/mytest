package com.pinyougou.service;

import com.pinyougou.cart.Cart;

import java.util.List; /**
 * 购物车服务接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-17<p>
 */
public interface CartService {
    /**
     * 把SKU商品加入购物车
     * @param carts 购物车
     * @param itemId SKU的id
     * @param num 购买数量
     * @return 修改后的购物车
     */
    List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num);

    /**
     * 把购物车存储到Redis数据库
     * @param userId 用户id
     * @param carts  购物车数据
     */
    void saveCartRedis(String userId, List<Cart> carts);

    /**
     * 从Redis数据库获取用户的购物车
     * @param userId 用户id
     * @return 购物车数据
     */
    List<Cart> findCartRedis(String userId);

    /**
     * 把Cookie中的购物车数据合并到Redis，得到合并后购物车
     * @param cookieCarts Cookie中购物车
     * @param redisCarts Redis中购物车
     * @return 合并后的购物车数据
     */
    List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> redisCarts);
}
