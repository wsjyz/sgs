package com.eighthinfo.sgs.service;

import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.CommonMessage;


/**
 * User: dam
 * Date: 13-11-18
 */
public interface PlayerService {

    /**
     * 进入房间
     * @param args
     * @return
     */
    CommonMessage enterRoom(String args);

    /**
     * 当前玩家已经准备好
     * @return
     */
    CommonMessage playerReady(String args);

    /**
     * 玩家离开房间
     * @param args
     * @return
     */
    CommonMessage leftRoom(String args);



}
