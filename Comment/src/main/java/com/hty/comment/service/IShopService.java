package com.hty.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.Shop;

/**
 * 服务实现类
 */
public interface IShopService extends IService<Shop> {


    Result queryById(Long id);

    Result update(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
