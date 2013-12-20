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

import java.util.Iterator;
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
        String awardId = jsonObject.getString("awardId");
        int male = jsonObject.getIntValue("male");

        if(org.apache.commons.lang3.StringUtils.isBlank(userId)
                || org.apache.commons.lang3.StringUtils.isBlank(nickName)){
            LOGGER.warn("userId and nickName can't be empty!");
             return null;
        }
        //验证是否已在房间内
        String userRoomInfo = redisTemplate.boundValueOps(userId).get();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(userRoomInfo)){ //已经在房间
            roomPlayer = JSON.parseObject(userRoomInfo,RoomPlayer.class);
        }else{
            roomPlayer.setUserId(userId);
            roomPlayer.setNickName(nickName);
            roomPlayer.setAwardId(awardId);
            roomPlayer.setMale(male);

            //查找应该进入的房间,根据奖品ID来查找，该类奖品下房间的情况
            Set<ZSetOperations.TypedTuple<String>> set
                    = redisTemplate.boundZSetOps(awardId).reverseRangeByScoreWithScores(0,4);

            AtomicBoolean hasRoom = new AtomicBoolean(false);
            if(set.isEmpty()){ //第一个人、没有房间
                hasRoom.set(false);
            }else{
                Iterator<ZSetOperations.TypedTuple<String>> itor = set.iterator();
                if(itor.hasNext()){//有人数未满的房间

                    ZSetOperations.TypedTuple<String> typedTuple = itor.next();
                    String roomId = typedTuple.getValue();
                    roomPlayer.setRoomId(roomId);
                    AtomicInteger seatNo = new AtomicInteger(typedTuple.getScore().intValue());
                    roomPlayer.setSeatNo(seatNo.incrementAndGet());
                    redisTemplate.boundZSetOps(awardId).incrementScore(roomPlayer.getRoomId(), 1);
                    hasRoom.set(true);

                }
            }
            //新建一个房间
            if(!hasRoom.get()){
                String roomId = StringUtils.genShortPK();
                roomPlayer.setRoomId(roomId);
                roomPlayer.setSeatNo(0);
                redisTemplate.boundZSetOps(awardId).add(roomId, 0);
            }
            String roomPlayerStr = JSON.toJSONString(roomPlayer);
            redisTemplate.boundListOps(roomPlayer.getRoomId()).rightPush(roomPlayerStr);
            redisTemplate.boundValueOps(userId).set(roomPlayerStr);
        }



        List<String> stringList = redisTemplate.boundListOps(roomPlayer.getRoomId()).range(0,5);
        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        //通知当前玩家，包含自己的座位号和其他人的信息
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiver(roomPlayer.getUserId());
        commonMessage.setCallMethod(Constants.ON_ENTER_ROOM);
        commonMessage.setCallMethodParameters(playerList);

        //广播当前玩家信息
        broadcastToOther(roomPlayer.getUserId(), playerList,
                Constants.ON_OTHER_USER_COME_IN, playerList);

        return commonMessage;
    }

    @Override
    public CommonMessage playerReady(String args) {

        JSONObject jsonObject = JSON.parseObject(args);
        if(jsonObject.get("roomId") == null || jsonObject.get("userId") == null){
            return null;
        }

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
        if(!redisTemplate.hasKey(userId)){
            LOGGER.warn("can't find userId:"+userId);
            return null;
        }
        RoomPlayer roomPlayer = JSON.parseObject(redisTemplate.boundValueOps(userId).get(),RoomPlayer.class);


        //从房间用户列表中删除当前用户
        String roomId = roomPlayer.getRoomId();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(roomId)){
            redisTemplate.boundListOps(roomPlayer.getRoomId()).remove(1,JSON.toJSONString(roomPlayer));
        }
        //减少此房间的人数
        int roomPlayerCounts = 0;
        if(org.apache.commons.lang3.StringUtils.isNotBlank(roomPlayer.getAwardId())){

            roomPlayerCounts = redisTemplate.boundZSetOps(roomPlayer.getAwardId())
                    .incrementScore(roomPlayer.getRoomId(),-1).intValue();

            if(roomPlayerCounts <= 0){  //所有人都退出了删除排名中的房间
                redisTemplate.boundZSetOps(roomPlayer.getAwardId()).remove(roomPlayer.getRoomId());
            }else{
                List<String> stringList = redisTemplate.boundListOps(roomPlayer.getRoomId()).range(0,5);

                List<RoomPlayer> playerList =  parseStringToObject(stringList);

                broadcastToOther(roomPlayer.getUserId(),playerList,
                        Constants.ON_OTHER_USER_LEFT,roomPlayer);
            }
        }


        //删除当前用户信息
        redisTemplate.delete(userId);
        //从缓存中删除
        BroadcastHandler.removeSession(userId);
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
