package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/14/16.
 */
public class PutMessage extends Message {
    // feel free to add other variables
    private long index;
    private int key;
    private int value;

    protected PutMessage(){}

    public PutMessage(NodeIdentifier sender, long index, int key, int value) {
        super(MSG_TYPE.Put, sender);
        this.index = index;
        this.key = key;
        this.value = value;
    }

    public long getIndex(){
        return index;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(index);
        buf.writeInt(key);
        buf.writeInt(value);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        index = buf.readLong();
        key = buf.readInt();
        value = buf.readInt();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Put Message: <src=").append(super.getSender())
                .append(" index=").append(index).append(" key=").append(key).append(" value=").append(value).append(">");

        return sb.toString();
    }
}
