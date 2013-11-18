package com.eighthinfo.sgs.service.impl;

import com.eighthinfo.sgs.dao.PlayerDAO;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.service.PlayerService;
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

    @Qualifier("playerDAO")
    private PlayerDAO playerDAO;
    @Override
    public List<RoomPlayer> enterRoom(String nickName,String roomId) {
        List<RoomPlayer> playerList = new ArrayList<RoomPlayer>();

        playerDAO.savePlayerRoom(nickName,roomId);

        playerList = playerDAO.findRoomPlayer(roomId);

        return playerList;
    }
}
