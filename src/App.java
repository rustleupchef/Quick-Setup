import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        setup(args[0], args[1]);
    }

    private static void setup(String sourcePath, String outputPath) throws IOException {
        File source = new File(sourcePath);
        File output = new File(outputPath);
        if ((!source.exists() || !output.exists()) || (!source.isDirectory() || !output.isDirectory())) return;

        File[] sourceFiles = source.listFiles();
        File[] outputFiles = output.listFiles();
        for (File file : sourceFiles) {
            if (file.isDirectory()) {
                String nSourcePath = file.getPath();
                File f = new File(outputPath + file.getName() + "/");
                if (!f.exists()) f.mkdir();
                String nOutputPath = f.getPath() + "/";
                setup(nSourcePath, nOutputPath);
                continue;
            }
            if (notExists(outputFiles, file.getName())) {
                String text = "";
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    text += scanner.nextLine() + "\n";
                }
                if (text.length() > 0) text = text.substring(0, text.length() - 1);
                scanner.close();
                FileWriter fileWriter = new FileWriter(new File(outputPath + file.getName()));
                fileWriter.write(text);
                fileWriter.close();
                
            }
        }
    }

    private static boolean notExists(File[] files, String target) {
        return !exists(files, target);
    }

    private static boolean exists(File[] files, String target) {
        for (File file : files) {
            if (target.equals(file.getName())) return true;
        }
        return false;
    }
}
