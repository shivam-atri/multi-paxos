package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/24/16.
 */
public class HeartBeatReplyMessage extends Message{

    private long index;

    protected HeartBeatReplyMessage() {}

    public HeartBeatReplyMessage(NodeIdentifier sender, long index) {
        super(MSG_TYPE.HeartBeatReply, sender);
        this.index = index;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(index);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        index = buf.readLong();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("HearBeatReply Message <src=").append(super.getSender())
                .append(" index=").append(index).append(">");
        return sb.toString();
    }

    public long getIndex() {
        return index;
    }
}
