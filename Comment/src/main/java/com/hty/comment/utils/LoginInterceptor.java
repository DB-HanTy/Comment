package com.hty.comment.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hty.comment.dto.UserDTO;
import com.hty.comment.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("LoginInterceptor: checking user status");
        UserDTO user = UserHolder.getUser();
        System.out.println("Current user: " + user);

        //判断是否需要拦截（Thread Local中是否有用户）
        if (user == null) {
            //没有，需要拦截，设置状态码
            System.out.println("No user found, intercepting request");
            response.setStatus(401);
            // 拦截
            return false;
        }
        //有用户，则放行
        System.out.println("User found, allowing request");
        return true;
    }

}
