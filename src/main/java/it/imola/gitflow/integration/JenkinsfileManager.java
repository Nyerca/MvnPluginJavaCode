package it.imola.gitflow.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JenkinsfileManager {
    private static JenkinsfileManager jenkinsfileManager;
    private String projectFolder;

    private JenkinsfileManager() {

    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public static JenkinsfileManager getInstance() {
        if (null == jenkinsfileManager) {
            jenkinsfileManager = new JenkinsfileManager();
        }
        return jenkinsfileManager;
    }

    /**
     * Metodo per aggiornare il Jenkinsfile
     *
     * @param branch Branch da inserire
     * @throws IOException
     */
    public void modifyJenkinsfile(String branch) throws IOException {
        List<String> newLines = new ArrayList<String>();

        for (String line : Files.readAllLines(Paths.get(projectFolder + "/Jenkinsfile.txt"), StandardCharsets.UTF_8)) {
            if (line.contains("mpl")) {
                Pattern p = Pattern.compile("mpl@\\S*'", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(line);
                String result = m.replaceAll("mpl@" + branch + "'");
                newLines.add(result);
            } else {
                newLines.add(line);
            }
        }
        Files.write(Paths.get(projectFolder + "/Jenkinsfile.txt"), newLines, StandardCharsets.UTF_8);
    }
}
