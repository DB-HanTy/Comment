package com.hty.comment;

import com.hty.comment.entity.Shop;
import com.hty.comment.service.IShopService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private IShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void loadShopData(){
        //1.��ѯ������Ϣ
        List<Shop> list = shopService.list();
        //2. �ѵ��̷��飬����typeId���飬typeIdһ�µķŵ�һ������
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop ::getTypeId));
        //3. �������д��Redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()){
            //3.1 ��ȡ����id
            Long typeId = entry.getKey();
            String key = "shop:geo:" + typeId;
            //3.2 ��ȡ��ͬ���͵ĵ����б�
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            //3.3 д��redis GEOADD key γ�� ���� member
            for (Shop shop : value){
//                stringRedisTemplate.opsForGeo().add(key,new Point(shop.getX(),shop.getY()),shop.getId().toString());
           locations.add(new RedisGeoCommands.GeoLocation<>(
                   shop.getId().toString(),
                   new Point(shop.getX(),shop.getY())
           ));
            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }
}
