package it.imola.gitflow.presentation;

import it.imola.gitflow.model.Project;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationRunner {
    public static void main(String[] args) {

        if (args.length > 1) {
            System.out.println(args[0]);
            GIT_FUN funzione = getFun(args[0]);
            if (funzione != null) {
                try {
                    switch (funzione) {
                        case feature_start:
                            if(!"master".equals(args[1]) && !"develop".equals(args[1])) {
                                openFeature(args[1]);
                            } else {
                                System.out.println("**** NOME FEATURE ERRATO ****");
                            }
                            break;
                        case feature_merge:
                            if(!"master".equals(args[1]) && !"develop".equals(args[1])) {
                                mergeFeature(args[1]);
                            } else {
                                System.out.println("**** NOME FEATURE ERRATO ****");
                            }
                            break;
                        case feature_merge_close:
                            if(!"master".equals(args[1]) && !"develop".equals(args[1])) {
                                closeFeature(args[1]);
                            } else {
                                System.out.println("**** NOME FEATURE ERRATO ****");
                            }
                            break;
                        case release_start_close:
                            RELEASE_TYPE release = getReleaseType(args[1]);
                            if (release != null) {
                                openRelease(release);
                            } else {
                                System.out.println("**** TIPO RELEASE NON ESISTENTE ****");
                            }
                            break;
                    }
                } catch (IOException | InterruptedException | JAXBException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("**** FUNZIONE INSERITA NON ESISTENTE ****");
            }
        } else {
            System.out.println("**** INSERIRE IL TIPO DI FUNZIONE DA RICHIAMARE E IL PARAMETRO DELLA RELATIVA FUNZIONE ****");
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

    /**
     * Metodo che data una stringa ritorna il relativo enumeratore, altrimenti null
     * @param str Stringa da ricercare
     * @return
     */
    public static RELEASE_TYPE getReleaseType(String str) {
        for (RELEASE_TYPE me : RELEASE_TYPE.values()) {
            if (me.name().equalsIgnoreCase(str))
                return me;
        }
        return null;
    }

    /**
     * Metodo che data una stringa ritorna il relativo enumeratore, altrimenti null
     * @param str Stringa da ricercare
     * @return
     */
    public static GIT_FUN getFun(String str) {
        for (GIT_FUN me : GIT_FUN.values()) {
            if (me.name().equalsIgnoreCase(str))
                return me;
        }
        return null;
    }

    /**
     * Metodo di apertura di una release
     * @param type Tipo di release MAJOR|MINOR|PATCH
     * @throws IOException
     * @throws InterruptedException
     * @throws JAXBException
     */
    public static void openRelease(RELEASE_TYPE type) throws IOException, InterruptedException, JAXBException {
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

    /**
     * Metodo di apertura di una feature da develop
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public static void openFeature(String name) throws IOException, InterruptedException {
        executeCommand("git checkout -b feature/" + name + " develop");
        modifyJenkinsfile("feature/" + name);
        executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    /**
     * Metodo di merge di una feature in develop
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public static void mergeFeature(String name) throws IOException, InterruptedException {
        executeCommand("git checkout develop && git merge feature/" + name);
        modifyJenkinsfile("develop");
        executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    /**
     * Metodo di merge di una feature in develop e di eliminazione della feature
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public static void closeFeature(String name) throws IOException, InterruptedException {
        mergeFeature(name);
        executeCommand("git branch -D feature/" + name);
    }

    /**
     * Metodo per calcolare la prossima versione data una versione attuale
     * @param type Tipo della release MAJOR|MINOR|PATCH
     * @param oldVersion Vecchia versione nel pom
     * @return
     */
    public static String findNewVersion(RELEASE_TYPE type, String oldVersion) {
        String returnVersion;
        if (oldVersion.contains("-SNAPSHOT")) {
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

    /**
     * Metodo di esecuzione di un comando su bat o sh
     * @param command Comando da eseguire
     * @throws IOException
     * @throws InterruptedException
     */
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

    /**
     * Metodo per aggiornare il Jenkinsfile
     * @param branch Branch da inserire
     * @throws IOException
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

    /**
     * Metodo per fare la print del return di un processo lanciato
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

    /**
     * Metodo per leggere e fare l'update della versione nel pom
     * @param type Tipo della release MAJOR|MINOR|PATCH
     * @return La nuova versione calcoalta
     * @throws IOException
     * @throws JAXBException
     */
    public static String readPOMVersion(RELEASE_TYPE type) throws IOException, JAXBException {
        JAXBContext jaxbContext;
        jaxbContext = org.eclipse.persistence.jaxb.JAXBContextFactory
                .createContext(new Class[]{Project.class}, null);

        File file = new File(String.valueOf(Paths.get("./pom.xml")));

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        Project o = (Project) jaxbUnmarshaller.unmarshal(file);

        System.out.println("OLD_VERSION:" + o.version);

        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd");
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        String newPomVersion = findNewVersion(type, o.version);
        o.version = newPomVersion;
        System.out.println("NEW_VERSION:" + o.version);

        // output to a xml file
        jaxbMarshaller.marshal(o, file);
        return o.version;
    }
}
