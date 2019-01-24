package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 购物车控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-17<p>
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    /** 注入请求对象 */
    @Autowired
    private HttpServletRequest request;
    /** 注入响应对象 */
    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 10000)
    private CartService cartService;

    /** 把SKU商品加入购物车 */
    @GetMapping("/addCart")
    @CrossOrigin(origins = {"http://item.pinyougou.com"},
            allowCredentials = "true")
    public boolean addCart(Long itemId, Integer num){
        try {
            // 设置响应头(设置允许访问的域名) 99%
            //response.setHeader("Access-Control-Allow-Origin", "http://item.pinyougou.com");
            // 设置响应头(设置允许访问我们的Cookie)
            //response.setHeader("Access-Control-Allow-Credentials", "true");

            // 1. 获取到原来的购物车
            List<Cart> carts = findCart();

            // 2. 调用购物车服务接口把商品加入购物车
            carts = cartService.addItemToCart(carts, itemId, num);

            // 获取登录用户名
            String userId = request.getRemoteUser();
            if (StringUtils.isNoneBlank(userId)){ // 已登录
                /** ########## 已登录的用户，把购物车数据存储到Redis数据库 ########## */
                cartService.saveCartRedis(userId, carts);

            }else{ // 未登录
                /** ########## 未登录的用户，把购物车数据存储到Cookie中 ########## */

                // 3. 把修改后的购物车存储到Cookie中(按秒)
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(carts),
                        60 * 60 * 24, true);
            }

            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 查询购物车 */
    @GetMapping("/findCart")
    public List<Cart> findCart(){
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 定义用户的购物车集合
        List<Cart> carts = null;

        // 判断用户名
        if (StringUtils.isNoneBlank(userId)){ // 已登录
            /** ######### 已登录的用户，从Redis数据库中获取用户的购物车数据 ##########*/
            carts = cartService.findCartRedis(userId);

            /** #########  购物车合并(把Cookie中的购物车数据合并到Redis数据库)  ######### */
            // 1. 先从Cookie中获取该用户的购物车数据
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            // 2. 判断购物数据是否为空
            if (StringUtils.isNoneBlank(cartJsonStr)){
                // 把json字符串转化成List<Cart>集合
                List<Cart> cookieCarts = JSON.parseArray(cartJsonStr, Cart.class);
                if (cookieCarts != null && cookieCarts.size() > 0){
                    // 3. 把Cookie中的购物车数据合并到Redis，得到合并后购物车
                    carts = cartService.mergeCart(cookieCarts, carts);

                    // 4. 把合并后的购物车存储到Redis数据库
                    cartService.saveCartRedis(userId, carts);

                    // 5. 删除Cookie中的购物车数据
                    CookieUtils.deleteCookie(request,response,
                            CookieUtils.CookieName.PINYOUGOU_CART);
                }
            }


        }else{ // 未登录
            /** ######### 未登录的用户，从Cookie中获取用户的购物车数据 ##########*/
            // cartJsonStr: List<Cart> 的json格式字符串 [{},{}]
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            if (StringUtils.isBlank(cartJsonStr)){
                // 创建新的购物车
                cartJsonStr = "[]";
            }
            // 把购物车json字符串转化成List<Cart>
            carts = JSON.parseArray(cartJsonStr, Cart.class);
        }
        return carts;
    }
}