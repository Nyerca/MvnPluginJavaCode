Il file java: GitFlowScript è stato realizzato a supporto delle operazioni da effettuare con git
in particolare per automatizzare la modifica del Jenkinsfile inserendo la branch corretta.

Di seguito verranno elencate le 4 operazioni supportate:
x) feature_start: per aprire una feature passandogli come parametro il nome della feature
    +) in automatico verrà creata la branch e verrà modificato il jenkinsfile
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare la feature a remote
    ESEMPIO CHIAMATA:
    mvn exec:java -Dexec.mainClass="GitFlowScript" -Dexec.args="feature_start tmpFeature"

x) feature_close: per chiudere una feature e mergiarla indietro a develop
    +) In automatico effettua il merge della feature su develop e modifica il jenkinsfile
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare la develop
    +) rimarrà da eliminare la branch di feature
    ESEMPIO CHIAMATA:
    mvn exec:java -Dexec.mainClass="GitFlowScript" -Dexec.args="feature_close"

x) feature_merge_close: per chiudere una feature e mergiarla indietro a develop e in seguito eliminare la feature
    +) In automatico effettua il merge della feature su develop e modifica il jenkinsfile
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare la develop
    ESEMPIO CHIAMATA:
    mvn exec:java -Dexec.mainClass="GitFlowScript" -Dexec.args="feature_merge_close"

x) release_start_close: per portare le modifiche su master passando come parametri, il tipo di modifica alla versione (MAJOR/MINOR/PATCH)
    +) in automatico verrà mergiato develop in master e verrà modificato il jenkinsfile ed il pom
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare master
    +) verrà mergiata master in develop e verrà modificato il jenkinsfile ed il pom
    +) in automatico verranno aggiunte e committate le modifiche
    +) rimarrà da pushare develop
    +) rimarrà da pushare i tags: git push origin --tags
    ESEMPIO CHIAMATA:
    mvn exec:java -Dexec.mainClass="GitFlowScript" -Dexec.args="release_start_close MINOR"



