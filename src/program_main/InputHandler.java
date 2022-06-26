package program_main;

import main_server.DeadLockHandlingMethod;
import main_server.MainServer;
import main_server.SchedulingAlgorithm;
import memory_server.MemoryServer;
import worker.Task;

import java.io.IOException;
import java.util.Scanner;

/**
 * Use to get the input and create the main server of the program
 * example of input:
 * 3 // java common args java
 * java
 * -classpath
 * bin/oshw2/
 * 8000 // master port
 * 2 // number of workers FCFS //scheduling algorithm NONE // deadlock handling 8001 // storage port
 * 1 2 3 0 // storage data
 * 2 // number of tasks
 * 50 0 50 1 150 2 // task 0
 * 0 1 100 2 100 3 // task 1
 */


public class InputHandler {
    final Scanner scanner;

    InputHandler() {
        scanner = new Scanner(System.in);
    }

    static String[] staticCommonArgs = new String[] {"/usr/bin/java", "-classpath", "build/classes/java/main/"};

    public void readInputsAndStartProgram() throws IOException, InterruptedException {
        //System.out.println("Starting the program...");
        int commonArgsCount = scanner.nextInt();
        String[] commonArgs = new String[commonArgsCount];
        for (int i = 0; i < commonArgsCount; i++) {
            commonArgs[i] = scanner.next();
        }
        int masterPort = scanner.nextInt();
        int workersCount = scanner.nextInt();
        String schedulingAlgorithmString = scanner.next();
        SchedulingAlgorithm schedulingAlgorithm = SchedulingAlgorithm.valueOf(schedulingAlgorithmString);
        if (schedulingAlgorithm == SchedulingAlgorithm.RR) {
            // Ignoring the time of RR
            scanner.next();
        }
        String deadLockHandlingMethodString = scanner.next();
        System.out.println(deadLockHandlingMethodString);
        DeadLockHandlingMethod deadLockHandlingMethod = DeadLockHandlingMethod.valueOf(deadLockHandlingMethodString);
        int storagePort = scanner.nextInt();
        MainServer mainServer = new MainServer(schedulingAlgorithm, deadLockHandlingMethod, masterPort, storagePort, workersCount, staticCommonArgs);

        scanner.nextLine();
        String storageDataString = scanner.nextLine();
        String[] storageDataArray = storageDataString.split(" ");
        int[] storageData = new int[storageDataArray.length];
        for (int i = 0; i < storageDataArray.length; i++) {
            storageData[i] = Integer.parseInt(storageDataArray[i]);
        }
        // Start the memory server
        MemoryServer.runMemoryServerProcess(storagePort, storageData);

        int numberOfTasks = Integer.parseInt(scanner.nextLine());
        MainServer.tasksCount = numberOfTasks;
        for (int i = 0; i < numberOfTasks; i++) {
            String taskString = scanner.nextLine();
            String[] taskArray = taskString.split(" ");
            int[] waitTimes = new int[taskArray.length / 2];
            int[] memoryAddresses = new int[taskArray.length / 2];
            for (int j = 0; j < taskArray.length; j++) {
                if (j % 2 == 0) {
                    waitTimes[j / 2] = Integer.parseInt(taskArray[j]);
                } else {
                    memoryAddresses[j / 2] = Integer.parseInt(taskArray[j]);
                }
            }
            Task task = new Task(i, waitTimes, memoryAddresses);
            mainServer.addTask(task);
        }
        // start the main server
        mainServer.run();
    }

}
