package com.eighthinfo.sgs.service.impl.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.Constants;
import com.eighthinfo.sgs.domain.PlayerAnswer;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.BroadcastMessage;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.PlayerService;
import com.eighthinfo.sgs.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: dam
 * Date: 13-11-26
 */
public class PlayerServiceImpl implements PlayerService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PlayerServiceImpl.class);

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public CommonMessage enterRoom(String args) {

        //RoomPlayer roomPlayer = JSON.parseObject(args, RoomPlayer.class);
        RoomPlayer roomPlayer = new RoomPlayer();
        JSONObject jsonObject = JSON.parseObject(args);
        String userId = jsonObject.getString("userId");
        String nickName = jsonObject.getString("nickName");

        if(org.apache.commons.lang3.StringUtils.isBlank(userId)
                || org.apache.commons.lang3.StringUtils.isBlank(nickName)){
            LOGGER.warn("userId and nickName can't be empty!");
             return null;
        }

        //查找应该进入的房间

        //TODO the key 'hallSet' should configurable
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.boundZSetOps("hallSet").reverseRangeWithScores(0,-1);

        AtomicBoolean hasRoom = new AtomicBoolean(false);

        if(set.isEmpty()){ //第一个人、没有房间
            hasRoom.set(false);
        }else{
            for(ZSetOperations.TypedTuple typedTuple:set){
               if(typedTuple.getScore() < 6 ){//有房间
                   roomPlayer.setRoomId(typedTuple.getValue().toString());
                   AtomicInteger seatNo = new AtomicInteger(typedTuple.getScore().intValue());
                   roomPlayer.setSeatNo(seatNo.incrementAndGet());
                   hasRoom.set(true);
               }
            }
        }
        //新建一个房间
        if(!hasRoom.get()){
            roomPlayer.setRoomId(StringUtils.genShortPK());
            roomPlayer.setSeatNo(1);
        }
        String roomPlayerStr = JSON.toJSONString(roomPlayer);
        redisTemplate.boundListOps(roomPlayer.getRoomId()).rightPush(roomPlayerStr);
        redisTemplate.boundValueOps(userId).set(roomPlayerStr);

        List<String> stringList = redisTemplate.boundListOps(roomPlayer.getRoomId()).range(0,5);

        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        //通知当前玩家，包含自己的座位号和其他人的信息
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiver(roomPlayer.getUserId());
        commonMessage.setCallMethod(Constants.ON_ENTER_ROOM);
        commonMessage.setCallMethodParameters(playerList);

        //广播当前玩家信息

        broadcastToOther(roomPlayer.getUserId(),playerList,
                Constants.ON_OTHER_USER_COME_IN,playerList);

        return commonMessage;
    }

    @Override
    public CommonMessage playerReady(String args) {

        JSONObject jsonObject = JSON.parseObject(args);

        String roomId = jsonObject.get("roomId").toString();
        String userId = jsonObject.get("userId").toString();

        List<String> stringList = redisTemplate.boundListOps(roomId).range(0,5);

        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        broadcastToOther(userId,playerList,Constants.ON_PLAYER_READY,"{\"userId\":\""+userId+"\"}");

        return null;
    }

    @Override
    public CommonMessage leftRoom(String args) {

        String userId = JSON.parseObject(args).getString("userId");

        if(org.apache.commons.lang3.StringUtils.isBlank(userId)){
            LOGGER.warn("userId can't be empty!");
            return null;
        }
        RoomPlayer roomPlayer = JSON.parseObject(redisTemplate.boundValueOps(userId).get(),RoomPlayer.class);

        //TODO needTest
        redisTemplate.boundListOps(roomPlayer.getRoomId()).remove(1,JSON.toJSONString(roomPlayer));

        List<String> stringList = redisTemplate.boundListOps(roomPlayer.getRoomId()).range(0,5);

        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        broadcastToOther(roomPlayer.getUserId(),playerList,
                Constants.ON_OTHER_USER_LEFT,roomPlayer);

        return null;
    }

    @Override
    public CommonMessage answerQuestion(String args) {

        PlayerAnswer playerAnswer = JSON.parseObject(args,PlayerAnswer.class);

        List<String> stringList = redisTemplate.boundListOps(playerAnswer.getRoomId()).range(0,5);

        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        broadcastToOther(playerAnswer.getUserId(),playerList,
                Constants.ON_ANSWER_COMPLETE,playerAnswer);

        return null;
    }

    private List<RoomPlayer> parseStringToObject(List<String> stringList){

        return (List<RoomPlayer>)CollectionUtils.collect(stringList,new Transformer() {
            @Override
            public Object transform(Object o) {
                RoomPlayer roomPlayer = JSON.parseObject(o.toString(),RoomPlayer.class);
                return roomPlayer;
            }
        });
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
