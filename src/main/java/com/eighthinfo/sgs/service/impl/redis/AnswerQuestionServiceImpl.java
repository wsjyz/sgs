package com.eighthinfo.sgs.service.impl.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.Constants;
import com.eighthinfo.sgs.domain.PlayerAnswer;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.service.AnswerQuestionService;
import com.eighthinfo.sgs.service.BaseService;
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
            redisTemplate.boundListOps("playerExp").leftPush(playerAnswer.getPlayerId()
                    +"#"+ Constants.SCORE_OPTION_ANSWER_RIGHT);

        }
        //获取房间内所有的玩家信息，并广播
        List<String> stringList = redisTemplate.boundListOps(playerAnswer.getRoomId()).range(0,5);
        //去掉roomId
        playerAnswer.setRoomId(null);
        List<RoomPlayer> playerList =  parseStringToObject(stringList);

        broadcastToOther(playerAnswer.getPlayerId(),playerList,
                Constants.ON_ANSWER_COMPLETE,playerAnswer);

        //给自己也发一个，客户端需要做进入下一题的依据
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiver(playerAnswer.getPlayerId());
        commonMessage.setCallMethod(Constants.ON_ANSWER_COMPLETE);
        commonMessage.setCallMethodParameters(playerAnswer);
        return commonMessage;
    }

    @Override
    public CommonMessage winGame(String args) {

        JSONObject jsonObject = JSON.parseObject(args);
        String playerId = jsonObject.getString("playerId");

        //添加得分
        //TODO the key playerExp should be conf
        redisTemplate.boundListOps("playerExp").leftPush(playerId
                +"#"+ Constants.SCORE_OPTION_WIN_GAME);

        return null;
    }
}
