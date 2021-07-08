package it.imola.gitflow.presentation;

import it.imola.gitflow.business.GitOperator;
import it.imola.gitflow.model.GIT_FUN;

import static it.imola.gitflow.model.GIT_FUN.getFun;

public class ApplicationRunner {
    public static void main(String[] args) {

        if (args.length > 2) {
            System.out.println(args[0]);
            GIT_FUN funzione = getFun(args[0]);
            GitOperator gitOperator = new GitOperator();
            gitOperator.executeFun(funzione, args[1], args[2]);
        } else {
            System.out.println("**** INSERIRE IL TIPO DI FUNZIONE DA RICHIAMARE E IL PARAMETRO DELLA RELATIVA FUNZIONE ****");
        }



/*
        List<String> newLines = new ArrayList<String>();

        try {
            for (String line : Files.readAllLines(Paths.get("C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow\\Jenkinsfile.txt"), StandardCharsets.UTF_8)) {
                if (line.contains("mpl")) {
                    Pattern p = Pattern.compile("mpl@\\S*'", Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(line);
                    String result = m.replaceAll("mpl@develop"  + "'");
                    newLines.add(result);
                } else {
                    newLines.add(line);
                }
            }
            CommandManager.getInstance().setProjectFolder(args[2]);
            try {
                CommandManager.getInstance().executeCommand("whoami");
                CommandManager.getInstance().executeCommand("echo ciao22 > ciao2.txt");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Files.write(Paths.get("C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow\\ciao.txt"), newLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

    }
}
