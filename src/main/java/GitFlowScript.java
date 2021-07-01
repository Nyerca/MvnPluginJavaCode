import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitFlowScript {
    public static void main(String[] args) {
        try {
            //openFeature("test");
            //closeFeature();
            //readPOMVersion("1.1.20-SNAPSHOT", "MIAO");
            openRelease(RELEASE_TYPE.MINOR, "1.4.0-SNAPSHOT");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public enum RELEASE_TYPE {
        MAJOR,
        MINOR,
        PATCH
    }

    public static void openRelease(RELEASE_TYPE type, String devVersion) throws IOException, InterruptedException {
        String masterVersion = devVersion.split("-SNAPSHOT")[0];
        String newDevVersion = findDevelopNewVersion(type, devVersion);
        System.out.println("MASTER_V: " + masterVersion + " DEV_V: " + newDevVersion);

        executeCommand("git checkout master && git merge develop");
        modifyJenkinsfile("master");
        readPOMVersion(devVersion, masterVersion);
        executeCommand("git add . && git commit -m \"Avvio release " + masterVersion + "\"");
        executeCommand("git tag " + masterVersion + " -m \"Release: " + masterVersion + "\"");
        //git push origin --tags
        //git push
        executeCommand("git checkout develop && git merge master");
        modifyJenkinsfile("develop");
        readPOMVersion(masterVersion, newDevVersion);
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

    public static String findDevelopNewVersion(RELEASE_TYPE type, String oldVersion) {
        String returnVersion;
        String[] splitted = oldVersion.split("\\.");

        if (RELEASE_TYPE.MAJOR.equals(type)) {
            returnVersion = (Integer.parseInt(splitted[0]) + 1) + ".0.0";
        } else if (RELEASE_TYPE.MINOR.equals(type)) {
            returnVersion = splitted[0] + "." + (Integer.parseInt(splitted[1]) + 1) + ".0";
        } else {
            returnVersion = splitted[0] + "." + splitted[1] + "." + (Integer.parseInt(splitted[2]) + 1);
        }
        return returnVersion + "-SNAPSHOT";
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

    public static void readPOMVersion(String pomVersion, String newPomVersion) throws IOException {
        List<String> newLines = new ArrayList<String>();

        for (String line : Files.readAllLines(Paths.get("pom.xml"), StandardCharsets.UTF_8)) {
            if (line.contains("<version>" + pomVersion + "</version>")) {
                newLines.add(line.replace("<version>" + pomVersion + "</version>", "<version>" + newPomVersion + "</version>"));
            } else {
                newLines.add(line);
            }
        }
        Files.write(Paths.get("pom.xml"), newLines, StandardCharsets.UTF_8);
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
