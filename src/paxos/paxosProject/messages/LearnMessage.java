package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/23/16.
 */
public class LearnMessage extends Message{
    private long key;
    private long value;
    private long slot;
    private long mid;
    private NodeIdentifier client;
    protected LearnMessage(){}

    public LearnMessage(NodeIdentifier sender, long key, long value, long slot, NodeIdentifier client, long mid) {
        super(Message.MSG_TYPE.Learn, sender);
        this.key = key;
        this.value = value;
        this.slot = slot;
        this.client = client;
        this.mid = mid;
    }

    public long getKey(){
        return key;
    }

    public long getValue() { return value;}


    public long getSlot() {return slot;}

    public NodeIdentifier getClient() {return client;}

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(key);
        buf.writeLong(value);
        buf.writeLong(slot);
        buf.writeLong(mid);
        buf.writeInt(client.hashCode());
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        key = buf.readLong();
        value = buf.readLong();
        slot = buf.readLong();
        mid = buf.readLong();
        client = new NodeIdentifier(buf.readInt());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("LearnMessage<src=").append(super.getSender())
                .append(" slot=").append(slot).append(">");
        return sb.toString();
    }

    public long getMid() {
        return mid;
    }
}
