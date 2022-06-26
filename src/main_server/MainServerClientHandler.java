package main_server;

import com.google.gson.Gson;
import logger.BenLogger;
import process_execute.ProcessExecute;
import worker.Task;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainServerClientHandler {
    private final Socket socket;
    private final int id;
    private boolean isBusy;
    private Task currentTask;
    private Scanner scanner;
    PrintWriter printWriter;
    private final DeadLockHandlingMethod deadLockHandlingMethod;

    static Integer endTasksCounter = 0;

    public MainServerClientHandler(Socket socket, int id, DeadLockHandlingMethod deadLockHandlingMethod) {
        this.socket = socket;
        this.id = id;
        isBusy = false;
        this.deadLockHandlingMethod = deadLockHandlingMethod;
    }

    public boolean assignTask(Task task) throws IOException {
        if (scanner == null) {
            scanner = new Scanner(socket.getInputStream());
        }
        isBusy = true;
        currentTask = task;
        Gson gson = new Gson();
        String jsonTask = gson.toJson(task);
        BenLogger.getInstance().log("MainServerClientHandler", "MainServerClientHandler json task: " + jsonTask);
        sendMessage(id + "#=" + jsonTask);
        String response = "possible";
        if (deadLockHandlingMethod == DeadLockHandlingMethod.PREVENTION) {
            response = scanner.nextLine();
        }
        BenLogger.getInstance().log("MainServerClientHandler", "is task possible to do: " + response);
        if (response.equals("possible")) {
            new Thread(() -> {
                String messageFromWorker = scanner.nextLine();
                BenLogger.getInstance().log("Answers", "task " + currentTask.taskId + " executed successfully with result " + messageFromWorker);

                synchronized (endTasksCounter.getClass()) {
                    endTasksCounter += 1;
                }
                isBusy = false;
                currentTask = null;
            }).start();
            return true;
        } else {
            return false;
        }
    }

    public void sendMessage(String message) throws IOException {
        if (printWriter == null) {
            printWriter = new PrintWriter(socket.getOutputStream());
        }
        printWriter.println(message);
        printWriter.flush();
    }


    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }
}
