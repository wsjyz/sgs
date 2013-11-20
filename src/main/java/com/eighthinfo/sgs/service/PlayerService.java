package com.eighthinfo.sgs.service;

import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.CommonMessage;


/**
 * User: dam
 * Date: 13-11-18
 */
public interface PlayerService {

    CommonMessage enterRoom(String args);

}
