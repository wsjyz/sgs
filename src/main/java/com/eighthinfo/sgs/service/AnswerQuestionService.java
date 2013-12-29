package com.eighthinfo.sgs.service;

import com.eighthinfo.sgs.message.CommonMessage;

/**
 * Created by dam on 13-12-25.
 */
public interface AnswerQuestionService {

    /**
     * 回答问题
     * @param args
     * @return
     */
    CommonMessage answerQuestion(String args);

    /**
     * 赢得本轮游戏胜利
     * @param args
     * @return
     */
    CommonMessage winGame(String args);
}
