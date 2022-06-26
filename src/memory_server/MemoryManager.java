package memory_server;

import logger.BenLogger;

import java.util.HashMap;
import java.util.List;

/**
 * MemoryManager is a singleton class that manages the memory.
 */
public class MemoryManager {
    private static MemoryManager instance = null;

    private MemoryManager() {
        // Singleton
    }

    public static MemoryManager init(int[] datas) {
        if (instance == null) {
            instance = new MemoryManager(datas);
        }
        return instance;
    }

    public static MemoryManager getInstance() {
        return instance;
    }

    private HashMap<Integer, MemoryDataHolder> memory;
    private Object[] locks;

    public MemoryManager(int[] memoryDatas) {
        memory = new HashMap<>();
        locks = new Object[memoryDatas.length];
        for (int i = 0; i < memoryDatas.length; i++) {
            memory.put(i, new MemoryDataHolder(i, memoryDatas[i]));
            locks[i] = new Object();
        }
    }

    public synchronized MemoryManager getMemoryState() {
        return this;
    }

    public int obtainMemory(int processId, int index) throws InterruptedException {
        synchronized (locks[index]) {
            BenLogger.getInstance().log("MemoryManager", "processId:" + processId + " is obtaining index:" + index);
            if (!memory.get(index).isFree() && processId != memory.get(index).getLockId()) {
                BenLogger.getInstance().log("MemoryManager", "index:" + index + " is not free and processId:" + processId + " will wait...");
                locks[index].wait();
            }
            memory.get(index).setFree(false);
            memory.get(index).setLockId(processId);
            BenLogger.getInstance().log("MemoryManager", "processId:" + processId + " has obtained index:" + index);

            return memory.get(index).getValue();
        }
    }

    public void release(int processId, int index) {
        synchronized (locks[index]) {
            if (!memory.get(index).isFree()) {
                if (memory.get(index).getLockId() == processId) {
                    BenLogger.getInstance().log("MemoryManager", "processId:" + processId + " is releasing index:" + index);
                    locks[index].notify();
                    memory.get(index).setFree(true);
                    memory.get(index).setLockId(-1);
                    BenLogger.getInstance().log("MemoryManager", "processId:" + processId + " has released index:" + index);
                } else {
                    BenLogger.getInstance().log("MemoryManager", "processId:" + processId + " can't release index:" + index);
                }
            }
        }
    }

    public void releaseAll(int processId) {
        BenLogger.getInstance().log("MemoryManager", "processId:" + processId + " will release all resources...");
        for (int i = 0; i < memory.size(); i++) {
            if (memory.get(i).getLockId() == processId) {
                synchronized (locks[i]) {
                    memory.get(i).setFree(true);
                    memory.get(i).setLockId(-1);
                    locks[i].notify();
                }
            }
        }
        BenLogger.getInstance().log("MemoryManager", "processId:" + processId + " has released all the resources!");
    }

    public synchronized void write(int lockId, int index, int value) throws InterruptedException {
        if (!memory.get(index).isFree()) {
            locks[lockId].wait();
        }
        if (memory.get(index).getLockId() == lockId) {
            memory.get(index).setValue(value);
        }
    }

    public HashMap<Integer, MemoryDataHolder> getMemory() {
        return memory;
    }

    public Object[] getLocks() {
        return locks;
    }
}
