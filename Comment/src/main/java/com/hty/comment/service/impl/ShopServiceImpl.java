package com.hty.comment.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.Shop;
import com.hty.comment.mapper.ShopMapper;
import com.hty.comment.service.IShopService;
import com.hty.comment.utils.CacheClient;
import com.hty.comment.utils.SystemConstants;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.hty.comment.utils.RedisConstants.*;


@Service("shopService")
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;


    @Override
    public Result queryById(Long id) {
        //缓存穿透
        Shop shop = cacheClient
                .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        //互斥锁解决缓存击穿
       // Shop = queryWithMutex(id);

        //逻辑过期解决缓存击穿
//        Shop shop = cacheClient
//                .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        //8、返回
        return Result.ok(shop);
    }

    //缓存击穿
//    public Shop queryWithMutex(Long id){
//        String key = CACHE_SHOP_KEY +  id;
//        //1、从Redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        //2、判断是否存在
//        if (StrUtil.isNotBlank(shopJson)){
//            //3、存在，直接返回
//            return JSONUtil.toBean(shopJson,Shop.class);
//        }
//        //判断命中的是否是空值
//        if (shopJson != null){
//            //返回错误信息
//            return null;
//        }
//        //4.实现缓存重建
//        //4.1 获取互斥锁
//        String lockKey = "lock:shop:" + id;
//        Shop shop = null;
//        try {
//            boolean isLock = tryLock(lockKey);
//            //4.2 判断是否获取成功
//            if (!isLock){
//                //4.3 失败，则休眠并重试
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//            //4.4 成功，根据id查询数据库
//            shop = getById(id);
//            //5、判断商铺是否存在
//            if (shop == null){
//                //将空值写入Redis
//                stringRedisTemplate.opsForValue().set(key, "", CACHE_SHOP_TTL, TimeUnit.MINUTES);
//                //返回错误信息
//                return null;
//            }
//            //6、写入Redis
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }finally {
//            //7、释放互斥锁
//            unlock(lockKey);
//        }
//        //8、返回
//        return shop;
//    }

    //缓存穿透
//    public Shop queryWithPassThrough(Long id){
//            String key = CACHE_SHOP_KEY +  id;
//            //1、从Redis查询商铺缓存
//            String shopJson = stringRedisTemplate.opsForValue().get(key + id);
//            //2、判断是否存在
//            if (StrUtil.isNotBlank(shopJson)){
//                //3、存在直接返回
//                return JSONUtil.toBean(shopJson,Shop.class);
//            }
//            //判断命中的是否是空值
//            if (shopJson != null){
//                //返回错误信息
//                return null;
//            }
//            //4、不存在，根据id查询数据库
//            Shop shop = getById(id);
//            //5、判断商铺是否存在
//            if (shop == null) {
//                //6、不存在，返回错误
//
//                //将空值写入Redis
//                stringRedisTemplate.opsForValue().set(key, "", CACHE_SHOP_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//            //7、存在写入Redis
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//            //8、返回
//            return shop;
//
//    }

    //尝试获取锁
//    private  boolean tryLock(String key){
//        Boolean falg = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(falg);
//    }
//    //释放锁
//    private void unlock(String key){
//        stringRedisTemplate.delete(key);
//    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        //1、更新数据库
        updateById(shop);
        //2、删除Redis缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        //1. 判断是否需要根据坐标查询
        if (x == null || y == null){
            //不需要坐标查询，按数据库分页查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            //返回数据
            return Result.ok(page.getRecords());
        }
        //2. 计算分页参数
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        //3. 查询redis、按照距离排序、分页。结果：shopId、distance
        String key = SHOP_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
        //4. 解析出id
        if (results == null){
            return Result.ok();
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from){
            //没有下一页了，结束
            return Result.ok(Collections.emptyList());
        }
        //4.1 截取from ~ end的部分
         List<Long> ids = new ArrayList<>(list.size());
        Map<String,Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            //获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            //获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });
        //5. 根据id查询shop
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Shop shop : shops){
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        //6. 返回
        return Result.ok(shops);
    }
}
