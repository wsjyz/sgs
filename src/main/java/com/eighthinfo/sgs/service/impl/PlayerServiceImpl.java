package com.eighthinfo.sgs.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.Constants;
import com.eighthinfo.sgs.dao.PlayerDAO;
import com.eighthinfo.sgs.domain.PlayerAnswer;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.BroadcastMessage;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.PlayerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: dam
 * Date: 13-11-18
 */
public class PlayerServiceImpl implements PlayerService{
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PlayerServiceImpl.class);

    @Autowired
    @Qualifier("playerDAO")
    private PlayerDAO playerDAO;

    @Override
    public CommonMessage enterRoom(String args) {

        RoomPlayer roomPlayer = JSON.parseObject(args,RoomPlayer.class);

        StopWatch clock = new StopWatch();
        clock.start();
        int seatNo = playerDAO.savePlayerRoom(roomPlayer);
        clock.stop();
        LOGGER.info("enterRoom.playerDAO.savePlayerRoom take "+clock.getTime());

        List<RoomPlayer> playerList = new ArrayList<RoomPlayer>();
        clock.reset();
        clock.start();
        playerList = playerDAO.findRoomPlayer(roomPlayer.getRoomId());
        clock.stop();
        LOGGER.info("enterRoom.playerDAO.findRoomPlayer take "+clock.getTime());


        //通知当前玩家，包含自己的座位号和其他人的信息
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiver(roomPlayer.getUserId());
        commonMessage.setCallMethod(Constants.ON_ENTER_ROOM);
        commonMessage.setCallMethodParameters(playerList);

        //广播当前玩家信息
        clock.reset();
        clock.start();

        broadcastToOther(roomPlayer.getUserId(),playerList,
                Constants.ON_OTHER_USER_COME_IN,playerList);
        clock.stop();
        LOGGER.info("enterRoom.BroadcastHandler.broadcast take "+clock.getTime());

        return commonMessage;
    }

    @Override
    public CommonMessage playerReady(String args) {
        JSONObject jsonObject = JSON.parseObject(args);
        String roomId = jsonObject.get("roomId").toString();
        String userId = jsonObject.get("userId").toString();

        List<RoomPlayer> playerList = new ArrayList<RoomPlayer>();
        playerList = playerDAO.findRoomPlayer(roomId);

        broadcastToOther(userId,playerList,Constants.ON_PLAYER_READY,"{\"userId\":\""+userId+"\"}");
        return null;
    }

    @Override
    public CommonMessage leftRoom(String args) {

        RoomPlayer roomPlayer = JSON.parseObject(args,RoomPlayer.class);

        playerDAO.removePlayerRoom(roomPlayer);

        List<RoomPlayer> roomPlayerList = playerDAO.findRoomPlayer(roomPlayer.getRoomId());

        broadcastToOther(roomPlayer.getUserId(),roomPlayerList,
                Constants.ON_OTHER_USER_LEFT,roomPlayer);

        return null;
    }

    @Override
    public CommonMessage answerQuestion(String args) {

        PlayerAnswer playerAnswer = JSON.parseObject(args,PlayerAnswer.class);

        List<RoomPlayer> roomPlayerList = playerDAO.findRoomPlayer(playerAnswer.getRoomId());

        broadcastToOther(playerAnswer.getUserId(),roomPlayerList,
                Constants.ON_ANSWER_COMPLETE,playerAnswer);

        return null;
    }

    private void broadcastToOther(final String sender,List<RoomPlayer> receivers,String callBack,Object parameters){

        //去掉要排除的sender
        receivers = (List<RoomPlayer>)CollectionUtils.select(receivers,new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                RoomPlayer roomPlayer = (RoomPlayer)o;
                return !roomPlayer.getUserId().equals(sender);
            }
        });
        //把要用的属性拿出来放到新的List中
        List<String> userIds = (List<String>)CollectionUtils.collect(receivers,new Transformer() {
            @Override
            public Object transform(Object o) {
                RoomPlayer roomPlayer = (RoomPlayer)o;
                return roomPlayer.getUserId();
            }
        });
        //广播信息
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        broadcastMessage.setReceivers(userIds);
        broadcastMessage.setCallMethod(callBack);
        broadcastMessage.setCallMethodParameters(parameters);

        BroadcastHandler.broadcast(broadcastMessage);
    }
}
