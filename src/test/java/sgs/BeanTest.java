package sgs;


import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.utils.StringUtils;

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
//        List<RoomPlayer>  playerList = new ArrayList<RoomPlayer>();
//        RoomPlayer roomPlayer1 = new RoomPlayer();
//        roomPlayer1.setNickName("1");
//        roomPlayer1.setSeatNo(1);
//
        RoomPlayer roomPlayer2 = new RoomPlayer();
        roomPlayer2.setNickName("2");
        roomPlayer2.setSeatNo(2);
        roomPlayer2.setPlayerId(StringUtils.genShortPK());
        roomPlayer2.setAwardId(StringUtils.genShortPK());
        roomPlayer2.setRoomId(StringUtils.genShortPK());
        roomPlayer2.setMale(1);
       // System.out.println(MemoryCalculator.shallowSizeOf(roomPlayer2));
//
//        playerList.add(roomPlayer1);
//        playerList.add(roomPlayer2);
//        System.out.println(JSON.toJSONString(playerList));
//        try {
//            System.out.println(new String(JSONUtils.toJSONStringBytes("{\"userId\":\"abc\"}"),"UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }
}
