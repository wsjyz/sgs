package com.eighthinfo.sgs.message;

import com.eighthinfo.sgs.Constants;
import com.eighthinfo.sgs.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: 下午4:49
 * To change this template use File | Settings | File Templates.
 */
public class MessageBase {

    private String callMethod;

    private Object callMethodParameters;

    public Object getCallMethodParameters() {
        return callMethodParameters;
    }

    public void setCallMethodParameters(Object callMethodParameters) {
        this.callMethodParameters = callMethodParameters;
    }

    public String getCallMethod() {
        return callMethod;
    }

    public void setCallMethod(String callMethod) {
        this.callMethod = callMethod;
    }

    public byte[] toBytes() throws Exception {
        byte[] params = JSONUtils.toJSONStringBytes(getCallMethodParameters());
        int serverMethodNameLength = Constants.MESSAGE_SERVICE_NAME_LENGTH;
        int len = serverMethodNameLength  + params.length;
        byte[] bytes = new byte[len];
        String serverMethodName = StringUtils.rightPad(getCallMethod(), serverMethodNameLength,
                Constants.MESSAGE_NAME_PAD_CHAR);

        serverMethodName = serverMethodName.substring(0, serverMethodNameLength);

        System.arraycopy(serverMethodName.getBytes(), 0, bytes, 0, serverMethodNameLength);
        System.arraycopy(params, 0, bytes, serverMethodNameLength, params.length);
        return bytes;
    }
}
