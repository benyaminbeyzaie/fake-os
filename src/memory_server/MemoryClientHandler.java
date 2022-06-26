package memory_server;

import com.google.gson.Gson;
import logger.BenLogger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MemoryClientHandler implements Runnable {
    final Socket socket;

    public MemoryClientHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            while (true) {
                String command = in.nextLine();
                String[] commands = command.split(" ");
                int id = Integer.parseInt(commands[0]);
                String order = commands[1];
                int memoryIndex = -1;
                if (commands.length > 2) {
                    memoryIndex = Integer.parseInt(commands[2]);
                }
                BenLogger.getInstance().log("MemoryClientHandler", "MemoryClientHandler-" + id + " command => " + command);


                switch (order) {
                    case "write":
                        int finalMemoryIndex = memoryIndex;
                        new Thread(() -> {
                            try {
                                MemoryManager.getInstance().write(id, finalMemoryIndex, Integer.parseInt(commands[3]));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                        break;
                    case "obtain":
                        int finalMemoryIndex2 = memoryIndex;
                        new Thread(() -> {
                            try {
                                int ans = MemoryManager.getInstance().obtainMemory(id, finalMemoryIndex2);
                                out.println(ans);
                                out.flush();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                        break;
                    case "release":
                        int finalMemoryIndex1 = memoryIndex;
                        new Thread(() -> {
                            MemoryManager.getInstance().release(id, finalMemoryIndex1);
                        }).start();

                        break;
                    case "releaseAll":
                        new Thread(() -> {
                            MemoryManager.getInstance().releaseAll(id);
                        }).start();

                        break;
                    case "memoryState":
                        new Thread(() -> {
                            var memoryState =  MemoryManager.getInstance().getMemoryState();
                            Gson gson = new Gson();
                            var memoryStateJson =  gson.toJson(memoryState);
                            BenLogger.getInstance().log("MemoryClientHandler", "memory state: " + memoryStateJson);
                            out.println(memoryStateJson);
                            out.flush();
                        }).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
