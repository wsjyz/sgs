package com.eighthinfo.sgs.service.impl.redis;

import com.eighthinfo.sgs.service.HallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by dam on 13-12-11.
 */
public class HallServiceImpl implements HallService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HallServiceImpl.class);

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void registHall(String host, int port) {
        //放入缓存
        boolean result = redisTemplate.boundZSetOps("hallSet").add(host+":"+port,0);

        LOGGER.info("向缓存中注册SGS服务 "+host+":"+port);
    }

    @Override
    public void cancelHall(String host, int port) {
        redisTemplate.boundZSetOps("hallSet").remove(host+":"+port);
        LOGGER.info("在缓存中注销SGS服务 "+host+":"+port);
    }

    @Override
    public void increaseHallUserCount(String host, int port,boolean isIncrease) {
        int step = 1;
        if(!isIncrease){
            step = -1;
        }
        double hallUserCount = redisTemplate.boundZSetOps("hallSet").incrementScore(host+":"+port,step);
        LOGGER.info("当前人数为"+hallUserCount);
    }
}
