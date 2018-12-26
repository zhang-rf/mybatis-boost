package cn.mybatisboost.generator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Snowflake {

    private long epoch;
    private long identity;
    private int timestampShifting;
    private int identityShifting;
    private long maxSequence;

    private long lastTimestamp;
    private BlockingQueue<Long> idQueue;

    public Snowflake(long epoch, long identity) {
        this(epoch, identity, 10, 12);
    }

    public Snowflake(long epoch, long identity, int identityBits, int sequenceBits) {
        this.epoch = epoch;
        this.identity = identity;
        timestampShifting = sequenceBits + identityBits;
        identityShifting = sequenceBits;
        maxSequence = ~(-1L << sequenceBits);
        idQueue = new ArrayBlockingQueue<>((int) maxSequence + 1);
    }

    public long next() throws InterruptedException {
        long currentTimeMillis = System.currentTimeMillis() - epoch;
        if (currentTimeMillis != lastTimestamp) {
            synchronized (this) {
                if (currentTimeMillis != lastTimestamp) {
                    if (currentTimeMillis < lastTimestamp) {
                        throw new IllegalStateException("Clock moved backwards");
                    }
                    idQueue.clear();
                    long id = currentTimeMillis << timestampShifting;
                    id |= identity << identityShifting;
                    for (int i = 0; i <= maxSequence; i++) {
                        idQueue.offer(id | i);
                    }
                    lastTimestamp = currentTimeMillis;
                }
            }
        }
        Long next = idQueue.poll(1, TimeUnit.MILLISECONDS);
        return next == null ? next() : next;
    }
}
