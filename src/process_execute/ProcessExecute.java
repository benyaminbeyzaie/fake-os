package process_execute;

import logger.BenLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessExecute {
    static ArrayList<Process> processes = new ArrayList<>();
    public static void killAll() {
        BenLogger.getInstance().log("ProcessExecute", "ProcessExecute will destroy all the processes...");
        for (Process p : processes) {
            p.destroyForcibly();
            BenLogger.getInstance().log("ProcessExecute", "Just destroyed a process!");

        }
    } 
    public static Process exec(Class clazz, List<String> runTimeArgs) throws IOException {
        BenLogger.getInstance().log("ProcessExecute", "ProcessExecute is going to start process of class " + clazz.getName());
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getName();

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(runTimeArgs);

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process =  builder.start();
        processes.add(process);
        return process;
    }
}


