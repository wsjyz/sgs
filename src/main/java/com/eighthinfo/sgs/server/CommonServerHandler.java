package com.eighthinfo.sgs.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.HallService;
import com.eighthinfo.sgs.service.PlayerService;
import com.eighthinfo.sgs.service.impl.redis.HallServiceImpl;
import com.eighthinfo.sgs.utils.ClassUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * User: dam
 * Date: 13-11-25
 */
public class CommonServerHandler extends IoHandlerAdapter{

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommonServerHandler.class);

    static ApplicationContext ac = new ClassPathXmlApplicationContext("spring-redis.xml","spring-config.xml");

    String minaHost = "";
    int minaPort = 0;

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);

        CommonMessage messageRequest = (CommonMessage)message;
        String handlerName = messageRequest.getCallMethod();
        String clazzName = handlerName.substring(0,handlerName.indexOf("."));
        String methodName = handlerName.substring(handlerName.indexOf(".") + 1,handlerName.length());

        StopWatch clock = new StopWatch();
        clock.start();
        JSONObject nickNameJson = JSON.parseObject(messageRequest.getCallMethodParameters().toString());
        clock.stop();
        LOGGER.info("parse json take "+clock.getTime()+" ms");
        String playerId = (String)nickNameJson.get("playerId");
        session.setAttribute("playerId",playerId);
        BroadcastHandler.addSession(playerId,session);
        clock.reset();
        clock.start();
        CommonMessage result = (CommonMessage) ClassUtils.invokeMethod(ac.getBean(clazzName),
                methodName, new Class<?>[]{String.class}, new String[]{messageRequest.getCallMethodParameters().toString()});
//            Invokers.Invoker invoker =
//                    Invokers.newInvoker(ac.getBean(clazzName).getClass().getMethod(methodName,String.class));
//            CommonMessage result =(CommonMessage)invoker.invoke(ac.getBean(clazzName),new String[]{messageRequest.getCallMethodParameters()});

        clock.stop();
        LOGGER.info("invoke method " + methodName + " take " + clock.getTime() + " ms");
        if(result != null){
            if(session.isConnected()){
                session.write(result);
            }
        }

    }

    //不要在 sessionCreated 方法中执行过多的操作 ,
    // 因为sessionCreated方法是由 I/O 处理线程来调用的，而 sessionOpened 是由其它线程来调用的
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        HallService hallService = (HallService)ac.getBean("hallService");
        hallService.increaseHallUserCount(minaHost,minaPort,true);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        LOGGER.warn("Unexpected error.", cause.toString());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        PlayerService playerService = (PlayerService)ac.getBean("playerService");
        String playerId = (String)session.getAttribute("playerId");

        if(org.apache.commons.lang3.StringUtils.isNotBlank(playerId)){
            playerService.leftRoom("{\"playerId\":\""+playerId+"\"}");
        }
        HallService hallService = (HallService)ac.getBean("hallService");
        hallService.increaseHallUserCount(minaHost,minaPort,false);
    }

    /**
     * 向缓存中注册当前HALL（sgs服务实例）
     */
    private void registHall(){
        readMinaPro();
        HallService hallService = (HallService)ac.getBean("hallService");
        hallService.registHall(minaHost,minaPort);
    }

    /**
     * 注销服务
     */
    private void cancelHall(){
        HallService hallService = (HallService)ac.getBean("hallService");
        hallService.cancelHall(minaHost,minaPort);
    }

    private void readMinaPro(){
        //获取实例地址和端口
        String minaConfFilePath = System.getProperty("APP_HOME")+ File.separator+"mina.properties";
        Resource resource = new FileSystemResource(minaConfFilePath);
        Properties props = null;
        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        minaPort = Integer.parseInt(props.getProperty("mina.port"));
        minaHost = props.getProperty("mina.host");
    }
}
