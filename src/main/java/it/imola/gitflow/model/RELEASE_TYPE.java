package it.imola.gitflow.model;

import it.imola.gitflow.GitFlowScript;

public enum RELEASE_TYPE {
    MAJOR,
    MINOR,
    PATCH;

    /**
     * Metodo che data una stringa ritorna il relativo enumeratore, altrimenti null
     * @param str Stringa da ricercare
     * @return
     */
    public static GitFlowScript.RELEASE_TYPE getReleaseType(String str) {
        for (GitFlowScript.RELEASE_TYPE me : GitFlowScript.RELEASE_TYPE.values()) {
            if (me.name().equalsIgnoreCase(str))
                return me;
        }
        return null;
    }
}
