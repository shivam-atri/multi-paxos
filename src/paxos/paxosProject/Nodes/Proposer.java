package paxosProject.Nodes;
import paxosProject.Configuration;
import paxosProject.DS.KeyValue;
import paxosProject.DS.KeyValueCount;
import paxosProject.DS.trackAccept;
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
public class Proposer implements EventHandler {

    NodeIdentifier myID = null;
    Network network = null;
    Boolean isLeader = false;
    Boolean proposeSuccessful = false;
    int epochNum = -1;
    Boolean fail_reply_heartbeat;
    NodeIdentifier leaderaddress = null;
    int slot;
    long latestheartbeatreplyindex;
    /*
    - D.S. to store replies to prepare message: Will need to merge with all accepted messages
    - Data Structures to store messages for whom accept has been sent
    - D.S. to store replies to accept messages
    */
    HashMap<Long, trackAccept> trackaccept;
    HashMap<Long, Long> acceptedmessages;
    HashMap<Long, Long> sentaccepted;
    HashMap<Long,Integer> replyCountForPrepare;
    HashMap<Long, KeyValueCount> tempaccepted;
    public Proposer(NodeIdentifier id, boolean isLeader, int epochNum){
        myID = id;
        network = new NettyNetwork(myID, this);
        this.isLeader = isLeader;
        this.epochNum = epochNum;
        if(isLeader) {
            leaderaddress = myID;
            sendPrepare();
        } else {
            leaderaddress = Configuration.proposerIDs.get(1);
        }
        replyCountForPrepare = new HashMap<>();
        trackaccept = new HashMap<>();
        acceptedmessages = new HashMap<>();
        sentaccepted = new HashMap<>();
        latestheartbeatreplyindex = -1;
        fail_reply_heartbeat = false;
        startHeartBeat();
    }



    public NodeIdentifier ID() {
        return myID;
    }

    @Override
    public void handleMessage(Message msg) {
        if(msg instanceof PutMessage) {
            handlePutMessage((PutMessage) msg);
        }
        else if (msg instanceof AcceptReplyMessage) {
            handleAcceptReplyMessage((AcceptReplyMessage) msg);
        } else if(msg instanceof PrepareReplyMessage) {
            handlePrepareReplyMessage((PrepareReplyMessage) msg);
        } else if(msg instanceof  HeartBeatMessage) {
            handleHeartBeatMessage((HeartBeatMessage) msg);
        }
        else if(msg instanceof  HeartBeatReplyMessage) {
            handleHeartBeatReplyMessage((HeartBeatReplyMessage) msg);
        } else if(msg instanceof LeaderElectionMessage) {
            handleLeaderElectionMessage((LeaderElectionMessage) msg);
        }
        else{
            System.out.println("Unkown message received by proposer!");
        }
    }

    private void handleLeaderElectionMessage(LeaderElectionMessage msg) {
        electnewLeader(msg.getFailedLeader());
    }

    private void handleHeartBeatMessage(HeartBeatMessage msg) {
     /*
        If I am leader, send reply
     */
        if(isLeader) {
            if(fail_reply_heartbeat) {
                System.out.println("Proposer: " + myID.getID() + " Failed to reply to HeartBeat Message");
                return;
            }
            NodeIdentifier proposer = msg.getSender();
            long index = msg.getIndex();
            HeartBeatReplyMessage message = new HeartBeatReplyMessage(myID, index);
            network.sendMessage(proposer, message);
        }
    }

    private void handleHeartBeatReplyMessage(HeartBeatReplyMessage msg) {
        long index = msg.getIndex();
        latestheartbeatreplyindex = index;
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

    public void handlePutMessage(PutMessage msg) {
        //System.out.println("got a put message with key " + msg.getKey() + " value " + msg.getValue());
        /*
        check if I am the leader else send reject
         */
        if(!isLeader) {
            sendWrongLeaderMessage(msg);
        }
        else if(!proposeSuccessful) {
            // Do Nothing?
        } else {
            /*
            I am the leader and propose has been successful - check if retransmitted message
            */
            if(acceptedmessages.containsKey(msg.getIndex())) {
                System.out.println("Proposer: " + myID.getID() + " got a Put message which has already been accepted!");
                sendLearnMessage(acceptedmessages.get(msg.getIndex()));
             } else if(sentaccepted.containsKey(msg.getIndex())){
                System.out.println("Proposer: " + myID.getID() + " got a Put message for which has accepts have already been sent but which has not be accepted. Resending!");
                 long slot = sentaccepted.get(msg.getIndex());
                 sendAccept(msg,slot);
             } else {
                 sendAccept(msg,-1);
             }
        }
    }

    private void handlePrepareReplyMessage(PrepareReplyMessage msg) {
        /*
        check if promised
         */
        long epochnum = msg.getEpochNum();
        NodeIdentifier leader = msg.getLeader();
        Boolean accepted = msg.wasAccepted();
        HashMap<Long, KeyValue> acceptedslotkeyvalue = msg.getAcceptslotkeyvalue();
        if(!accepted) {
            isLeader = false;
            leaderaddress = leader;
        } else {
            mergeReplies(acceptedslotkeyvalue);
            if (replyCountForPrepare.containsKey(epochnum)) {
                int count = replyCountForPrepare.get(epochnum);
                count = count + 1;
                replyCountForPrepare.put(epochnum, count);
                if (count == Configuration.F + 1) {
                    proposeSuccessful = true;
                    System.out.println("Proposer: " + myID.getID() + " got Promise messages from F + 1: Prepare Successful!");
                    propogateValues();
                }
            } else {
                replyCountForPrepare.put(epochnum, 1);
            }
        }
    }

    private void propogateValues() {
        /*
        Send values in tempaccepted to all acceptors?
         */
        System.out.println("After election New Leader has learnt: ");
        for(Map.Entry<Long, KeyValueCount> slotkeyvalue : tempaccepted.entrySet()) {
            Long slot = slotkeyvalue.getKey();
            KeyValueCount kvc = slotkeyvalue.getValue();
            long key = kvc.getKeyValue().getKey();
            long value = kvc.getKeyValue().getValue();
            System.out.println(slot + " " + key + " " + value);
        }
    }

    private void mergeReplies(HashMap<Long, KeyValue> acceptedslotkeyvalue) {
        /*
        Update slot
         */
        for(Map.Entry<Long, KeyValue> entry : acceptedslotkeyvalue.entrySet()){
            long slot = entry.getKey();
            KeyValue kv = entry.getValue();
            if(this.slot <= slot) {
                this.slot = (int)slot + 1;
            }
            if(tempaccepted.containsKey(slot)) {
                // check if same then update
                KeyValueCount kvc = tempaccepted.get(slot);
                KeyValue k = kvc.getKeyValue();
                if(k.equals(kv)) {
                kvc.setCount(kvc.getCount()+1);
                } else {
                    /*
                    Choose one with higher proposer number
                     */
                    if(k.getProposer().getID() < kv.getProposer().getID()) {
                        KeyValueCount higher= new KeyValueCount(kv,1);
                        tempaccepted.put(slot,higher);
                    }
                }
            } else {
                KeyValueCount kvc= new KeyValueCount(kv,1);
                tempaccepted.put(slot,kvc);
            }
        }
    }

    public void handleAcceptReplyMessage(AcceptReplyMessage msg) {
        /*
        check if rejected
         */
        long slot = msg.getSlot();
        Boolean accepted = msg.isAccepted();
        if(!accepted) {
            isLeader = false;
            leaderaddress = msg.getAcceptedLeader();
            System.out.println("Proposer: " + myID.getID() + "got a accept reject message: Not leader anymore");
        } else {
            trackAccept ta = trackaccept.get(slot);
            ta.setCount(ta.getCount()+1);
            if(ta.getCount()+1 == (Configuration.F +1)) {
                System.out.println("Proposer: " + myID.getID() + " received F+1 accept messages for slot: " + slot + " key: " + ta.getKey());
                acceptedmessages.put(ta.getId(),slot);
                // send learn messages
                sendLearnMessage(slot);
            }
        }
    }

    public void sendLearnMessage(long slot) {
        System.out.println("Proposer: " + myID.getID() + " is sending learn messages for key: " + trackaccept.get(slot).getKey());
        trackAccept ta = trackaccept.get(slot);
         long key = ta.getKey();
         long value = ta.getValue();
         NodeIdentifier client = ta.getClient();
        long mid = ta.getId();
        LearnMessage learnmessage = new LearnMessage(myID,key,value,slot,client,mid);
        for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.acceptorIDs.entrySet()) {
            NodeIdentifier acceptor = entry.getValue();
            network.sendMessage(acceptor,learnmessage);
        }

    }
    public void sendWrongLeaderMessage(PutMessage msg) {
        PutReplyMessage message = new PutReplyMessage(myID, msg.getIndex(),true,leaderaddress, msg.getKey());
        network.sendMessage(msg.getSender(),message);
    }

    public void setIsLeader() {
        isLeader = true;
    }

    public void unsetIsLeader() {
        isLeader = false;
    }

    public void sendPrepare() {
        System.out.println(myID.getID() + " is sending prepare messages.");
        /*
        Clear existing learned and accepted D.S.
         */
        trackaccept = new HashMap<>();
        acceptedmessages = new HashMap<>();
        sentaccepted = new HashMap<>();
        tempaccepted =new HashMap<>();
        /*
        Send a propose message to all acceptors with epochNum
         */
        for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.acceptorIDs.entrySet()) {
            NodeIdentifier acceptor = entry.getValue();
            Message prepareMessage = new PrepareMessage(myID,epochNum);
            network.sendMessage(acceptor,prepareMessage);
        }
        epochNum = epochNum + Configuration.proposerIDs.size();
    }

    public void sendAccept(PutMessage msg, long slot) {
        System.out.println("Proposer: " + myID.getID() + " is sending accept messages for key: " + msg.getKey());
        if(slot == -1) {
            slot = getNextSlot();
        }
        int key = msg.getKey();
        int value = msg.getValue();
        NodeIdentifier client = msg.getSender();
        long id = msg.getIndex();
        trackAccept ta = new trackAccept(key,value,this.ID(),client,0,id);
        trackaccept.put(slot,ta);
        AcceptMessage acceptmessage = new AcceptMessage(myID,key,value,epochNum,slot,client,id);

        for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.acceptorIDs.entrySet()) {
            NodeIdentifier acceptor = entry.getValue();
            network.sendMessage(acceptor,acceptmessage);
        }
        sentaccepted.put(msg.getIndex(),slot);
    }

    public long getNextSlot() {
        return slot++;
    }

    private void startHeartBeat() {

        Thread t = new Thread() {
          public void run() {
              try {
                  Thread.sleep(Configuration.pingTimeout);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              int index = myID.getID();
              while(true) {
                  try {
                        /*
                        Check if I am not the leader then send heartbeat to leader
                         */
                      Thread.sleep(Configuration.pingTimeout);
                      if(!isLeader) {
                          System.out.println("Proposer: " + myID.getID() + " is not leader. Sending HeartBeat message");
                          HeartBeatMessage message = new HeartBeatMessage(myID,index,leaderaddress);
                          network.sendMessage(leaderaddress, message);


                      Thread.sleep(Configuration.pingTimeout);
                        // check if recieved reply else send electnewleadermessage

                          if (!(latestheartbeatreplyindex == index)) {
                              System.out.println("Proposer: " + myID.getID() + "did not get heartbeat reply. ");
                              sendelectnewLeaderMessage(leaderaddress);
                          }
                      }
                      index = index + Configuration.proposerIDs.size();
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }
        };
        t.start();
    }

    public void  sendelectnewLeaderMessage(NodeIdentifier failedleader) {
        // send a electnewleader message to all proposers
        LeaderElectionMessage message = new LeaderElectionMessage(myID, failedleader);
        for(Map.Entry<Integer, NodeIdentifier> entry : Configuration.proposerIDs.entrySet()) {
            NodeIdentifier proposer = entry.getValue();
            network.sendMessage(proposer,message);
        }
    }

    public void electnewLeader(NodeIdentifier failedleader) {
        if(myID.getID() == failedleader.getID()) {
            //isLeader = false;
        }
     if(leaderaddress.getID() == failedleader.getID()) {
         // elect new leader
         NodeIdentifier newLeader = getNextLeader(failedleader);
         leaderaddress = newLeader;
        if(myID.getID() == leaderaddress.getID()) {
            isLeader = true;
            System.out.println("Proposer: " + myID.getID() + " is the new leader");
            sendPrepare();
        }
     }
    }

    public NodeIdentifier getNextLeader(NodeIdentifier previousLeader) {
        int id = previousLeader.getID();
        int nextId = 1 + (id % (Configuration.proposerIDs.size()));
        return Configuration.proposerIDs.get(nextId);
    }

    public void set_fail_reply_HeartBeat() {
        fail_reply_heartbeat = true;
    }

}
