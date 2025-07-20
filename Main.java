import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Set;

public class Main { 
    private static String cwd = System.getProperty("user.dir");
    
    public static void main(String[] args) throws Exception {
        Set<String> commands = Set.of("cd", "echo", "exit", "pwd", "type");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (input.equals("exit 0") || input.equals("exit")) {
                System.exit(0);
            } 
            else if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));
            } 
            else if (input.equals("echo")) {
                System.out.println();
            }
            else if (input.startsWith("type ")) {
                String arg = input.substring(5).trim();
                if (commands.contains(arg)) {
                    System.out.printf("%s is a shell builtin%n", arg);
                } else {
                    String path = getPath(arg);
                    if (path == null) {
                        System.out.printf("%s: not found%n", arg);
                    } else {
                        System.out.printf("%s is %s%n", arg, path);
                    }
                }
            } 
            else if (input.equals("pwd")) {
                System.out.println(cwd);
            } 
            else if (input.startsWith("cd ")) {
                String dir = input.substring(3).trim();
                changeDirectory(dir);
            }
            else if (input.equals("cd")) {
                // cd without arguments goes to home directory
                changeDirectory("~");
            }
            else {
                // Handle external commands
                String command = input.split(" ")[0];
                String path = getPath(command); //gets full path of the command in the PATH
                if (path == null) {
                    System.out.printf("%s: command not found%n", command);
                } else {
                    executeCommand(input, path);
                }
            }
        }
    }
    
    private static void changeDirectory(String dir) {
        try {
            Path newPath;
            
            if (dir.equals("~")) {
                String home = System.getenv("HOME");
                newPath = Paths.get(home);
            } else if (dir.startsWith("/")) {
                // Absolute path
                newPath = Paths.get(dir);
            } else {
                // Relative path
                newPath = Paths.get(cwd, dir);
            }
            
            // Normalize the path to handle .. and . properly
            newPath = newPath.normalize().toAbsolutePath();
            
            if (Files.isDirectory(newPath)) {
                cwd = newPath.toString();
            } else {
                System.out.printf("cd: %s: No such file or directory%n", dir);
            }
        } catch (Exception e) {
            System.out.printf("cd: %s: No such file or directory%n", dir);
        }
    }
    
    private static void executeCommand(String input, String commandPath) {
        try {
            String[] parts = input.split(" ");
            String[] cmdArray = new String[parts.length];
            cmdArray[0] = commandPath;
            System.arraycopy(parts, 1, cmdArray, 1, parts.length - 1);  //copies parts of the command into cmdArray 
            
            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.directory(new java.io.File(cwd));  //sets working directory as current directory
            pb.inheritIO();  // makes the process inherits IO of the current process
            
            Process process = pb.start();  // executes the command
            int exitCode = process.waitFor();  //waits for the process to finish
            
        } catch (IOException | InterruptedException e) {
            System.out.printf("Error executing command: %s%n", e.getMessage());
        }
    }
    
    private static String getPath(String command) {  //searches for the command in the PATH environment variable and return its full path
        String pathEnv = System.getenv("PATH");  // retrieves the PATH environment variable
        if (pathEnv == null) {
            return null;
        }
        
        for (String path : pathEnv.split(System.getProperty("path.separator"))) {  // splits the PATH by the system's path separator ; for Windows
            Path fullPath = Paths.get(path, command);   
            if (Files.isRegularFile(fullPath) && Files.isExecutable(fullPath)) {
                return fullPath.toString();
            }
        }
        return null;
    }
}
