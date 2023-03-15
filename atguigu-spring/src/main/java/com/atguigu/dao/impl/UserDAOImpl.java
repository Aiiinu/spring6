package com.atguigu.dao.impl;

import com.atguigu.annotation.Bean;
import com.atguigu.dao.UserDAO;

/**
 * @author RoselleShaw
 * @create 2023-03-12 20:22
 */
@Bean
public class UserDAOImpl implements UserDAO {
    @Override
    public void add() {
        System.out.println("dao......");
    }
}
