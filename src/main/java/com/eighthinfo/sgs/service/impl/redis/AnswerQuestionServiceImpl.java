package com.eighthinfo.sgs.service.impl.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.Constants;
import com.eighthinfo.sgs.domain.PlayerAnswer;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.AnswerQuestionService;
import com.eighthinfo.sgs.service.BaseService;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * Created by dam on 13-12-25.
 */
public class AnswerQuestionServiceImpl extends BaseService implements AnswerQuestionService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AnswerQuestionServiceImpl.class);

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public CommonMessage answerQuestion(String args) {

        PlayerAnswer playerAnswer = JSON.parseObject(args, PlayerAnswer.class);
        //答对题加分
        if(playerAnswer.getRight() == 1){
            //添加得分
            //TODO the key playerExp should be conf
            redisTemplate.boundListOps("playerExp").leftPush(playerAnswer.getUserId()
                    +"#"+ Constants.SCORE_OPTION_ANSWER_RIGHT);

        }
        //获取房间内所有的玩家信息，并广播
        List<String> stringList = redisTemplate.boundListOps(playerAnswer.getRoomId()).range(0,5);
        //去掉roomId
        playerAnswer.setRoomId(null);
        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        broadcastToOther(playerAnswer.getUserId(),playerList,
                Constants.ON_ANSWER_COMPLETE,playerAnswer);

        return null;
    }

    @Override
    public CommonMessage winGame(String args) {

        JSONObject jsonObject = JSON.parseObject(args);
        String userId = jsonObject.getString("userId");

        //添加得分
        //TODO the key playerExp should be conf
        redisTemplate.boundListOps("playerExp").leftPush(userId
                +"#"+ Constants.SCORE_OPTION_WIN_GAME);

        return null;
    }
}
