package paxosProject.Nodes;
import paxosProject.Configuration;
import paxosProject.DS.Retransmittedmessage;
import paxosProject.messages.PutMessage;
import paxosProject.messages.PutReplyMessage;
import paxosProject.network.EventHandler;
import paxosProject.network.NettyNetwork;
import paxosProject.network.Network;
import paxosProject.network.NodeIdentifier;
import paxosProject.messages.Message;
import java.nio.channels.ClosedChannelException;
import java.util.HashSet;
import java.util.concurrent.DelayQueue;

/**
 * Created by shivam on 4/14/16.
 */
public class Client implements EventHandler{

    NodeIdentifier myID = null;
    Network network = null;
    long index;
    NodeIdentifier leader = null;
    HashSet<Long> receivedReply = null;
    DelayQueue<Retransmittedmessage> dq;
    public Client(NodeIdentifier id, int leaderid){
        myID = id;
        network = new NettyNetwork(myID, this);
        index = 0;
        receivedReply = new HashSet<>();
        dq = new DelayQueue<>();
        leader = Configuration.proposerIDs.get(leaderid);
        startRetranmissionJob();
    }


    public NodeIdentifier ID() {
        return myID;
    }

    @Override
    public void handleMessage(Message msg) {
        if(msg instanceof PutReplyMessage) {
            handlePutReplyMessage((PutReplyMessage) msg);
        } else {
            System.out.printf("unknown message received by client");
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
    public void put(int key, int value) {
        System.out.println("Client sending put message with key: " + key + " & value: " + value);
        /*
        Add message to some sort of queue & wait timeout time
         */
        long msgindex = ++index;
        PutMessage message = new PutMessage(myID,msgindex,key,value);
        transmitPutMessage(message);
    }

    public void transmitPutMessage(PutMessage msg) {
        Retransmittedmessage message = new Retransmittedmessage(msg, Configuration.pingTimeout);
        dq.add(message);
        network.sendMessage(leader, msg);
    }

    public void handlePutReplyMessage(PutReplyMessage msg) {
        /*
        Check if its not wrong leader - if yes, then update leader address
         */
        long index = msg.getMid();
        if(msg.wrongLeader() && (leader.getID() != msg.getLeader().getID())) {
            System.out.println("Client sent message to wrong leader. New leader is: " + msg.getLeader());
            leader = msg.getLeader();
        } else {
        /*
        Add to received queue
         */
            if(!receivedReply.contains(index)) {
                System.out.println("Client got reply from learner for key: " + msg.getKey());
                receivedReply.add(index);
            }
        }
    }

    private void startRetranmissionJob() {
        Thread t = new Thread() {
            public void run() {
                // check if the queue has some job
                while (true) {
                    try {
                        Thread.sleep(Configuration.pingTimeout);
                        try {
                            Retransmittedmessage msg = dq.take();
                            PutMessage message = msg.getMessage();
                            if (!receivedReply.contains(message.getIndex())) {
                                System.out.println("Client did not receive learnt message for key: " + message.getKey() + " .Retransmittied it.");
                                transmitPutMessage(message);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        t.start();
    }

}
