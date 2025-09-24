package com.hty.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.Follow;

public interface IFollowService extends IService <Follow> {
    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);
}
