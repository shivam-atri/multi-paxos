package paxosProject.messages;

import paxosProject.network.NodeIdentifier;
import io.netty.buffer.ByteBuf;

public class AcceptReplyMessage extends Message {

	// feel free to add other variables
	private long slot;
	private  boolean accepted;
	private NodeIdentifier acceptedLeader;
	protected AcceptReplyMessage(){}
	
	public AcceptReplyMessage(NodeIdentifier sender, long slot, boolean accepted, NodeIdentifier acceptedLeader) {
		super(MSG_TYPE.AcceptReply, sender);
		this.slot = slot;
		this.accepted = accepted;
		this.acceptedLeader = acceptedLeader;
	}
	
	public long getSlot(){
		return slot;
	}

	public boolean isAccepted() { return accepted;}

	public NodeIdentifier getAcceptedLeader() { return  acceptedLeader;}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeLong(slot);
		buf.writeBoolean(accepted);
		buf.writeInt(acceptedLeader.hashCode());
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
        slot = buf.readLong();
		accepted = buf.readBoolean();
		acceptedLeader = new NodeIdentifier(buf.readInt());
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("AcceptReplyMessage<src=").append(super.getSender())
			.append(" slot=").append(slot).append(">");

		return sb.toString();
	}
}
