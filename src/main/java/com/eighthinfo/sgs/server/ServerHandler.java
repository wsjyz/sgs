package com.eighthinfo.sgs.server;

import org.apache.mina.statemachine.annotation.State;
import org.apache.mina.statemachine.context.AbstractStateContext;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-11
 * Time: pm4:44
 * To change this template use File | Settings | File Templates.
 */
public class ServerHandler {

    @State
    public static final String ROOT = "Root";
    @State
    public static final String WAIT_IN_ROOM = "WaitInRoom";
    @State
    public static final String PKING = "Pking";
    @State
    public static final String WAIT_PK = "WaitPK";
    @State
    public static final String WATCH = "Watch";
    @State
    public static final String NOT_CONNECTED = "NotConnected";

    static class SgsServerContext extends AbstractStateContext{

    }

}
