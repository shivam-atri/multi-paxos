package paxosProject.messages;

import paxosProject.network.NodeIdentifier;
import io.netty.buffer.ByteBuf;

public class Message {

    // please feel free to add new types of messages
    public enum MSG_TYPE {
	    Prepare, PrepareReply,
	    Accept, AcceptReply,
		Put, Get,
		HeartBeat,
		LeaderElection,
		Learn, PutReply, HeartBeatReply

    }
	
	private int type;
	private NodeIdentifier sender;
	
	protected Message(){}
	
	public Message(MSG_TYPE msgType, NodeIdentifier sender){
		this.type = msgType.ordinal();
		this.sender = sender;
	}

    public int getType() {
        return type;
    }

	public void setType(int t) {
		type = t;
	}

	public NodeIdentifier getSender(){
		return sender;
	}

	public void setSender(int hashCode){
		sender = new NodeIdentifier(hashCode);
	}

	public NodeIdentifier.Role getSenderRole(){
		return sender.getRole();
	}
	
	public int getSenderID(){
		return sender.getID();
	}
	
	public void serialize(ByteBuf buf){
		buf.writeInt(type);
		buf.writeInt(sender.hashCode());
	}
	
	public void deserialize(ByteBuf buf){
		type = buf.readInt();
		sender = new NodeIdentifier(buf.readInt());
	}
	
	public static Message deserializeRaw(ByteBuf buf){
		Message ret;
		buf.markReaderIndex();
		int type = buf.readInt();
		buf.resetReaderIndex();
        //System.out.println("call deserializeRaw with type: " + MSG_TYPE.values()[type] + "\n");
		switch(MSG_TYPE.values()[type]){
			case Prepare:
				ret = new PrepareMessage();
				break;
			case PrepareReply:
				ret = new PrepareReplyMessage();
				break;
			case Accept:
				ret = new AcceptMessage();
				break;
			case AcceptReply:
				ret = new AcceptReplyMessage();
				break;
			case Put:
				ret = new PutMessage();
				break;
			case Get:
				ret = new GetMessage();
				break;
			case HeartBeat:
				ret = new HeartBeatMessage();
				break;
			case HeartBeatReply:
				ret = new HeartBeatReplyMessage();
				break;
			case LeaderElection:
				ret = new LeaderElectionMessage();
				break;
			case PutReply:
				ret = new PutReplyMessage();
				break;
			case Learn:
				ret = new LearnMessage();
				break;
			default:
				throw new RuntimeException("Unknown msg type "+type);
		}
		ret.deserialize(buf);
		return ret;
	}
}
