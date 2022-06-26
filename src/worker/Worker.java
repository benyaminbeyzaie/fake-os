package worker;

import com.google.gson.Gson;
import logger.BenLogger;
import main_server.DeadLockHandlingMethod;
import main_server.MainServer;
import memory_server.MemoryManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Worker is a process and if the worker has a task it should run it parallel with other workers and tasks!
 */
public class Worker {
    public static void main(String[] args) {
        BenLogger.getInstance().log("Worker" + args[0], "Worker is starting with args: " + args[0] + ", " + args[1] + ", " + args[2]);
        Worker worker = new Worker(args[0], args[1], args[2], args[3]);
        worker.start();
    }

    public Worker(String id, String masterPort, String memoryPort, String deadlockHandlingMethod) {
        this.memoryPort = Integer.parseInt(memoryPort);
        this.masterPort = Integer.parseInt(masterPort);
        this.id = Integer.parseInt(id);
        if (deadlockHandlingMethod.equals("0")) this.deadLockHandlingMethod = DeadLockHandlingMethod.NONE;
        else this.deadLockHandlingMethod = DeadLockHandlingMethod.PREVENTION;
    }

    final int memoryPort;
    final int masterPort;
    final int id;
    final DeadLockHandlingMethod deadLockHandlingMethod;

    public int doTheTask(Task task) throws InterruptedException, IOException {
        BenLogger.getInstance().log("Worker" + id, "Worker is doing a task...");
        int ans = 0;
        Socket socket = new Socket("localhost", memoryPort);
        Scanner in = new Scanner(socket.getInputStream());
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        for (int i = 0; i < task.waitTimes.length; i++) {
            Thread.sleep(task.waitTimes[i]);
            out.println(id + " obtain " + task.memoryAddresses[i]);
            out.flush();
            String response = in.nextLine();
            BenLogger.getInstance().log("Worker" + id, "got the response: " + i + " --> response: " + response);
            int value = Integer.parseInt(response);
            ans += value;
        }
        BenLogger.getInstance().log("Worker" + id, "Worker will send releaseAll to memory");
        out.println(id + " releaseAll");
        out.flush();
        task.isDone = true;
        BenLogger.getInstance().log("Worker" + id, "Worker has ended a task with answer: " + ans);
        return ans;
    }

    private boolean isPossible(Task task) throws IOException {
        BenLogger.getInstance().log("Worker" + id, "Worker " + id + " is detecting cycle ");

        Socket socket = new Socket("localhost", memoryPort);
        Scanner in = new Scanner(socket.getInputStream());
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(id + " memoryState");
        out.flush();
        String memoryManagerJson = in.nextLine();
        BenLogger.getInstance().log("Worker" + id, "Worker " + id + "has received the memory state: " + memoryManagerJson);

        Gson gson = new Gson();
        MemoryManager memoryManager = gson.fromJson(memoryManagerJson, MemoryManager.class);
        for (int address : task.memoryAddresses) {
            if (!memoryManager.getMemory().get(address).isFree()) {
                BenLogger.getInstance().log("Worker" + id, "Worker " + id + "the task is NOT possible to do...");
                return false;
            }
        }
        BenLogger.getInstance().log("Worker" + id, "Worker " + id + "the task is possible to do...");
        return true;

    }


    public void start() {
        // socket for master connection
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", masterPort);
                while (true) {
                    Scanner scanner = new Scanner(socket.getInputStream());
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    String serverOrder = scanner.nextLine();
                    BenLogger.getInstance().log("Worker" + id, "Worker " + id + " received an order: " + serverOrder);

                    String[] orders = serverOrder.split("#=");
                    int workerId = Integer.parseInt(orders[0]);

                    String jsonTask = orders[1];
                    Gson gson = new Gson();
                    Task task = gson.fromJson(jsonTask, Task.class);
                    BenLogger.getInstance().log("Worker" + id, "DeadLockHandlingMethod is " + deadLockHandlingMethod);

                    if (deadLockHandlingMethod == DeadLockHandlingMethod.PREVENTION) {
                        BenLogger.getInstance().log("Worker" + id, "DeadLockHandlingMethod is PREVENTION");

                        boolean isPossibleToDoTheTask = isPossible(task);
                        if (isPossibleToDoTheTask) {
                            printWriter.println("possible");
                            printWriter.flush();
                            int ans = doTheTask(task);
                            printWriter.println(ans);
                            printWriter.flush();
                        } else {
                            printWriter.println("not possible");
                            printWriter.flush();
                        }
                    } else {
                        BenLogger.getInstance().log("Worker" + id, "DeadLockHandlingMethod is NONE");

                        int ans = doTheTask(task);
                        printWriter.println(ans);
                        printWriter.flush();
                    }
                }

            } catch (IOException | InterruptedException e) {
                BenLogger.getInstance().log("Worker" + id, " has an error: " + e);

                e.printStackTrace();
            }
        }).start();

    }


}
