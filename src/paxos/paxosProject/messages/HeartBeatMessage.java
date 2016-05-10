package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/14/16.
 */
public class HeartBeatMessage extends Message {

    private long index;
    private NodeIdentifier leader;

    protected HeartBeatMessage() {}

    public HeartBeatMessage(NodeIdentifier sender, long index, NodeIdentifier leader) {
        super(MSG_TYPE.HeartBeat, sender);
        this.index = index;
        this.leader = leader;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(index);
        buf.writeInt(leader.hashCode());
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        index = buf.readLong();
        leader = new NodeIdentifier(buf.readInt());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("HearBeat Message <src=").append(super.getSender())
                .append(" index=").append(index).append(">");
        return sb.toString();
    }

    public long getIndex() {
        return index;
    }
}
