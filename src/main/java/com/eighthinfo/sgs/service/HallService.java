package com.eighthinfo.sgs.service;

/**
 * Created by dam on 13-12-11.
 */
public interface HallService {

    void registHall(String host,int port);

    void cancelHall(String host,int port);

    void increaseHallUserCount(String host,int port,boolean isIncrease);
}
