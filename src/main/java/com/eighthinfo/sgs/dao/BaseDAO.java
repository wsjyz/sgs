package com.eighthinfo.sgs.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-18
 * Time: 下午5:39
 * To change this template use File | Settings | File Templates.
 */
public class BaseDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
