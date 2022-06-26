package logger;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class BenLogger {
    private static BenLogger instance = null;
    private HashMap<String, Integer> fileOpenCounter = new HashMap<>();

    private BenLogger() {
    }

    public static BenLogger getInstance() {
        if (instance == null) {
            instance = new BenLogger();
        }
        return instance;
    }

    public synchronized void log(String type, String message) {
        if (!fileOpenCounter.containsKey(type)) {
            fileOpenCounter.put(type, 0);
            basicLog(type, "------------------------------ New Runtime ------------------------------");
            basicLog(type, message);
        } else {
            basicLog(type, message);
        }
    }

    private synchronized void basicLog(String type, String message) {
        try {
            File directory = new File("./");
            final String filePath = directory.getAbsolutePath() + "/logs/" + type + ".log";
            File logFile = new File(filePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
            out.println(getCurrentDate() + ": " + type + " ---> " + message);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}
