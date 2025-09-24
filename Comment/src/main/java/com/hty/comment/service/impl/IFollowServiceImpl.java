package com.hty.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.Follow;
import com.hty.comment.mapper.FollowMapper;
import com.hty.comment.service.IFollowService;
import com.hty.comment.utils.UserHolder;

public class IFollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //1. 获取登录用户
        Long userId = UserHolder.getUser().getId();
        //1. 判断到底是关注还是取关
        if (isFollow) {
            //2. 关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);
        }else {
            //3. 取关，删除数据
            remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        //1. 获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2. 查询是否关注
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        //3. 判断
        return Result.ok(count > 0);
    }
}
