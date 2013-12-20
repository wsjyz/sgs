package com.eighthinfo.sgs.domain;


import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * User: dam
 * Date: 13-11-23
 */
public class AbstractModel implements java.io.Serializable {

    private static final long serialVersionUID = -6889934380648195269L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
