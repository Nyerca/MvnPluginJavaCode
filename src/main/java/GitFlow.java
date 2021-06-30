import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GitFlow {
    public static void main(String[] args) {
        System.out.println("ciao");

        List<String> newLines = new ArrayList<String>();
        try {
            for (String line : Files.readAllLines(Paths.get("Jenkinsfile.txt"), StandardCharsets.UTF_8)) {
                if (line.contains("develop")) {
                    newLines.add(line.replace("develop", ""+System.currentTimeMillis()));
                } else {
                    newLines.add(line);
                }
            }
            Files.write(Paths.get("Jenkinsfile.txt"), newLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
