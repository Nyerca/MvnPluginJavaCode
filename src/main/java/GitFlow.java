import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitFlow {
    public static void main(String[] args) {
        try {
            String branch = getCurrentGitBranch();

            if(branch.contains("release")) {
                branch = "master";
            }
            System.out.println("Current branch: " + branch);
            /*
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

            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                builder.command("cmd.exe", "/c", "git add . && git commit -m \"Modifica versione pom\"");
            } else {
                builder.command("sh", "-c", "git add . && git commit -m \"Modifica versione pom\"");
            }
            Process process = builder.start();
            int exitCode = process.waitFor();
            printResults(process);
            */


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
}
