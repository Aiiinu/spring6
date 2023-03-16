package com.atguigu;

import com.atguigu.bean.AnnotationApplicationContext;
import com.atguigu.bean.ApplicationContext;
import com.atguigu.dao.UserDAO;
import com.atguigu.service.UserService;

/**
 * @author RoselleShaw
 * @create 2023-03-12 21:25
 */
public class UserTest {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationApplicationContext("com.atguigu");
        UserService userService = (UserService) context.getBean(UserService.class);
        System.out.println(userService);
        userService.add();
        System.out.println("hello git!");
    }

}
