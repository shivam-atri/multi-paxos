package paxosProject.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.DS.KeyValue;
import paxosProject.network.NodeIdentifier;

import java.util.HashMap;
import java.util.Map;

public class PrepareReplyMessage extends Message {
    private long epochNum;
    private Boolean accepted;
    private NodeIdentifier acceptedLeader;
    private HashMap<Long, KeyValue> acceptslotkeyvalue = new HashMap<>();
    protected PrepareReplyMessage(){}

    public PrepareReplyMessage(NodeIdentifier sender, long epochNum, Boolean accepted, NodeIdentifier acceptedLeader, HashMap<Long, KeyValue> acceptsslotjeyvalye) {
        super(MSG_TYPE.PrepareReply, sender);
        this.epochNum = epochNum;
        this.accepted = accepted;
        this.acceptedLeader = acceptedLeader;
        this.acceptslotkeyvalue = acceptsslotjeyvalye;
    }

    public long getEpochNum(){
        return epochNum;
    }

    public boolean wasAccepted() { return accepted; }

    public NodeIdentifier getLeader() {return acceptedLeader;}

    public HashMap<Long, KeyValue> getAcceptslotkeyvalue() {
        return acceptslotkeyvalue;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(epochNum);
        buf.writeBoolean(accepted);
        buf.writeInt(acceptedLeader.hashCode());
        int size = acceptslotkeyvalue.size();
        buf.writeInt(size);
        for(Map.Entry<Long, KeyValue> entry : acceptslotkeyvalue.entrySet()){
            Long slot = entry.getKey();
            KeyValue kv = entry.getValue();
            long key = kv.getKey();
            long value = kv.getValue();
            long mid = kv.getMid();
            NodeIdentifier client = kv.getClient();
            NodeIdentifier proposer = kv.getProposer();
            buf.writeLong(slot);
            buf.writeLong(key);
            buf.writeLong(value);
            buf.writeLong(mid);
            buf.writeInt(client.hashCode());
            buf.writeInt(proposer.hashCode());
        }
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        epochNum = buf.readLong();
        accepted = buf.readBoolean();
        acceptedLeader = new NodeIdentifier(buf.readInt());
        acceptslotkeyvalue = new HashMap<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            long slot = buf.readLong();
            long key = buf.readLong();
            long value = buf.readLong();
            long mid = buf.readLong();
            NodeIdentifier client = new NodeIdentifier(buf.readInt());
            NodeIdentifier proposer = new NodeIdentifier(buf.readInt());
            KeyValue kv = new KeyValue(key,value,proposer,client,mid);
            acceptslotkeyvalue.put(slot, kv);
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PrepareReplyMessage<src=").append(super.getSender())
                .append(" index=").append(epochNum).append(">");

        return sb.toString();
    }
}
