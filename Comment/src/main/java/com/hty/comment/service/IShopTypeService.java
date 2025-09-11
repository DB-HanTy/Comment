package com.hty.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.ShopType;

import java.util.List;


public interface IShopTypeService extends IService<ShopType> {

    Result getTypeList();
}
