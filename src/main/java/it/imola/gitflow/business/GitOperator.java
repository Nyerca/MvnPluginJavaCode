package it.imola.gitflow.business;

import it.imola.gitflow.integration.CommandManager;
import it.imola.gitflow.model.GIT_FUN;
import it.imola.gitflow.model.Project;
import it.imola.gitflow.model.RELEASE_TYPE;
import it.imola.gitflow.presentation.ApplicationRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.imola.gitflow.model.RELEASE_TYPE.getReleaseType;


public class GitOperator {

    public GitOperator() {

    }

    /**
     * Metodo per calcolare la prossima versione data una versione attuale
     * @param type Tipo della release MAJOR|MINOR|PATCH
     * @param oldVersion Vecchia versione nel pom
     * @return
     */
    public String findNewVersion(RELEASE_TYPE type, String oldVersion) {
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
     * Metodo per aggiornare il Jenkinsfile
     * @param branch Branch da inserire
     * @throws IOException
     */
    public void modifyJenkinsfile(String branch) throws IOException {
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
     * Metodo per leggere e fare l'update della versione nel pom
     * @param type Tipo della release MAJOR|MINOR|PATCH
     * @return La nuova versione calcoalta
     * @throws IOException
     * @throws JAXBException
     */
    public String readPOMVersion(RELEASE_TYPE type) throws IOException, JAXBException {
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

    public void executeFun(GIT_FUN funzione, String argument){
        if (funzione != null) {
            try {
                switch (funzione) {
                    case feature_start:
                        if(!"master".equals(argument) && !"develop".equals(argument)) {
                            openFeature(argument);
                        } else {
                            System.out.println("**** NOME FEATURE ERRATO ****");
                        }
                        break;
                    case feature_merge:
                        if(!"master".equals(argument) && !"develop".equals(argument)) {
                            mergeFeature(argument);
                        } else {
                            System.out.println("**** NOME FEATURE ERRATO ****");
                        }
                        break;
                    case feature_merge_close:
                        if(!"master".equals(argument) && !"develop".equals(argument)) {
                            closeFeature(argument);
                        } else {
                            System.out.println("**** NOME FEATURE ERRATO ****");
                        }
                        break;
                    case release_start_close:
                        RELEASE_TYPE release = getReleaseType(argument);
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
    }

    /**
     * Metodo di apertura di una release
     * @param type Tipo di release MAJOR|MINOR|PATCH
     * @throws IOException
     * @throws InterruptedException
     * @throws JAXBException
     */
    public void openRelease(RELEASE_TYPE type) throws IOException, InterruptedException, JAXBException {
        CommandManager.getInstance().executeCommand("git checkout master && git merge develop");
        modifyJenkinsfile("master");
        String newVersion = readPOMVersion(type);
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Avvio release " + newVersion + "\"");
        CommandManager.getInstance().executeCommand("git tag " + newVersion + " -m \"Release: " + newVersion + "\"");
        //git push origin --tags
        //git push
        CommandManager.getInstance().executeCommand("git checkout develop && git merge master");
        modifyJenkinsfile("develop");
        readPOMVersion(type);
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Merge con master e aggiornamento versione\"");
        //git push
    }

    /**
     * Metodo di apertura di una feature da develop
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public void openFeature(String name) throws IOException, InterruptedException {
        CommandManager.getInstance().executeCommand("git checkout -b feature/" + name + " develop");
        modifyJenkinsfile("feature/" + name);
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    /**
     * Metodo di merge di una feature in develop
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public void mergeFeature(String name) throws IOException, InterruptedException {
        CommandManager.getInstance().executeCommand("git checkout develop && git merge feature/" + name);
        modifyJenkinsfile("develop");
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    /**
     * Metodo di merge di una feature in develop e di eliminazione della feature
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public void closeFeature(String name) throws IOException, InterruptedException {
        mergeFeature(name);
        CommandManager.getInstance().executeCommand("git branch -D feature/" + name);
    }

}
