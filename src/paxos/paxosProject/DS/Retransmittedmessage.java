package paxosProject.DS;

import paxosProject.messages.PutMessage;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by shivam on 4/24/16.
 */
public class Retransmittedmessage implements Delayed {
    PutMessage msg;
    long startTime;

    public Retransmittedmessage(PutMessage msg, long delay) {
        this.msg = msg;
        this.startTime = System.currentTimeMillis() + delay;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((Retransmittedmessage) o).startTime) {
            return -1;
        }
        if (this.startTime > ((Retransmittedmessage) o).startTime) {
            return 1;
        }
        return 0;
    }

    public PutMessage getMessage() {
        return msg;
    }
}
