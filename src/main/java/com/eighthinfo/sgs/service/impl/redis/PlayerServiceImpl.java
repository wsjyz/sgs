package com.eighthinfo.sgs.service.impl.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.Constants;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.BaseService;
import com.eighthinfo.sgs.service.PlayerService;
import com.eighthinfo.sgs.utils.StringUtils;
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
public class PlayerServiceImpl extends BaseService implements PlayerService {

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
        String playerId = jsonObject.getString("playerId");
        String nickName = jsonObject.getString("nickName");
        String awardId = jsonObject.getString("awardId");
        int male = jsonObject.getIntValue("male");

        if(org.apache.commons.lang3.StringUtils.isBlank(playerId)
                || org.apache.commons.lang3.StringUtils.isBlank(nickName)){
            LOGGER.warn("playerId and nickName can't be empty!");
             return null;
        }
        //验证是否已在房间内
        String userRoomInfo = redisTemplate.boundValueOps(playerId).get();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(userRoomInfo)){ //已经在房间
            roomPlayer = JSON.parseObject(userRoomInfo,RoomPlayer.class);
        }else{
            roomPlayer.setPlayerId(playerId);
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
                    //设置座位号,队列中最后一个人的index+1=size
                    Long lsize = redisTemplate.boundListOps(roomPlayer.getRoomId()).size();
                    //String index = redisTemplate.boundListOps(roomPlayer.getRoomId()).index(lsize);
                    //AtomicInteger seatNo = new AtomicInteger(Integer.parseInt(index));
                    roomPlayer.setSeatNo(lsize.intValue());
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
            redisTemplate.boundValueOps(playerId).set(roomPlayerStr);
        }



        List<String> stringList = redisTemplate.boundListOps(roomPlayer.getRoomId()).range(0,5);
        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        //通知当前玩家，包含自己的座位号和其他人的信息
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiver(roomPlayer.getPlayerId());
        commonMessage.setCallMethod(Constants.ON_ENTER_ROOM);
        commonMessage.setCallMethodParameters(playerList);
        //System.out.println("向自己发送:"+playerList);
        //广播当前玩家信息
        broadcastToOther(roomPlayer.getPlayerId(), playerList,
                Constants.ON_OTHER_USER_COME_IN, roomPlayer);
        //System.out.println("向其他人发送:"+roomPlayer);
        return commonMessage;
    }

    @Override
    public CommonMessage playerReady(String args) {

        JSONObject jsonObject = JSON.parseObject(args);
        if(jsonObject.get("roomId") == null || jsonObject.get("playerId") == null){
            return null;
        }

        String roomId = jsonObject.get("roomId").toString();
        String playerId = jsonObject.get("playerId").toString();

        List<String> stringList = redisTemplate.boundListOps(roomId).range(0,5);

        List<RoomPlayer> playerList =  parseStringToObject(stringList);
        //告诉其他人已经准备好了
        broadcastToOther(playerId,playerList,Constants.ON_PLAYER_READY,"{\"playerId\":\""+playerId+"\"}");

        //判断是否这个房间内所有人都准备好了，如果都准备好了就开始答题
        String readyStr = redisTemplate.boundValueOps(roomId+"_READINFO").get();
        int readyCount = 0;
        if(org.apache.commons.lang3.StringUtils.isNotBlank(readyStr)){
            readyCount = Integer.parseInt(readyStr);
        }
        AtomicInteger readyPlayerCount = new AtomicInteger(readyCount);
        Integer result = readyPlayerCount.incrementAndGet();
        if(readyCount >= 6){
            broadcastToOther(null,playerList,Constants.ON_PK_READY,"{\"ready\":\""+true+"\"}");
        }else{
            redisTemplate.boundValueOps(roomId+"_READINFO").set(result.toString());
        }
        return null;
    }

    @Override
    public CommonMessage leftRoom(String args) {

        String playerId = JSON.parseObject(args).getString("playerId");

        if(org.apache.commons.lang3.StringUtils.isBlank(playerId)){
            LOGGER.warn("playerId can't be empty!");
            return null;
        }
        if(!redisTemplate.hasKey(playerId)){
            LOGGER.warn("can't find playerId:"+playerId);
            return null;
        }
        RoomPlayer roomPlayer = JSON.parseObject(redisTemplate.boundValueOps(playerId).get(),RoomPlayer.class);


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

            if(roomPlayerCounts < 0){  //所有人都退出了删除排名中的房间
                redisTemplate.boundZSetOps(roomPlayer.getAwardId()).remove(roomPlayer.getRoomId());
                redisTemplate.delete(roomId+"_READINFO");
            }else{
                List<String> stringList = redisTemplate.boundListOps(roomPlayer.getRoomId()).range(0,5);

                List<RoomPlayer> playerList =  parseStringToObject(stringList);

                broadcastToOther(roomPlayer.getPlayerId(),playerList,
                        Constants.ON_OTHER_USER_LEFT,playerList); //广播所有，让客户端感知座位的变化
            }
        }


        //删除当前用户信息
        redisTemplate.delete(playerId);
        //从缓存中删除
        BroadcastHandler.removeSession(playerId);
        return null;
    }



}
