package com.atguigu.service.impl;

import com.atguigu.annotation.Bean;
import com.atguigu.annotation.Di;
import com.atguigu.dao.UserDAO;
import com.atguigu.service.UserService;

/**
 * @author RoselleShaw
 * @create 2023-03-12 20:22
 */
@Bean
public class UserServiceImpl implements UserService {

    @Di
    private UserDAO userDAO;

    @Override
    public void add() {
        System.out.println("service......");
        //调用DAO的方法
        userDAO.add();
    }
}
