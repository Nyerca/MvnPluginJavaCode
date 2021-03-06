Il file java: "it.imola.gitflow.presentation.ApplicationRunner" è stato realizzato a supporto delle operazioni da effettuare con git
in particolare per automatizzare la modifica del Jenkinsfile inserendo la branch corretta.

Di seguito verranno elencate le 4 operazioni supportate:
x) feature_start: per aprire una feature passandogli come parametro il nome della feature
    +) in automatico verrà creata la branch e verrà modificato il jenkinsfile
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare la feature a remote
    ESEMPIO CHIAMATA JAR:
    java -jar gitflow-1.8.4-SNAPSHOT.jar feature_start tmpFeature C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow
    ESEMPIO CHIAMATA MAVEN:
    mvn exec:java -Dexec.mainClass="it.imola.gitflow.presentation.ApplicationRunner" -Dexec.args="feature_start tmpFeature C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow"

x) feature_close: per chiudere una feature e mergiarla indietro a develop
    +) In automatico effettua il merge della feature su develop e modifica il jenkinsfile
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare la develop
    +) rimarrà da eliminare la branch di feature
    ESEMPIO CHIAMATA JAR:
    java -jar gitflow-1.8.4-SNAPSHOT.jar feature_merge tmpFeature C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow
    ESEMPIO CHIAMATA:
    mvn exec:java -Dexec.mainClass="it.imola.gitflow.presentation.ApplicationRunner" -Dexec.args="feature_merge tmpFeature C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow"

x) feature_merge_close: per chiudere una feature e mergiarla indietro a develop e in seguito eliminare la feature
    +) In automatico effettua il merge della feature su develop e modifica il jenkinsfile
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare la develop
    ESEMPIO CHIAMATA JAR:
    java -jar gitflow-1.8.4-SNAPSHOT.jar feature_merge_close tmpFeature C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow
    ESEMPIO CHIAMATA:
    mvn exec:java -Dexec.mainClass="it.imola.gitflow.presentation.ApplicationRunner" -Dexec.args="feature_merge_close tmpFeature C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow"

x) release_start_close: per portare le modifiche su master passando come parametri, il tipo di modifica alla versione (MAJOR/MINOR/PATCH)
    +) in automatico verrà mergiato develop in master e verrà modificato il jenkinsfile ed il pom
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare master
    +) verrà mergiata master in develop e verrà modificato il jenkinsfile ed il pom
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare develop
    +) rimarrà da pushare i tags: git push origin --tags
    ESEMPIO CHIAMATA JAR:
    java -jar gitflow-1.8.4-SNAPSHOT.jar release_start_close MINOR C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow
    ESEMPIO CHIAMATA:
    mvn exec:java -Dexec.mainClass="it.imola.gitflow.presentation.ApplicationRunner" -Dexec.args="release_start_close MINOR C:\\Users\\UTENTE\\Desktop\\testJGitFlow\\jgitflow"



ESECUZIONE JAR
java -jar path/to/jar.jar arg[0] arg[1] arg[2]

