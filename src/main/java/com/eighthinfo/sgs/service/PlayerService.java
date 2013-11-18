package com.eighthinfo.sgs.service;

import com.eighthinfo.sgs.domain.RoomPlayer;

import java.util.List;


/**
 * User: dam
 * Date: 13-11-18
 */
public interface PlayerService {

    List<RoomPlayer> enterRoom(String nickName,String roomId);

}
