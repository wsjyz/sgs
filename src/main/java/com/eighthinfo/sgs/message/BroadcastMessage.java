package com.eighthinfo.sgs.message;

import java.util.List;

/**
 * User: dam
 * Date: 13-11-19
 */
public class BroadcastMessage extends MessageBase{

    private List<String> receivers;

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }
}
