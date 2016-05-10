package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/14/16.
 */
public class LeaderElectionMessage extends Message {


    NodeIdentifier failedLeader;

    protected LeaderElectionMessage() {}

    public LeaderElectionMessage(NodeIdentifier sender, NodeIdentifier failedLeader) {
        super(Message.MSG_TYPE.LeaderElection, sender);
        this.failedLeader = failedLeader;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(failedLeader.hashCode());
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        failedLeader = new NodeIdentifier(buf.readInt());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("LeaderElectionMessage<src=").append(super.getSender())
                .append(" failedleader=").append(failedLeader.getID()).append(">");
        return sb.toString();
    }

    public NodeIdentifier getFailedLeader() {
        return failedLeader;
    }

}
