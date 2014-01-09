package com.eighthinfo.sgs.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eighthinfo.sgs.codec.SgsCodecFactory;
import com.eighthinfo.sgs.message.BroadcastHandler;
import com.eighthinfo.sgs.message.CommonMessage;
import com.eighthinfo.sgs.utils.ClassUtils;
import com.eighthinfo.sgs.utils.Invokers;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.redis.core.RedisTemplate;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;

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

    private String host;
    private int port;

    public MainServer(String host,int port) {
        this.port = port;
        this.host = host;
        acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("threadPool",new ExecutorFilter(Executors.newCachedThreadPool()));
        acceptor.getFilterChain().addLast("protocol",
                new ProtocolCodecFilter(new SgsCodecFactory()));
        acceptor.setHandler(new CommonServerHandler(host,port));
    }

    public void start() throws IOException {
        acceptor.bind(new InetSocketAddress(host,port));
        createMBean(acceptor);
    }
    public static void main(String[] args){
        Properties properties = com.eighthinfo.sgs.utils.StringUtils.readProperties("mina.properties");
        int minaPort = Integer.parseInt(properties.getProperty("mina.port"));
        String minaHost = properties.getProperty("mina.host");
        MainServer server = new MainServer(minaHost,minaPort);
        try {
            server.start();
            LOGGER.info("***********************************************************");
            LOGGER.info("* MINA NIO Acceptor Listening socket on:"+minaHost+":"+minaPort+" *");
            LOGGER.info("***********************************************************");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建jmx管理
     * @param nioSocketAcceptor
     */
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
