package paxosProject.DS;

import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/23/16.
 */
public class trackAccept {
    long key;
    long value;
    NodeIdentifier proposer;
    NodeIdentifier client;
    long count;
    long id;


    public trackAccept(long key, long value, NodeIdentifier proposer, NodeIdentifier client, long count, long id) {
        this.key = key;
        this.value = value;
        this.proposer = proposer;
        this.client = client;
        this.count = count;
        this.id = id;
    }


    public long getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }

    public NodeIdentifier getProposer() {
        return proposer;
    }

    public NodeIdentifier getClient() {
        return client;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getId() {
        return id;
    }

}
