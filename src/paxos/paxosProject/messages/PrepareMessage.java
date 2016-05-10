package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

public class PrepareMessage extends Message {
    // feel free to add other variables
    private long epochNum;

    protected PrepareMessage(){}

    public PrepareMessage(NodeIdentifier sender, long epochNum) {
        super(MSG_TYPE.Prepare, sender);
        this.epochNum = epochNum;
    }

    public long getEpochNum(){
        return epochNum;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(epochNum);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        epochNum = buf.readLong();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PrepareReplyMessage<src=").append(super.getSender())
                .append(" index=").append(epochNum).append(">");

        return sb.toString();
    }
}
