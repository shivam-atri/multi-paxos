package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

public class AcceptMessage extends Message {
    // feel free to add other variables
    private long key;
    private long value;
    private long epochNum;
    private long slot;
    private long mid;
    private  NodeIdentifier client;
    protected AcceptMessage(){}

    public AcceptMessage(NodeIdentifier sender, long key, long value, long epochNum, long slot, NodeIdentifier client, long mid) {
        super(MSG_TYPE.Accept, sender);
        this.key = key;
        this.value = value;
        this.epochNum = epochNum;
        this.slot = slot;
        this.client = client;
        this.mid = mid;
    }

    public long getKey(){
        return key;
    }

    public long getValue() { return value;}

    public long getEpochNum() { return epochNum;}

    public long getSlot() {return slot;}

    public NodeIdentifier getClient() {return client;}

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(key);
        buf.writeLong(value);
        buf.writeLong(epochNum);
        buf.writeLong(slot);
        buf.writeLong(mid);
        buf.writeInt(client.hashCode());
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        key = buf.readLong();
        value = buf.readLong();
        epochNum = buf.readLong();
        slot = buf.readLong();
        mid = buf.readLong();
        client = new NodeIdentifier(buf.readInt());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("AcceptMessage<src=").append(super.getSender())
                .append(" slot=").append(slot).append(">");
        return sb.toString();
    }

    public long getMid() {
        return mid;
    }
}
