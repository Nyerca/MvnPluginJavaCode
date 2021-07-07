package it.imola.gitflow.integration;

import it.imola.gitflow.model.Project;
import it.imola.gitflow.model.RELEASE_TYPE;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class PomManager {
    private static PomManager pomManager;
    private String projectFolder;
    private boolean isWindows;

    private PomManager() {

    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public static PomManager getInstance() {
        if(null == pomManager) {
            pomManager = new PomManager();
        }
        return pomManager;
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

        File file = new File(String.valueOf(Paths.get(projectFolder + "/pom.xml")));

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
