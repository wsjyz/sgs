package com.eighthinfo.sgs.codec;

import com.eighthinfo.sgs.message.CommonMessage;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Created with IntelliJ IDEA.
 * User: dam
 * Date: 13-11-14
 * Time: pm1:02
 * To change this template use File | Settings | File Templates.
 */
public class MessageEncoder extends ProtocolEncoderAdapter {
    @Override
    public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
        CommonMessage message = (CommonMessage)o;
        byte[] bytes = message.toBytes();
        IoBuffer buf = IoBuffer.allocate(bytes.length, false);

        buf.setAutoExpand(true);
        buf.putInt(bytes.length);
        buf.put(bytes);

        buf.flip();
        protocolEncoderOutput.write(buf);
    }
}
