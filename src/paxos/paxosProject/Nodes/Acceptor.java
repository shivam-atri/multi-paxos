package paxosProject.Nodes;
import paxosProject.DS.KeyValue;
import paxosProject.DS.KeyValueCount;
import paxosProject.messages.*;
import paxosProject.network.EventHandler;
import paxosProject.network.NettyNetwork;
import paxosProject.network.Network;
import paxosProject.network.NodeIdentifier;

import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shivam on 4/14/16.
 */
public class Acceptor implements EventHandler {

    NodeIdentifier myID = null;
    Network network = null;
    Boolean fail_to_client;
    long largestacceptedEpoch = -1;
    NodeIdentifier largestacceptedLeader = null;

    /*
    D.S. to store accepted messages
     */
    HashMap<Long, KeyValue> acceptslotkeyvalue;

    /*
    D.S. to store learn messages
     */
    HashMap<Long, KeyValue> learnslotkeyvalue;

    public Acceptor(NodeIdentifier id){
        myID = id;
        network = new NettyNetwork(myID, this);
        acceptslotkeyvalue = new HashMap<>();
        learnslotkeyvalue  =  new HashMap<>();
        fail_to_client = true;
    }

    public NodeIdentifier ID() {
        return myID;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg instanceof AcceptMessage) {
            handleAcceptMessage((AcceptMessage) msg);
        } else if(msg instanceof PrepareMessage) {
            handlePrepareMessage((PrepareMessage) msg);
        } else if(msg instanceof LearnMessage) {
            handleLearnMessage((LearnMessage) msg);
        }
    }

    private void handleLearnMessage(LearnMessage msg) {
         System.out.println("Learner: " + myID.getID() + " got a learn message for key: " + msg.getKey());
         long key = msg.getKey();
         long value = msg.getValue();
         long slot = msg.getSlot();
         long mid = msg.getMid();
         NodeIdentifier client = msg.getClient();
         KeyValue kv = new KeyValue(key,value,largestacceptedLeader,client,mid);
         learnslotkeyvalue.put(slot, kv);
         PutReplyMessage m = new PutReplyMessage(myID, mid, false, largestacceptedLeader, (int) key);
        if(msg.getKey() == 12 && fail_to_client) {
            fail_to_client = false;
            System.out.println("Learner failed to send message to Client for key: 12");
        } else {
            network.sendMessage(client,m);
        }
    }

    private void handleAcceptMessage(AcceptMessage msg) {
        System.out.println("Acceptor: " + myID.getID() + " got a accept message for key: " + msg.getKey());
         long key = msg.getKey();
         long value = msg.getValue();
         long epochNum = msg.getEpochNum();
         long slot = msg.getSlot();
         long mid = msg.getMid();
         NodeIdentifier proposer = msg.getSender();
         NodeIdentifier client = msg.getClient();
        /*
        check to accept or reject
         */
        if(epochNum >= largestacceptedEpoch) {
            largestacceptedEpoch = epochNum;
            largestacceptedLeader = proposer;
            // Accept
            System.out.println("Acceptor: " +myID.getID() + " accepting message");
            KeyValue kv = new KeyValue(key,value,proposer,client,mid);
            acceptslotkeyvalue.put(slot, kv);
            AcceptReplyMessage message = new AcceptReplyMessage(myID, slot, true, largestacceptedLeader);
            network.sendMessage(proposer, message);
            if(myID.getID() == 2) {
                 System.out.println("Acceptor 2: Failed to reply to accept message");
            } else {
                network.sendMessage(proposer, message);
            }

        } else {
            System.out.println("Acceptor: " +myID.getID() + " rejecting message");
            // reject
            AcceptReplyMessage message = new AcceptReplyMessage(myID, slot, false, largestacceptedLeader);
            network.sendMessage(proposer, message);
        }
    }

    @Override
    public void handleTimer() {
    }

    @Override
    public void handleFailure(NodeIdentifier node, Throwable cause) {
        if (cause instanceof ClosedChannelException) {
            System.out.printf("%s handleFailure get %s\n", myID, cause);
        }
    }

    public void handlePrepareMessage(PrepareMessage msg) {
        System.out.println("Acceptor: " + myID.getID() + " got a prepare message with Proposal Number: " + msg.getEpochNum());
        long epochnum = msg.getEpochNum();
        NodeIdentifier proposer = msg.getSender();
        if(epochnum > largestacceptedEpoch) {
            /*
            yes I promise
             */
            if(myID.getID() == 1) {
                System.out.println("Acceptor 1 failed to reply to prepare message");
            } else {
                largestacceptedEpoch = epochnum;
                largestacceptedLeader = proposer;
                PrepareReplyMessage message = new PrepareReplyMessage(myID,largestacceptedEpoch,true,largestacceptedLeader,acceptslotkeyvalue);
                network.sendMessage(proposer,message);
            }
        } else {
            /*
            I reject
             */
            PrepareReplyMessage message = new PrepareReplyMessage(myID,largestacceptedEpoch,false,largestacceptedLeader,acceptslotkeyvalue);
            network.sendMessage(proposer,message);
        }
    }


}
