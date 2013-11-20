package com.eighthinfo.sgs.message;

import org.apache.mina.core.session.IoSession;

import java.util.*;

/**
 * User: dam
 * Date: 13-11-19
 */
public class BroadcastHandler {

    private static final Set<IoSession> sessions = Collections
            .synchronizedSet(new HashSet<IoSession>());


    public static void addSession(IoSession session){
        sessions.add(session);
    }
    public static void broadcast(BroadcastMessage broadcastMessage){
        for(IoSession session:sessions){
            if(session.isConnected() ){

                String nickName = session.getAttribute("nickName").toString();
                if(broadcastMessage.getReceivers().contains(nickName)){
                    session.write(broadcastMessage.getCallMethodParameters());
                }

            }
        }
    }
}
