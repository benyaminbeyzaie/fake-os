package main_server;

import logger.BenLogger;
import process_execute.ProcessExecute;
import worker.Task;
import worker.Worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for starting the workers and running the tasks based on
 * the selected algorithm.
 */
public class MainServer {
    /**
     * The main server maintains a queue of tasks assigned to it along with their status.
     * The server checks its queue every `taskListCheckInterval` millisecond.
     * Then, according to the type of scheduling algorithm that is specified at runtime, one of the three forms FCFS SJF RR
     * starts scheduling and processing.
     */
    // this is the interval of the task checking, milliseconds
    final int taskListCheckInterval = 100;
    final List<Task> tasks;
    final SchedulingAlgorithm schedulingAlgorithms;
    private final DeadLockHandlingMethod deadLockHandlingMethod;
    final int masterPort;
    final int memoryPort;
    final int workersCount;
    final List<MainServerClientHandler> mainServerClientHandlers;
    final String[] commonArgs;
    public static int tasksCount;


    public MainServer(SchedulingAlgorithm schedulingAlgorithms, DeadLockHandlingMethod deadLockHandlingMethod, int masterPort, int memoryPort, int workersCount, String[] commonArgs) {
        this.schedulingAlgorithms = schedulingAlgorithms;
        this.deadLockHandlingMethod = deadLockHandlingMethod;
        this.masterPort = masterPort;
        this.workersCount = workersCount;
        this.commonArgs = commonArgs;
        this.memoryPort = memoryPort;
        tasks = new ArrayList<>();
        mainServerClientHandlers = new ArrayList<>();
        BenLogger.getInstance().log("MainServer", "MainServer created with args => " + "schedulingAlgorithms: " + schedulingAlgorithms + ", " + "deadLockHandlingMethod: " + deadLockHandlingMethod + ", " + "masterPort: " + masterPort + ", " + "workersCount: " + workersCount + ", " + "commonArgs: " + Arrays.toString(commonArgs));
    }

    /**
     * This method is responsible for adding a task to the queue.
     *
     * @param task the task to be added to the queue.
     * @throws IllegalArgumentException if the task is null.
     */
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        BenLogger.getInstance().log("MainServer", "Task added to the queue => " + task);
        tasks.add(task);
    }

    private Task getNextTask(SchedulingAlgorithm schedulingAlgorithms) {
        if (schedulingAlgorithms == SchedulingAlgorithm.SJF) {
            Collections.sort(tasks);
        }
        return tasks.get(0);
    }

    public void run() throws InterruptedException {
        Thread serverSocketThread = new Thread(() -> {
            int i = 0;
            try {
                BenLogger.getInstance().log("MainServer", "MainServer is listening on port: " + masterPort);

                ServerSocket serverSocket = new ServerSocket(masterPort);
                while (true) {
                    Socket socket = serverSocket.accept();
                    BenLogger.getInstance().log("MainServer", "MainServer started a connection");
                    MainServerClientHandler mainServerClientHandler = new MainServerClientHandler(socket, i, deadLockHandlingMethod);
                    mainServerClientHandlers.add(mainServerClientHandler);
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverSocketThread.start();

        // start all the workers processes
        for (int i = 0; i < workersCount; i++) {
            String[] args = new String[4];
            args[0] = i + "";
            args[1] = masterPort + "";
            args[2] = memoryPort + "";
            args[3] = String.valueOf(deadLockHandlingMethod == DeadLockHandlingMethod.NONE ? 0 : 1);
            try {
                BenLogger.getInstance().log("MainServer", "MainServer is starting an process...");
                Process process = ProcessExecute.exec(Worker.class, List.of(args));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (tasks.size() > 0) {
            try {
                Thread.sleep(taskListCheckInterval);
            } catch (InterruptedException e) {
                BenLogger.getInstance().log("MainServer", "MainServer run failed => " + e.getMessage());
            }
            // check if there are any tasks in the queue
            if (tasks.size() > 0) {
                BenLogger.getInstance().log("MainServer", "MainServer will assign one task soon...");

                // get the next task to be executed
                Task task = getNextTask(schedulingAlgorithms);
                // if there is any empty worker assign the task and start
                for (MainServerClientHandler c : mainServerClientHandlers) {
                    if (!c.isBusy()) {
                        try {
                            BenLogger.getInstance().log("MainServer", "MainServer is assigning task to a worker");
                            tasks.remove(task);
                            boolean isAssigned = c.assignTask(task);
                            // add the removed task to the tasks list
                            if (!isAssigned && deadLockHandlingMethod == DeadLockHandlingMethod.PREVENTION)
                                tasks.add(task);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        boolean isThereAnyBusyWorker = true;
        while (isThereAnyBusyWorker) {
            Thread.sleep(100);
            isThereAnyBusyWorker = false;
            for (MainServerClientHandler c : mainServerClientHandlers) {
                if (c.isBusy()) {
                    isThereAnyBusyWorker = true;
                    break;
                }
            }

        }
        BenLogger.getInstance().log("MainServer", "MainServer is shutting down");

        ProcessExecute.killAll();
        System.exit(0);
    }
}
