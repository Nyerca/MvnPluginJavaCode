import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitFlowScript {
    public static void main(String[] args) {
        try {
            //openFeature("test");
            //closeFeature();
            executeCommand("git push");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static String getCurrentGitBranch() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
        process.waitFor();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        return reader.readLine();
    }

    public static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public static void openRelease(String version) throws IOException, InterruptedException {
        executeCommand("git checkout master && git merge develop");
        modifyJenkinsfile("master");
        //MODIFY pom(TAG)
        executeCommand("git add . && git commit -m \"Avvio release " + version +"\"");
        executeCommand("git tag " + version + " -m \"Release: " + version +"\"");
        //git push origin --tags
        //git push
        executeCommand("git checkout develop && git merge master");
        modifyJenkinsfile("develop");
        //MODIFY pom(TAG)
        executeCommand("git add . && git commit -m \"Merge con master e aggiornamento versione\"");
        //git push
    }


    public static void openFeature(String name) throws IOException, InterruptedException {
        executeCommand("git checkout -b feature/" + name);
        modifyJenkinsfile("feature/" + name);
        executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    public static void mergeFeature() throws IOException, InterruptedException {
        String name = getCurrentGitBranch();
        System.out.println("FEATURE: git checkout develop && git merge " + name);
        executeCommand("git checkout develop && git merge " + name);
        modifyJenkinsfile("develop");
        executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    public static void closeFeature() throws IOException, InterruptedException {
        String name = getCurrentGitBranch();
        mergeFeature();
        executeCommand("git branch -D " + name);
    }

    public static void executeCommand(String command) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder().inheritIO();
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }
        Process process = builder.start();
        int exitCode = process.waitFor();
        printResults(process);
    }
/*
    static CompletableFuture<String> readOutStream(InputStream is) {
        return CompletableFuture.supplyAsync(() -> {
            try (
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                StringBuilder res = new StringBuilder();
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    res.append(inputLine).append(System.lineSeparator());
                }
                return res.toString();
            } catch (Throwable e) {
                throw new RuntimeException("problem with executing program", e);
            }
        });
    }

    public static void executeFutureCommand(String command) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(command);
        CompletableFuture<String> soutFut = readOutStream(p.getInputStream());
        CompletableFuture<String> serrFut = readOutStream(p.getErrorStream());
        CompletableFuture<String> resultFut =
                soutFut.thenCombine(serrFut, (stdout, stderr) -> {
                    // print to current stderr the stderr of process and return the stdout
                    System.err.println(stderr);

                    return stdout;
                });
// get stdout once ready, blocking
        try {
            String result = resultFut.get();
            System.out.println(result);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
*/

    public static void modifyJenkinsfile(String branch) throws IOException {
        List<String> newLines = new ArrayList<String>();

        for (String line : Files.readAllLines(Paths.get("Jenkinsfile.txt"), StandardCharsets.UTF_8)) {
            if (line.contains("mpl")) {

                Pattern p = Pattern.compile("mpl@\\S*'", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(line);
                String result = m.replaceAll("mpl@" + branch + "'");
                newLines.add(result);
            } else {
                newLines.add(line);
            }
        }
        Files.write(Paths.get("Jenkinsfile.txt"), newLines, StandardCharsets.UTF_8);
    }
}
