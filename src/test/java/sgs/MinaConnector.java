package sgs;

import com.eighthinfo.sgs.codec.SgsCodecFactory;
import com.eighthinfo.sgs.domain.RoomPlayer;
import com.eighthinfo.sgs.message.CommonMessage;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-8
 * Time: 下午2:38
 * To change this template use File | Settings | File Templates.
 */
public class MinaConnector {
    public static final int CONNECT_TIMEOUT = 3000;

    private String host;
    private int port;
    private static boolean  USE_CUSTOM_CODEC = true;
    NioSocketConnector connector;
    private IoSession session;
    public MinaConnector(String host, int port){
        this.host = host;
        this.port = port;
        connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.getFilterChain().addLast("protocol",new ProtocolCodecFilter(new SgsCodecFactory()));
    }
    public void setHandler(IoHandler handler) {
        connector.setHandler(handler);
    }
    public void connect() {
        ConnectFuture connectFuture = connector.connect(new InetSocketAddress(
                host, port));
        connectFuture.awaitUninterruptibly(CONNECT_TIMEOUT);
        try {
            session = connectFuture.getSession();
        } catch (RuntimeIoException e) {
            e.printStackTrace();
        }
    }
    public void disconnect() {
        if (session != null) {
            session.close(false).awaitUninterruptibly(CONNECT_TIMEOUT);
            session = null;
        }
    }

    public IoSession getSession() {
        return this.session;
    }

    public void sendCommand(CommonMessage command) {
        if (session != null) {
            session.write(command);
        }
    }
    public class ClientHandler extends IoHandlerAdapter{
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            System.out.println("client receive message");
            super.messageReceived(session, message);
        }
    }
    public static void main(String[] args){
        MinaConnector minaConnector = new MinaConnector("localhost",9110);
        minaConnector.setHandler(minaConnector.new ClientHandler());
        minaConnector.connect();
        CommonMessage messageRequest = new CommonMessage();
        messageRequest.setCallMethod("playerService.enterRoom");
        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setUserId("jyz");
        roomPlayer.setNickName("bit");
        messageRequest.setCallMethodParameters(roomPlayer);
        minaConnector.sendCommand(messageRequest);
        //minaConnector.disconnect();
    }
}
