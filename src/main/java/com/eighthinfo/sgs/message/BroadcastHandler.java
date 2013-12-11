package com.eighthinfo.sgs.message;

import org.apache.mina.core.session.IoSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: dam
 * Date: 13-11-19
 */
public class BroadcastHandler {

    private static final ConcurrentHashMap<String,IoSession> sessions = new ConcurrentHashMap<String,IoSession>();


    public static void addSession(String key ,IoSession session){
        sessions.putIfAbsent(key,session);
    }
    public static void broadcast(BroadcastMessage broadcastMessage){

        for(Map.Entry<String,IoSession> entry : sessions.entrySet()){

            String userId = entry.getKey();
            IoSession session = entry.getValue();

            if(broadcastMessage.getReceivers().contains(userId)){
                if(session.isConnected()){
                    session.write(broadcastMessage);
                }

            }
        }

    }

    public static void removeSession(String key){
        sessions.remove(key);
    }
}
