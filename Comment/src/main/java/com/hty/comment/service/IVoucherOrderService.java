package com.hty.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.VoucherOrder;

public interface IVoucherOrderService extends IService<VoucherOrder> {
    Result seckillVoucher(Long voucherId);

    Result createVouvherOrder(Long voucherId);
}
