package it.imola.gitflow.presentation;

import it.imola.gitflow.business.GitOperator;
import it.imola.gitflow.model.GIT_FUN;

import javax.xml.bind.JAXBException;
import java.io.IOException;

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
    }
}
