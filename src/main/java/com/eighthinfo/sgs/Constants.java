package com.eighthinfo.sgs;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: pm1:07
 * To change this template use File | Settings | File Templates.
 */
public interface Constants {

    int MESSAGE_SERVICE_NAME_LENGTH = 30;

    int MAX_MESSAGE_LENGTH = 4192;

    char MESSAGE_NAME_PAD_CHAR = ' ';

    String SCORE_OPTION_ANSWER_RIGHT= "scoreOptionAnswerRight";

    String SCORE_OPTION_WIN_GAME = "scoreOptionWinGame";

    String ON_ENTER_ROOM = "onEnterRoom";

    String ON_OTHER_USER_COME_IN = "onOtherUserComeIn";

    String ON_PLAYER_READY = "onPlayerReady";

    String ON_GAME_START = "onGameStart";

    String ON_ANSWER_COMPLETE ="onAnswerComplete";

    String ON_ANSWER_WRONG ="onAnswerWrong";

    String ON_CURRENT_PLAYER_CHANGE= "onCurrentPlayerChange";

    String ON_ROUND_FINISHED = "onRoundFinished";

    String ON_GAME_OVER = "onGameOver";

    String ON_OTHER_USER_LEFT = "onOtherUserLeft";

    String ON_ERROR_OCCUR = "onErrorOccur";
}
