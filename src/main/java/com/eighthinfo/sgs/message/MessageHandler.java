package com.eighthinfo.sgs.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: 下午4:15
 * To change this template use File | Settings | File Templates.
 */
public class MessageHandler extends CommonMessage {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MessageHandler.class);

    public CommonMessage enterRoom(String args){
        //LOGGER.info("in server side MessageHandler.enterRoom "+args);
        String roomInfo =
        "{\"roomId\": \"r1\",\"seatNo\":3,\"players\":[{\"nickName\":\"u1\",\"seatNo\":1},{\"nickName\":\"u2\",\"seatNo\":2}]}\n";
        CommonMessage messageResponse = new CommonMessage();
        messageResponse.setCallMethod("aaa");
        messageResponse.setCallMethodParameters(roomInfo);
        return messageResponse;
    }
}
