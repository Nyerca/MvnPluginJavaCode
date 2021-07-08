package it.imola.gitflow.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandManager {
    private static CommandManager commandManager;
    private String projectFolder;
    private boolean isWindows;

    private CommandManager() {
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public static CommandManager getInstance() {
        if (null == commandManager) {
            commandManager = new CommandManager();
        }
        return commandManager;
    }

    /**
     * Metodo di esecuzione di un comando su bat o sh
     *
     * @param command Comando da eseguire
     * @throws IOException
     * @throws InterruptedException
     */
    public void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder().inheritIO();
        command = projectFolder != null ? "cd " + projectFolder + " && " + command : command;
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        Process process = builder.start();
        int exitCode = process.waitFor();
        printResults(process);
    }

    /**
     * Metodo per fare la print del return di un processo lanciato
     *
     * @param process
     * @throws IOException
     */
    public static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }


}
