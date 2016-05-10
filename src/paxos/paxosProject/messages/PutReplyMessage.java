package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/21/16.
 */
public class PutReplyMessage extends Message {
    long mid;
    Boolean wrongLeader;
    NodeIdentifier leader;

    public int getKey() {
        return key;
    }

    int key;
    protected PutReplyMessage(){}

    public PutReplyMessage(NodeIdentifier sender, long mid, Boolean wrongLeader, NodeIdentifier leader, int key) {
        super(MSG_TYPE.PutReply, sender);
        this.mid = mid;
        this.wrongLeader = wrongLeader;
        this.leader = leader;
        this.key = key;
    }

    public long getMid(){
        return mid;
    }
    public boolean wrongLeader() {return wrongLeader;}
    public NodeIdentifier getLeader() {return  leader;}
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(mid);
        buf.writeBoolean(wrongLeader);
        buf.writeInt(leader.hashCode());
        buf.writeInt(key);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        mid = buf.readLong();
        wrongLeader = buf.readBoolean();
        leader = new NodeIdentifier(buf.readInt());
        key = buf.readInt();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PutReply<src=").append(super.getSender())
                .append(" messageindex=").append(mid).append(">");

        return sb.toString();
    }
}
