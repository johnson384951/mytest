package com.pinyougou.shop.service;

import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户认证的服务类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2018-12-31<p>
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("sellerService: " + sellerService);

        // 根据商家的id查询商家
        Seller seller = sellerService.findOne(username);
        // 判断是否为空、审核是否通过
        if (seller != null && "1".equals(seller.getStatus())) {

            // 定义List集合封装角色
            List<GrantedAuthority> authorities = new ArrayList<>();
            // 添加一个角色
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

            return new User(username, seller.getPassword(), authorities);
        }
        return null;
    }

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

}
