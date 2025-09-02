package com.hty.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.Voucher;

public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
