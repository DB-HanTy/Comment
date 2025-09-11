package com.hty.comment.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.ShopType;
import com.hty.comment.mapper.ShopTypeMapper;
import com.hty.comment.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

import static com.hty.comment.utils.RedisConstants.CACHE_SHOP_TYPE_LIST_KEY;


@Service("shopTypeService")
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getTypeList() {
        String key = CACHE_SHOP_TYPE_LIST_KEY;
        // 1. 从Redis查询商铺类型列表缓存
        String shopTypeListJson = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(shopTypeListJson)) {
            // 2.1 存在直接返回
            List<ShopType> shopType =  JSONUtil.toList(shopTypeListJson, ShopType.class);
            return  Result.ok(shopType);
        }
        // 2.2 不存在，查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList == null){
            return  Result.fail("店铺种类不存在");
        }
        // 3. 写入Redis缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(typeList));
        // 4. 返回
        return Result.ok(typeList);
    }
}
