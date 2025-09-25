package com.hty.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.Follow;
import com.hty.comment.mapper.FollowMapper;
import com.hty.comment.service.IFollowService;
import com.hty.comment.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Service("followService")
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //1. 获取登录用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        //1. 判断到底是关注还是取关
        if (isFollow) {
            //2. 关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess){
                //把关注用户的id，放入redis的set集合
                stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
        }else {
            //3. 取关，删除数据
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                //把关注用户的id从Redis集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
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
