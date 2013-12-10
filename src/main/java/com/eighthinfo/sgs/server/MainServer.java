package com.eighthinfo.sgs.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.codec.SgsCodecFactory;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.utils.ClassUtils;
import com.eighthinfo.sgs.utils.Invokers;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.integration.jmx.IoServiceMBean;
import org.apache.mina.statemachine.StateMachine;
import org.apache.mina.statemachine.StateMachineFactory;
import org.apache.mina.statemachine.StateMachineProxyBuilder;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.statemachine.context.IoSessionStateContextLookup;
import org.apache.mina.statemachine.context.StateContext;
import org.apache.mina.statemachine.context.StateContextFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-11
 * Time: pm4:44
 * To change this template use File | Settings | File Templates.
 */
public class MainServer {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MainServer.class);

    private NioSocketAcceptor acceptor;

    private int port;

    public MainServer(int port) {
        this.port = port;
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("protocol",
                new ProtocolCodecFilter(new SgsCodecFactory()));
        acceptor.setHandler(new CommonServerHandler());
    }

    public void start() throws IOException {
        acceptor.bind(new InetSocketAddress(port));
    }
    public static void main(String[] args){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("server-run.xml");
        NioSocketAcceptor socketAcceptor = (NioSocketAcceptor)applicationContext.getBean("ioAcceptor");
        MainServer.createMBean(socketAcceptor);

        LOGGER.info("***************************************************");
        LOGGER.info("* MINA NIO Acceptor Listening socket in port:"+socketAcceptor.getDefaultLocalAddress().getPort()+" *");
        LOGGER.info("***************************************************");


    }
    private static void createMBean(NioSocketAcceptor nioSocketAcceptor){

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        IoServiceMBean acceptorMBean = new IoServiceMBean(nioSocketAcceptor);
        ObjectName acceptorName = null;
        try {
            acceptorName = new ObjectName(nioSocketAcceptor.getClass()
                    .getPackage().getName()
                    + ":type=acceptor,name=" + nioSocketAcceptor.getClass().getSimpleName());
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        try {
            mBeanServer.registerMBean(acceptorMBean, acceptorName);
        } catch (InstanceAlreadyExistsException
                | MBeanRegistrationException
                | NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }
    private static IoHandler createIoHandlerUseStateMachine() {
        StateMachine sm = StateMachineFactory.getInstance(
                IoHandlerTransition.class).create(ServerHandler.NOT_CONNECTED,
                new ServerHandler());
        return new StateMachineProxyBuilder().setStateContextLookup(
                new IoSessionStateContextLookup(new StateContextFactory() {
                    public StateContext create() {
                        return new ServerHandler.SgsServerContext();
                    }
                })).create(IoHandler.class, sm);
    }
}
