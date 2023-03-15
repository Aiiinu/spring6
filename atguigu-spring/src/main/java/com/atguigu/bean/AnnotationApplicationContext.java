package com.atguigu.bean;

import com.atguigu.annotation.Bean;
import com.atguigu.annotation.Di;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author RoselleShaw
 * @create 2023-03-12 20:28
 */
public class AnnotationApplicationContext implements ApplicationContext {

    //创建map集合，放bean对象
    Map<Class, Object> beanFactory = new HashMap<>();
    private String rootPath;

    @Override
    public Object getBean(Class clazz) {
        return beanFactory.get(clazz);
    }

    //创建有参数构造，传递包路径，设置包扫描规则
    //当前包及其子包，哪个类有@Bean注解，把这个类通过反射实例化，保存到容器
    public AnnotationApplicationContext(String basePackage) {
        try {
            //com.atguigu
            //1 把.换成\
            String packagePath = basePackage.replaceAll("\\.", "\\\\");

            //2 获取包绝对路径
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String filePath = URLDecoder.decode(url.getFile(), "utf-8");

                //获取包前面路径部分，字符串截取
                rootPath = filePath.substring(0, filePath.length() - packagePath.length());
                //包扫描
                loadBean(new File(filePath));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //属性注入
        try {
            loadDi();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //包扫描过程，实例化
    private void loadBean(File file) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //1 判断当前内容是否文件夹
        if (file.isDirectory()) {
            //2 获取文件夹里面所有内容
            File[] childrenFiles = file.listFiles();

            //3 判断文件夹里面为空，直接返回
            if (childrenFiles == null || childrenFiles.length == 0) {
                return;
            }

            //4 如果文件夹里面不为空，遍历文件夹所有内容
            for (File childrenFile : childrenFiles) {
                //4.1 遍历得到每个File对象，继续判断，如果还是文件夹，递归
                if (childrenFile.isDirectory()) {
                    //递归
                    loadBean(childrenFile);
                }

                //4.2 遍历得到File对象不是文件夹，是文件，
                //4.3 得到包路径+类路径名称-字符串截取
                String pathWithClass = childrenFile.getAbsolutePath().substring(rootPath.length() - 1);

                //4.4 判断当前文件类型是否.class
                if (pathWithClass.contains(".class")) {
                    //4.5 如果是.class类型，把路径\替换成. 把.class去掉
                    // com.atguigu.service.UserServiceImpl
                    String allName = pathWithClass.replaceAll("\\\\", ".").replace(".class", "");

                    //4.6 判断类上面是否有@Bean，如果有实例化
                    //4.6.1 获取类的Class
                    Class<?> clazz = Class.forName(allName);
                    //4.6.2 判断是不是接口
                    if (!clazz.isInterface()) {
                        //4.6.3 判断类上面是否有注解@Bean
                        Bean bean = clazz.getAnnotation(Bean.class);
                        if (bean != null) {
                            //4.6.4 实例化
                            Object instance = clazz.getConstructor().newInstance();

                            //4.7 将对象放到map集合beanFactory
                            //4.7.1 判断当前类如果有接口，让接口class作为map的key
                            if (clazz.getInterfaces().length != 0) {
                                beanFactory.put(clazz.getInterfaces()[0], instance);
                            } else {
                                beanFactory.put(clazz, instance);
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadDi() throws IllegalAccessException {
        //实例化对象在beanFactory的map集合里面
        //1 遍历beanFactory的map集合
        for (Map.Entry<Class, Object> entry : beanFactory.entrySet()) {
            //2 获取map集合对象（value），并获取每个对象的属性
            Object value = entry.getValue();
            Field[] fields = value.getClass().getDeclaredFields();

            //3 遍历属性数组，得到属性
            for (Field field : fields) {
                //4 判断属性上是否有@Di注解
                Di di = field.getAnnotation(Di.class);

                //5 如果有@Di注解，在beanFactory中找对应的对象给属性赋值
                if (di != null) {
                    //如果私有属性，设置可以访问
                    if (Modifier.isPrivate(field.getModifiers())) {
                        field.setAccessible(true);
                    }
                    field.set(value, beanFactory.get(field.getType()));
                }
            }
        }
    }
}
