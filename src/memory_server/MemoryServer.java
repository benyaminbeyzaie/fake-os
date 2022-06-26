package memory_server;


import logger.BenLogger;
import process_execute.ProcessExecute;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MemoryServer {
    public static void main(String[] args) {
        BenLogger.getInstance().log("MemoryServer", "MemoryServer args on run => " + Arrays.toString(args));
        int port = Integer.parseInt(args[0]);
        int[] memoryDatas = new int[args.length - 1];
        for (int i = 0; i < memoryDatas.length; i++) {
            memoryDatas[i] = Integer.parseInt(args[i + 1]);
        }
        new MemoryServer(memoryDatas, port).start();
    }

    // Hash map of integer to integer, This is the memory map
    private final int memoryPort;

    public MemoryServer(int[] memoryDatas, int memoryPort) {
        this.memoryPort = memoryPort;
        // Initialize the memory map
        MemoryManager.init(memoryDatas);
        BenLogger.getInstance().log("MemoryServer", "Memory created with args => " + "memoryPort: " + memoryPort + ", memoryDatas: " + Arrays.toString(memoryDatas));
    }

    public void start() {
        new Thread(() -> {
            try {
                BenLogger.getInstance().log("MemoryServer", "MemoryServer is listening on port => " + memoryPort);
                final ServerSocket serverSocket = new ServerSocket(memoryPort);
                while (true) {
                    final Socket socket = serverSocket.accept();
                    BenLogger.getInstance().log("MemoryServer", "MemoryServer is has new connection on port: " + memoryPort);
                    new Thread(new MemoryClientHandler(socket)).start();
                }
            } catch (IOException e) {
                BenLogger.getInstance().log("MemoryServer", "MemoryServer start failed => " + e.getMessage());
            }
        }).start();
    }

    public static Process runMemoryServerProcess(int port, int[] memoryDatas) throws IOException {
            var args = new ArrayList<String>();
            args.add(Integer.toString(port));
            // add all memory data to args
            for (int memoryData : memoryDatas) {
                args.add(Integer.toString(memoryData));
            }
            return ProcessExecute.exec(MemoryServer.class, args);

    }
}
