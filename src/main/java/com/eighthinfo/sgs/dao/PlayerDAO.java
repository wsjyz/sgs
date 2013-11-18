package com.eighthinfo.sgs.dao;

import com.eighthinfo.sgs.domain.RoomPlayer;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-18
 * Time: 下午4:49
 * To change this template use File | Settings | File Templates.
 */
public interface PlayerDAO {

    void savePlayerRoom(String nickName,String roomId);

    List<RoomPlayer> findRoomPlayer(String roomId);
}
