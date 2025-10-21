import cmd.CommandLine;
import fx.Main;

public class Launcher {
    public static void main(String[] args) {
        // Si tiene el argumento "consola" se ejecuta por consola
        if (args.length > 0 && args[0].equalsIgnoreCase("consola")) {
            CommandLine.main(args);
        } else { // Si no se ejecuta por JavaFX
            Main.main(args);
        }
    }
}