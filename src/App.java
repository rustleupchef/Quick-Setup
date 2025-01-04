import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        if (System.getenv("qspath") == null) {
            System.out.println("Please make environment variable qspath");
            return;
        }

        String source = args[0];
        if (source.equals("init")) {
            if (args.length > 1 && args[1].equals("all")) {
                File[] folders = folders(System.getProperty("user.dir"));
                for (File folder : folders) {
                    String path = folder.getAbsolutePath() + "/";
                    String name = folder.getName();
                    FileWriter writer = new FileWriter(new File(System.getenv("qspath") + "paths.qs"), true);
                    writer.write(name.length() + "%" + name + path + "\n");
                    writer.close();
                }
                return;
            }
            String path = System.getProperty("user.dir") + "/";
            String name = new File(path).getName();
            if (args.length > 1) {
                name = args[1];
            }
            FileWriter writer = new FileWriter(new File(System.getenv("qspath") + "paths.qs"), true);
            writer.write(name.length() + "%" + name + path + "\n");
            writer.close();
            return;
        }

        Scanner paths = new Scanner(new File(System.getenv("qspath") + "paths.qs"));
        while (paths.hasNextLine()) {
            String[] repo = getRepo(paths.nextLine());
            if (repo[0].equals(source)) {
                source = repo[1];
            }
        }
        paths.close();

        int type = 1;
        if (args.length > 2) {
            type = Integer.valueOf(args[2]);
            setup(source, System.getProperty("user.dir"), (byte) type, false);
            runCommand(source);
            return;
        }

        Scanner defaultConfiguration = new Scanner(new File(System.getenv("qspath") + "default.qsconf"));
        while (defaultConfiguration.hasNextLine()) {
            try {
                String[] item = defaultConfiguration.nextLine().split("=");
                if (item[0].equals("type")) {
                    type = Integer.valueOf(item[1]);
                }
            } catch (Exception e) {
                System.out.println("Their was an error in the default.qsconf files. " + e.getMessage());
            }
        }
        defaultConfiguration.close();

        File qsConf = new File(source + ".qsconf");
        if (!qsConf.exists()) {
            System.out.println("The file .qsconf doesn't exist or is not formatted correctly");
            type = clamp(type, 1, 3);
            setup(source, System.getProperty("user.dir") + "/", (byte) type, false);
            runCommand(source);
            return;
        }
        Scanner configuration = new Scanner(qsConf);
        while (configuration.hasNextLine()) {
            String[] item = configuration.nextLine().split("=");
            if (item.length < 2) continue;
            if (item[0].equals("type")) {
                type = Integer.valueOf(item[1]);
            }
        }
        configuration.close();
        type = clamp(type, 1, 3);
        setup(source, System.getProperty("user.dir") + "/", (byte) type, false);
        runCommand(source);
    }

    private static void setup(String sourcePath, String outputPath, byte type) throws IOException {
        setup(sourcePath, outputPath, (byte) type, true);
    }

    private static void setup(String sourcePath, String outputPath, byte type, boolean asked) throws IOException {
        File source = new File(sourcePath);
        File output = new File(outputPath);

        if (type == (byte) 2 && !asked) {
            Scanner readInput = new Scanner(System.in);
            System.out.println("Are you sure you want to delete all files? Y or y/any other input)");
            if (!readInput.nextLine().toLowerCase().equals("y")) {
                readInput.close();
                return;
            }
            deleteFolder(output, true);
            readInput.close();
        }

        if ((!source.exists() || !output.exists()) || (!source.isDirectory() || !output.isDirectory())) {
            System.out.println("Error. This folder likely doesn't exist"); 
            return; 
        } 

        File[] sourceFiles = source.listFiles();

        for (File file : sourceFiles) {
            if (file.isDirectory()) {
                String nSourcePath = file.getPath();
                File f = new File(outputPath + file.getName() + "/");
                if (!f.exists()) f.mkdir();
                String nOutputPath = f.getPath() + "/";
                setup(nSourcePath, nOutputPath, type);
                continue;
            }

            if (file.getName().equals(".qsconf") || file.getName().equals(".qscmd")) continue;

            if (type == (byte) 3) {
                copy(file, outputPath, true);
                continue;
            }
            copy(file, outputPath, false);
        }
    }

    private static void deleteFolder (File folder, boolean isFirst) {
        if (!folder.isDirectory()) return;

        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolder(file);
                continue;
            }
            file.delete();
        }
        if (!isFirst) folder.delete();
    }

    private static void deleteFolder(File folder) {
        deleteFolder(folder, false);
    }

    private static void copy (File file, String outputPath, boolean replace) throws IOException {
        try {
            Path sourceFile = Paths.get(file.getAbsolutePath());
            Path targetFile = Paths.get(outputPath).resolve(file.getName());
            if (replace) {
                Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return;
            }
            Files.copy(sourceFile, targetFile);
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("File " + file.getName() + " had a error while copying");
        }
    }

    private static int clamp(int value, int min, int max) {
        if (value > max) return max;
        if (value < min) return min;
        return value;
    }

    private static String[] getRepo(String line) {
        String num = "";
        int startIndex = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '%') {
                startIndex = i + 1;
                break;
            } 
            num += line.charAt(i);
        }
        int endIndex = startIndex + Integer.valueOf(num);
        return new String[] {line.substring(startIndex, endIndex), line.substring(endIndex)};
    }

    private static File[] folders (String path) {
        File[] file = new File(path).listFiles();
        List<File> folders = new ArrayList<File>();
        for (File f : file) {
            if (f.isDirectory()) folders.add(f);
        }
        return folders.toArray(new File[0]);
    }

    private static void runCommand(String path) throws IOException {
        File  qsCmd = new File(path + ".qscmd");
        if (!qsCmd.exists()){
            System.out.println("The file .qscmd doesn't exist or is not formatted correctly");
            return;
        }
        Scanner file = new Scanner(qsCmd);
        while (file.hasNextLine()) {
            String line = file.nextLine();
            if (line.charAt(0) == '#') continue;
            String[] command = line.split(" ");
            new ProcessBuilder(command).start();
        }
        file.close();
    }
}