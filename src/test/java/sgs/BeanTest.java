package sgs;


import com.alibaba.fastjson.JSON;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.utils.ClassUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-15
 * Time: 下午5:41
 * To change this template use File | Settings | File Templates.
 */
public class BeanTest {

    public static void main(String[] args){
        //ApplicationContext ac = new ClassPathXmlApplicationContext("spring-config.xml");
//        MessageResponse result = (MessageResponse)ClassUtils.invokeMethod(ac.getBean("messageHandler"),"enterRoom",new Class<?>[]{String.class},new String[]{"222"});
//        System.out.println(result.getClientMethodParameters());
        List<RoomPlayer>  playerList = new ArrayList<RoomPlayer>();
        RoomPlayer roomPlayer1 = new RoomPlayer();
        roomPlayer1.setNickName("1");
        roomPlayer1.setSeatNo(1);

        RoomPlayer roomPlayer2 = new RoomPlayer();
        roomPlayer2.setNickName("2");
        roomPlayer2.setSeatNo(2);

        playerList.add(roomPlayer1);
        playerList.add(roomPlayer2);
        System.out.println(JSON.toJSONString(playerList));
    }
}
