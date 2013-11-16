package com.eighthinfo.sgs.message;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: 下午4:44
 * To change this template use File | Settings | File Templates.
 */
public class MessageResponse {

    private String clientMethod;

    private String clientMethodParameters;

    public String getClientMethod() {
        return clientMethod;
    }

    public void setClientMethod(String clientMethod) {
        this.clientMethod = clientMethod;
    }

    public String getClientMethodParameters() {
        return clientMethodParameters;
    }

    public void setClientMethodParameters(String clientMethodParameters) {
        this.clientMethodParameters = clientMethodParameters;
    }

}
