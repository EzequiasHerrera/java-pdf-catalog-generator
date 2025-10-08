import fx.Main;

public class Launcher {
    public static void main(String[] args) {
        // Si tiene argumento "consola"
        if (args.length > 0 && args[0].equalsIgnoreCase("consola")) {
            cmd.CommandLine.main(args);
        } else { // Si no se asume modo gráfico
            Main.main(args);
        }
    }
}