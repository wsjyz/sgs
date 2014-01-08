package com.eighthinfo.sgs.service.impl.redis;

import com.eighthinfo.sgs.service.HallService;
import com.eighthinfo.sgs.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Properties;

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

        LOGGER.info("regist sgs "+host+":"+port+" successful!");
    }

    @Override
    public void cancelHall(String host, int port) {
        redisTemplate.boundZSetOps("hallSet").remove(host+":"+port);
        LOGGER.info("cancel sgs "+host+":"+port);
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
    public void init(){
        Properties properties = StringUtils.readProperties("mina.properties");
        int minaPort = Integer.parseInt(properties.getProperty("mina.port"));
        String minaHost = properties.getProperty("mina.host");
        registHall(minaHost,minaPort);
    }
}
