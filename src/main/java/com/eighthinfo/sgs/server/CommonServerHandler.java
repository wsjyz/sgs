package com.eighthinfo.sgs.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.PlayerService;
import com.eighthinfo.sgs.utils.ClassUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * User: dam
 * Date: 13-11-25
 */
public class CommonServerHandler extends IoHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommonServerHandler.class);
    static ApplicationContext ac = new ClassPathXmlApplicationContext("spring-redis.xml","spring-config.xml");


    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        CommonMessage messageRequest = (CommonMessage)message;
        String handlerName = messageRequest.getCallMethod();
        String clazzName = handlerName.substring(0,handlerName.indexOf("."));
        String methodName = handlerName.substring(handlerName.indexOf(".") + 1,handlerName.length());
        StopWatch clock = new StopWatch();
        clock.start();
        JSONObject nickNameJson = JSON.parseObject(messageRequest.getCallMethodParameters().toString());
        clock.stop();
        LOGGER.info("parse json take "+clock.getTime()+" ms");
        session.setAttribute("userId",nickNameJson.get("userId"));
        clock.reset();
        clock.start();
        CommonMessage result = (CommonMessage) ClassUtils.invokeMethod(ac.getBean(clazzName),
                methodName, new Class<?>[]{String.class}, new String[]{messageRequest.getCallMethodParameters().toString()});
//            Invokers.Invoker invoker =
//                    Invokers.newInvoker(ac.getBean(clazzName).getClass().getMethod(methodName,String.class));
//            CommonMessage result =(CommonMessage)invoker.invoke(ac.getBean(clazzName),new String[]{messageRequest.getCallMethodParameters()});

        clock.stop();
        LOGGER.info("invoke method " + methodName + " take " + clock.getTime() + " ms");
        super.messageReceived(session, message);
        if(result != null){
            session.write(result);
        }

    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        BroadcastHandler.addSession(session);
        super.sessionOpened(session);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        LOGGER.warn("Unexpected error.", cause.toString());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {

        PlayerService playerService = (PlayerService)ac.getBean("playerService");

        playerService.leftRoom("{\"userId\":\""+session.getAttribute("userId").toString()+"\"}");

        super.sessionClosed(session);
    }
}
