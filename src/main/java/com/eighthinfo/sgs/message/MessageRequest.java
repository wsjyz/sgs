package com.eighthinfo.sgs.message;

import com.eighthinfo.sgs.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: 下午4:10
 * To change this template use File | Settings | File Templates.
 */
public class MessageRequest {

    private String serverMethod;

    private String serverMethodParameters;

    public String getServerMethodParameters() {
        return serverMethodParameters;
    }

    public void setServerMethodParameters(String serverMethodParameters) {
        this.serverMethodParameters = serverMethodParameters;
    }

    public String getServerMethod() {
        return serverMethod;
    }

    public void setServerMethod(String serverMethod) {
        this.serverMethod = serverMethod;
    }

    public byte[] toBytes() throws Exception {
        byte[] params = getServerMethodParameters().getBytes();
        int serverMethodNameLength = Constants.MESSAGE_SERVICE_NAME_LENGTH;
        int len = serverMethodNameLength  + params.length;
        byte[] bytes = new byte[len];
        String serverMethodName = StringUtils.rightPad(getServerMethod(), serverMethodNameLength,
                Constants.MESSAGE_NAME_PAD_CHAR);

        serverMethodName = serverMethodName.substring(0, serverMethodNameLength);

        System.arraycopy(serverMethodName.getBytes(), 0, bytes, 0, serverMethodNameLength);
        System.arraycopy(params, 0, bytes, serverMethodNameLength, params.length);
        return bytes;
    }
}
