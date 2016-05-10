package paxosProject.DS;

import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/23/16.
 */
public class KeyValue {
    long key;
    long value;
    long mid;
    NodeIdentifier proposer;
    NodeIdentifier client;


    public KeyValue(long key, long value, NodeIdentifier proposer, NodeIdentifier client, long mid) {
        this.key = key;
        this.value = value;
        this.proposer = proposer;
        this.client = client;
        this.mid = mid;
    }


    public long getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }

    public long getMid() {
        return mid;
    }

    public NodeIdentifier getProposer() {
        return proposer;
    }

    public NodeIdentifier getClient() {
        return client;
    }

    public Boolean equals(KeyValue kv) {
        return (kv.getKey() == key && kv.getValue()== value && kv.getMid() == mid);
    }
}
