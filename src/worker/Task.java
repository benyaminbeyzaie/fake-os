package worker;

import java.util.Arrays;
import java.util.Objects;

/**
 * Each task is followed by a series of readings from memory with some delay between each reading.
 * The goal is to calculate the sum of the numbers in the memory cells.
 * A task after reading the last house of the requested memory calculates the total sum and returns the result.
 * A worker only begins to release the lock after the work is completed. Burst access to a house of memory is also allowed.
 */
public class Task implements Comparable<Task>{
    public final int taskId;
    public final int[] waitTimes;
    public final int[] memoryAddresses;
    public boolean isDone;

    public Task(int taskId, int[] waitTimes, int[] memoryAddresses) {
        this.taskId = taskId;
        this.waitTimes = waitTimes;
        this.memoryAddresses = memoryAddresses;
        this.isDone = false;
    }

    @Override
    public String toString() {
        return "Task{" + "taskId=" + taskId + ", waitTimes=" + Arrays.toString(waitTimes) + ", memoryAddresses=" + Arrays.toString(memoryAddresses) + '}';
    }

    @Override
    public int compareTo(Task o) {
        long totalWaitTime = 0;
        long otherTaskTotalWaitTime = 0;
        for (int waitTime : waitTimes) {
            totalWaitTime += waitTime;
        }
        for (int waitTime : o.waitTimes) {
            otherTaskTotalWaitTime += waitTime;
        }
        if (totalWaitTime < otherTaskTotalWaitTime) return -1;
        else if (totalWaitTime == otherTaskTotalWaitTime) return 0;
        else return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Task task = (Task) o;
        return taskId == task.taskId;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(taskId, isDone);
        result = 31 * result + Arrays.hashCode(waitTimes);
        result = 31 * result + Arrays.hashCode(memoryAddresses);
        return result;
    }
}
