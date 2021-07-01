import model.Project;

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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class GitFlowScript {
    public static void main(String[] args) {

        if(args.length > 0) {
            System.out.println(args[0]);
            GIT_FUN funzione = asMyEnum(args[0]);
            if(funzione != null) {
                try {
                switch(funzione) {
                    case feature_start:
                        if(args.length > 1) {
                            openFeature(args[1]);
                        } else {
                            System.out.println("**** INSERIRE IL NOME DELLA FEATURE DA CREARE ****");
                        }
                        break;
                    case feature_merge:
                        mergeFeature();
                        break;
                    case feature_merge_close:
                        closeFeature();
                        break;
                    case release_start_close:
                        if(args.length > 2) {
                            RELEASE_TYPE release = asMyEnumRelease(args[1]);
                            if(release != null) {
                                openRelease2(release, args[2]);
                            } else {
                                System.out.println("**** TIPO RELEASE NON ESISTENTE ****");
                            }
                        } else {
                            System.out.println("**** INSERIRE IL TIPO DI RELEASE DA CREARE E LA VERSIONE DI DEVELOP ****");
                        }
                        break;
                }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JAXBException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("**** FUNZIONE INSERITA NON ESISTENTE ****");
            }
        } else {
            System.out.println("**** INSERIRE IL TIPO DI FUNZIONE DA RICHIAMARE ****");
        }


    }

    public enum RELEASE_TYPE {
        MAJOR,
        MINOR,
        PATCH
    }

    public enum GIT_FUN {
        feature_start,
        feature_merge,
        feature_merge_close,
        release_start_close
    }

    public static RELEASE_TYPE asMyEnumRelease(String str) {
        for (RELEASE_TYPE me : RELEASE_TYPE.values()) {
            if (me.name().equalsIgnoreCase(str))
                return me;
        }
        return null;
    }

    public static GIT_FUN asMyEnum(String str) {
        for (GIT_FUN me : GIT_FUN.values()) {
            if (me.name().equalsIgnoreCase(str))
                return me;
        }
        return null;
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

    public static void openRelease2(RELEASE_TYPE type, String devVersion) throws IOException, InterruptedException, JAXBException {

        executeCommand("git checkout master && git merge develop");
        modifyJenkinsfile("master");
        String newVersion = readPOMVersion(type);
        executeCommand("git add . && git commit -m \"Avvio release " + newVersion + "\"");
        executeCommand("git tag " + newVersion + " -m \"Release: " + newVersion + "\"");
        //git push origin --tags
        //git push
        executeCommand("git checkout develop && git merge master");
        modifyJenkinsfile("develop");
        readPOMVersion(type);
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
        if(oldVersion.contains("-SNAPSHOT")) {
            return oldVersion.split("-SNAPSHOT")[0];
        }
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

    public static String readPOMVersion(RELEASE_TYPE type) throws IOException, JAXBException {
        JAXBContext jaxbContext;
        jaxbContext = org.eclipse.persistence.jaxb.JAXBContextFactory
                .createContext(new Class[]{Project.class}, null);

        File file = new File(String.valueOf(Paths.get("./pippo.xml")));

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        Project o = (Project) jaxbUnmarshaller.unmarshal(file);

        System.out.println("OLD_VERSION:" + o.version);

        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd");
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        String newPomVersion = findDevelopNewVersion(type, o.version);
        o.version = newPomVersion;
        System.out.println("NEW_VERSION:" + o.version);

        // output to a xml file
        jaxbMarshaller.marshal(o, file);
        return o.version;
    }
}
