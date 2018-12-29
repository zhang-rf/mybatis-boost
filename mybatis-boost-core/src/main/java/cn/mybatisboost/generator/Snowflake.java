package cn.mybatisboost.generator;

public class Snowflake {

    private final long epoch;
    private final long identity;
    private final int timestampShifting;
    private final int identityShifting;
    private final long maxSequence;

    private long lastTimestamp;
    private long sequence;

    public Snowflake(long epoch, long identity) {
        this(epoch, identity, 10, 12);
    }

    public Snowflake(long epoch, long identity, int identityBits, int sequenceBits) {
        this.epoch = epoch;
        this.identity = identity;
        timestampShifting = sequenceBits + identityBits;
        identityShifting = sequenceBits;
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
        id |= identity << identityShifting;
        if (sequence > maxSequence) {
            Thread.yield();
            return next();
        }
        id |= sequence++;
        return id;
    }
}
