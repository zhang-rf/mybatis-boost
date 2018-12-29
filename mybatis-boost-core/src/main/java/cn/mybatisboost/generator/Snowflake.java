package cn.mybatisboost.generator;

public class Snowflake {

    private final long epoch;
    private final long workerId;
    private final int timestampShifting;
    private final int workerIdShifting;
    private final long maxSequence;

    private long lastTimestamp;
    private long sequence;

    public Snowflake(long workerId) {
        this(1545825894992L, workerId);
    }

    public Snowflake(long epoch, long workerId) {
        this(epoch, workerId, 10, 12);
    }

    public Snowflake(long epoch, long workerId, int workerIdBits, int sequenceBits) {
        this.epoch = epoch;
        this.workerId = workerId;
        timestampShifting = sequenceBits + workerIdBits;
        workerIdShifting = sequenceBits;
        maxSequence = ~(-1L << sequenceBits);
    }

    public synchronized long next() {
        long timestamp = System.currentTimeMillis() - epoch;
        if (timestamp != lastTimestamp) {
            if (timestamp < lastTimestamp)
                throw new IllegalStateException("Clock moved backwards");
            lastTimestamp = timestamp;
            sequence = 0;
        }
        long id = timestamp << timestampShifting;
        id |= workerId << workerIdShifting;
        if (sequence > maxSequence) {
            Thread.yield();
            return next();
        }
        id |= sequence++;
        return id;
    }
}
