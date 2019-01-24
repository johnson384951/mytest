package com.pinyougou.cart;

import com.pinyougou.pojo.OrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车实体(一个购物车实体对象 对应一个商家)
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-17<p>
 */
public class Cart implements Serializable {
    /** 商家ID */
    private String sellerId;
    /** 商家名称 */
    private String sellerName;
    /** 购物车明细集合 Item */
    private List<OrderItem> orderItems;

    /** setter and getter method */
    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}