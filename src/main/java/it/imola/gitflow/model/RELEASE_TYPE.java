package it.imola.gitflow.model;

public enum RELEASE_TYPE {
    MAJOR,
    MINOR,
    PATCH;

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
}
