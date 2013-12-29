package com.eighthinfo.sgs.service;

import com.alibaba.fastjson.JSON;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.BroadcastMessage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import java.util.List;

/**
 * Created by dam on 13-12-25.
 */
public class BaseService {

    protected void broadcastToOther(final String sender,List<RoomPlayer> receivers,String callBack,Object parameters){

        //去掉要排除的sender
        receivers = (List<RoomPlayer>) CollectionUtils.select(receivers, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                RoomPlayer roomPlayer = (RoomPlayer) o;
                return !roomPlayer.getPlayerId().equals(sender);
            }
        });
        //把要用的属性拿出来放到新的List中
        List<String> userIds = (List<String>)CollectionUtils.collect(receivers,new Transformer() {
            @Override
            public Object transform(Object o) {
                RoomPlayer roomPlayer = (RoomPlayer)o;
                return roomPlayer.getPlayerId();
            }
        });
        //广播信息
        BroadcastMessage broadcastMessage = new BroadcastMessage();
        broadcastMessage.setReceivers(userIds);
        broadcastMessage.setCallMethod(callBack);
        broadcastMessage.setCallMethodParameters(parameters);


        BroadcastHandler.broadcast(broadcastMessage);
    }

    protected List<RoomPlayer> parseStringToObject(List<String> stringList){

        return (List<RoomPlayer>)CollectionUtils.collect(stringList,new Transformer() {
            @Override
            public Object transform(Object o) {
                RoomPlayer roomPlayer = JSON.parseObject(o.toString(), RoomPlayer.class);
                return roomPlayer;
            }
        });
    }
}
