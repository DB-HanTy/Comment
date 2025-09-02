package com.hty.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.entity.ShopType;
import com.hty.comment.mapper.ShopTypeMapper;
import com.hty.comment.service.IShopTypeService;
import org.springframework.stereotype.Service;


@Service("shopTypeService")
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

}
