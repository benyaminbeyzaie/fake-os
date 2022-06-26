package memory_server;

public class MemoryDataHolder {
    private final int id;
    private int value;
    private boolean isFree;
    private int lockId;

    public MemoryDataHolder(int id, int value) {
        this.id = id;
        this.value = value;
        this.isFree = true;
        this.lockId = -1;
    }

    public synchronized boolean isFree() {
        return isFree;
    }

    public synchronized void setFree(boolean free) {
        isFree = free;
    }

    public synchronized int getLockId() {
        return lockId;
    }

    public synchronized void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public synchronized int getValue() {
        return value;
    }

    public synchronized void setValue(int value) {
        this.value = value;
    }
}
