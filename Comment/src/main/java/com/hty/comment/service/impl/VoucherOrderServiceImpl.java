package com.hty.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.config.RedissionConfig;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.SeckillVoucher;
import com.hty.comment.entity.VoucherOrder;
import com.hty.comment.mapper.VoucherOrderMapper;
import com.hty.comment.service.ISeckillVoucherService;
import com.hty.comment.service.IVoucherOrderService;
import com.hty.comment.utils.RedisIdWorker;
import com.hty.comment.utils.SimpleRedisLock;
import com.hty.comment.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service("voucherOrderService") // 指定明确的Bean名称
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;//释放锁的脚本
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        //获取用户id
//        Long userId = UserHolder.getUser().getId();
//
//        //1. 执行lua脚本
//        Long result = stringRedisTemplate.execute(
//                SECKILL_SCRIPT,
//                Collections.emptyList(),
//                voucherId.toString(), userId.toString()
//        );
//        //2. 判断结果是否为0
//        int r = result.intValue();
//        if (r != 0) {
//            //2.1 不为0，代表没有购买资格
//            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
//        }
//        //2.2 为0，有购买资格，把下单信息保存到阻塞队列
//        long orderId = redisIdWorker.nextId("order");
//        //3. 返回订单id
//        return Result.ok();
//    }
@Override
public Result seckillVoucher(Long voucherId) {
    //1、查询优惠券
    SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
    //2、判断秒杀是否开始
    if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
        //尚未开始
        return Result.fail("秒杀尚未开始");
    }
    //3、判断秒杀是否结束
    if (voucher.getEndTime().isBefore(LocalDateTime.now())){
        return Result.fail("秒杀已结束");
    }
    //4、判断库存是否充足
    if (voucher.getStock() < 1){
        //库存不足
        return Result.fail("库存不足");
    }

    //确保提交完订单后再释放锁
    Long userId = UserHolder.getUser().getId();
    //创建锁对象
    RLock lock = redissonClient.getLock("lock:order:" + userId);
    //获取锁
    boolean isLock = false;
    try {
        isLock = lock.tryLock(10, TimeUnit.SECONDS);
        //判断是否获取锁成功
        if (!isLock) {
            //获取锁失败，返回错误或重试
            return Result.fail("不允许重复下单");
        }

        //获取代理对象,以使事务生效
        IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
        return proxy.createVouvherOrder(voucherId);
    } catch (InterruptedException e) {
        log.error("获取锁失败", e);
        return Result.fail("获取锁失败");
    } finally {
        //只有成功获取锁的线程才能释放锁
        if (isLock && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}


    @Transactional
    public Result createVouvherOrder(Long voucherId) {
        //5.一人一单
        //获取用户id
        Long userId = UserHolder.getUser().getId();
            //5.1 查询订单
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            //5.2 查询是否存在
            if (count > 0) {
                //用户已经购买过了
                return Result.fail("用户已经购买过一次");
            }

            //6、扣减库存
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")//set stock = stock - 1
                    .eq("voucher_id", voucherId)
                    .gt("stock", 0)//where id = ? and stock = >0
                    .update();
            if (!success) {
                //扣减失败
                return Result.fail("库存不足");
            }
            //7、创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            //7.1 订单id
            long orderId = redisIdWorker.nextId("order");
            voucherOrder.setId(orderId);
            //7.2 用户id
            voucherOrder.setUserId(userId);
            //7.3 代金券id
            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            //8、返回订单id
            return Result.ok(orderId);

    }
}
