package com.hty.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hty.comment.dto.LoginFormDTO;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {



    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sedCode(String phone, HttpSession session);

    Result sign();

    Result signCount();
}
