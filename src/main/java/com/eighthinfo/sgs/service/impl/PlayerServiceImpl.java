package com.eighthinfo.sgs.service.impl;

import com.eighthinfo.sgs.dao.PlayerDAO;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.BroadcastMessage;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.PlayerService;
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
        String nickName = "jyz";
        String roomId = "a1";

        StopWatch clock = new StopWatch();
        clock.start();
        int seatNo = playerDAO.savePlayerRoom(nickName,roomId);
        clock.stop();
        LOGGER.info("enterRoom.playerDAO.savePlayerRoom take "+clock.getTime());

        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiver(nickName);
        commonMessage.setCallMethod("lll");
        commonMessage.setCallMethodParameters(seatNo+"");


        List<RoomPlayer> playerList = new ArrayList<RoomPlayer>();
        clock.reset();
        clock.start();
        playerList = playerDAO.findRoomPlayer(roomId);
        clock.stop();
        LOGGER.info("enterRoom.playerDAO.findRoomPlayer take "+clock.getTime());

        List<String> nickNames = new ArrayList<String>();

        for(RoomPlayer player:playerList){

            if(!player.getNickName().equals(nickName)){
                nickNames.add(player.getNickName());
            }

        }
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        broadcastMessage.setReceivers(nickNames);
        broadcastMessage.setCallMethod("aa");
        broadcastMessage.setCallMethodParameters(nickName+seatNo);
        clock.reset();
        clock.start();
        BroadcastHandler.broadcast(broadcastMessage);
        clock.stop();
        LOGGER.info("enterRoom.BroadcastHandler.broadcast take "+clock.getTime());
        return commonMessage;
    }
}
