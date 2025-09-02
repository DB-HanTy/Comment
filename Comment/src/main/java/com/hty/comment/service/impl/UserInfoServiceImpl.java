package com.hty.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.entity.UserInfo;
import com.hty.comment.mapper.UserInfoMapper;
import com.hty.comment.service.IUserInfoService;
import org.springframework.stereotype.Service;

@Service("userInfoService")
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
