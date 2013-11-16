package com.eighthinfo.sgs.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: 下午4:15
 * To change this template use File | Settings | File Templates.
 */
public class MessageHandler extends MessageRequest {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MessageHandler.class);

    public MessageResponse enterRoom(String args){
        //LOGGER.info("in server side MessageHandler.enterRoom "+args);
        String roomInfo =
        "{\"roomId\": \"r1\",\"seatNo\":3,\"players\":[{\"nickName\":\"u1\",\"seatNo\":1},{\"nickName\":\"u2\",\"seatNo\":2}]}\n";
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setClientMethod("hello");
        messageResponse.setClientMethodParameters(roomInfo);
        return messageResponse;
    }
}
