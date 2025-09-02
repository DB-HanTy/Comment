package com.hty.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.entity.Shop;
import com.hty.comment.mapper.ShopMapper;
import com.hty.comment.service.IShopService;
import org.springframework.stereotype.Service;


@Service("shopService")
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {



}
