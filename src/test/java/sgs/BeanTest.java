package sgs;


import com.eighthinfo.sgs.utils.ClassUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-15
 * Time: 下午5:41
 * To change this template use File | Settings | File Templates.
 */
public class BeanTest {

    public static void main(String[] args){
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-config.xml");
//        MessageResponse result = (MessageResponse)ClassUtils.invokeMethod(ac.getBean("messageHandler"),"enterRoom",new Class<?>[]{String.class},new String[]{"222"});
//        System.out.println(result.getClientMethodParameters());
    }
}
