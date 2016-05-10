package paxosProject.DS;

import paxosProject.network.NodeIdentifier;

/**
 * Created by shivam on 4/24/16.
 */
public class KeyValueCount {
    KeyValue kv;
    int count;


    public KeyValue getKeyValue() {
        return kv;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public KeyValueCount(KeyValue kv, int count) {
        this.kv = kv;
        this.count = count;
    }



}
