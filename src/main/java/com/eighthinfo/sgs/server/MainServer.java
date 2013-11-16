package com.eighthinfo.sgs.server;

import com.eighthinfo.sgs.codec.SgsCodecFactory;
import com.eighthinfo.sgs.message.MessageRequest;
import com.eighthinfo.sgs.message.MessageResponse;
import com.eighthinfo.sgs.utils.ClassUtils;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.statemachine.StateMachine;
import org.apache.mina.statemachine.StateMachineFactory;
import org.apache.mina.statemachine.StateMachineProxyBuilder;
import org.apache.mina.statemachine.annotation.IoHandlerTransition;
import org.apache.mina.statemachine.context.IoSessionStateContextLookup;
import org.apache.mina.statemachine.context.StateContext;
import org.apache.mina.statemachine.context.StateContextFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-11
 * Time: pm4:44
 * To change this template use File | Settings | File Templates.
 */
public class MainServer {

    private NioSocketAcceptor acceptor;

    private int port;

    ApplicationContext ac = new ClassPathXmlApplicationContext("spring-config.xml");

    public MainServer(int port) {
        this.port = port;
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("protocol",
                new ProtocolCodecFilter(new SgsCodecFactory()));
        acceptor.setHandler(new CommonIoHandler());
    }

    public void start() throws IOException {
        acceptor.bind(new InetSocketAddress(port));
    }
    public static void main(String[] args) throws IOException{
        MainServer mainServer = new MainServer(9110);
        mainServer.start();
    }
    private class CommonIoHandler extends IoHandlerAdapter {
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            MessageRequest messageRequest = (MessageRequest)message;
            String handlerName = messageRequest.getServerMethod();
            String clazzName = handlerName.substring(0,handlerName.indexOf("."));
            String methodName = handlerName.substring(handlerName.indexOf(".") + 1,handlerName.length());

            MessageResponse result = (MessageResponse)ClassUtils.invokeMethod(ac.getBean(clazzName),
                    methodName,new Class<?>[]{String.class},new String[]{messageRequest.getServerMethodParameters()});

            super.messageReceived(session, message);
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
