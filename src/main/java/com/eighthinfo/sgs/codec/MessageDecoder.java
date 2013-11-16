package com.eighthinfo.sgs.codec;

import com.eighthinfo.sgs.Constants;
import com.eighthinfo.sgs.message.MessageRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: pm1:01
 * To change this template use File | Settings | File Templates.
 */
public class MessageDecoder extends CumulativeProtocolDecoder {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MessageDecoder.class);

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer in, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        if (in.prefixedDataAvailable(4, Constants.MAX_MESSAGE_LENGTH)) {
            int length = in.getInt();
            byte[] bytes = new byte[length];
            in.get(bytes);
            int operateNameLength = Constants.MESSAGE_SERVICE_NAME_LENGTH;
            byte[] messageNameBytes = new byte[operateNameLength];
            System.arraycopy(bytes, 0, messageNameBytes, 0, operateNameLength);
            String messageName = StringUtils.trim(new String(messageNameBytes));
            LOGGER.info("receive message name "+messageName);


            byte[] messageBodyBytes = new byte[length - operateNameLength];
            System.arraycopy(bytes, operateNameLength, messageBodyBytes, 0,
                    length - operateNameLength);
            String messageBody = new String(messageBodyBytes);
            LOGGER.info("receive message body "+messageBody);
            MessageRequest messageRequest = new MessageRequest();
            messageRequest.setServerMethod(messageName);
            messageRequest.setServerMethodParameters(messageBody);
            protocolDecoderOutput.write(messageRequest);
            return true;
        }else{
            return false;
        }

    }
}
