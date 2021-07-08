package it.imola.gitflow.business;

import it.imola.gitflow.integration.CommandManager;
import it.imola.gitflow.integration.JenkinsfileManager;
import it.imola.gitflow.integration.PomManager;
import it.imola.gitflow.model.GIT_FUN;
import it.imola.gitflow.model.RELEASE_TYPE;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static it.imola.gitflow.model.RELEASE_TYPE.getReleaseType;


public class GitOperator {

    public GitOperator() {

    }

    public void executeFun(GIT_FUN funzione, String argument, String path) {
        CommandManager.getInstance().setProjectFolder(path);
        PomManager.getInstance().setProjectFolder(path);
        JenkinsfileManager.getInstance().setProjectFolder(path);
        if (funzione != null) {
            try {
                switch (funzione) {
                    case feature_start:
                        if (!"master".equals(argument) && !"develop".equals(argument)) {
                            openFeature(argument);
                        } else {
                            System.out.println("**** NOME FEATURE ERRATO ****");
                        }
                        break;
                    case feature_merge:
                        if (!"master".equals(argument) && !"develop".equals(argument)) {
                            mergeFeature(argument);
                        } else {
                            System.out.println("**** NOME FEATURE ERRATO ****");
                        }
                        break;
                    case feature_merge_close:
                        if (!"master".equals(argument) && !"develop".equals(argument)) {
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
     *
     * @param type Tipo di release MAJOR|MINOR|PATCH
     * @throws IOException
     * @throws InterruptedException
     * @throws JAXBException
     */
    public void openRelease(RELEASE_TYPE type) throws IOException, InterruptedException, JAXBException {
        CommandManager.getInstance().executeCommand("git checkout master && git merge develop");
        JenkinsfileManager.getInstance().modifyJenkinsfile("master");
        String newVersion = PomManager.getInstance().readPOMVersion(type);
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Avvio release " + newVersion + "\"");
        CommandManager.getInstance().executeCommand("git tag " + newVersion + " -m \"Release: " + newVersion + "\"");
        //git push origin --tags
        //git push
        CommandManager.getInstance().executeCommand("git checkout develop && git merge master");
        JenkinsfileManager.getInstance().modifyJenkinsfile("develop");
        PomManager.getInstance().readPOMVersion(type);
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Merge con master e aggiornamento versione\"");
        //git push
    }

    /**
     * Metodo di apertura di una feature da develop
     *
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public void openFeature(String name) throws IOException, InterruptedException {
        CommandManager.getInstance().executeCommand("git checkout -b feature/" + name + " develop");
        JenkinsfileManager.getInstance().modifyJenkinsfile("feature/" + name);
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    /**
     * Metodo di merge di una feature in develop
     *
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public void mergeFeature(String name) throws IOException, InterruptedException {
        CommandManager.getInstance().executeCommand("git checkout develop && git merge feature/" + name);
        JenkinsfileManager.getInstance().modifyJenkinsfile("develop");
        CommandManager.getInstance().executeCommand("git add . && git commit -m \"Modifica branch nel Jenkinsfile\"");
    }

    /**
     * Metodo di merge di una feature in develop e di eliminazione della feature
     *
     * @param name Nome feature
     * @throws IOException
     * @throws InterruptedException
     */
    public void closeFeature(String name) throws IOException, InterruptedException {
        mergeFeature(name);
        CommandManager.getInstance().executeCommand("git branch -D feature/" + name);
    }

}
