package it.imola.gitflow.model;

import it.imola.gitflow.GitFlowScript;

public enum GIT_FUN {
    feature_start,
    feature_merge,
    feature_merge_close,
    release_start_close;

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
}