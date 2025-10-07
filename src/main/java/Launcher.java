import fx.Main;

public class Launcher {
    public static void main(String[] args) {
        // Si tiene consola adjunta (ejecutado desde cmd/powershell)
        if (System.console() != null) {
            cmd.CommandLine.main(args);
        } else { // Si no se asume modo gráfico
            Main.main(args);
        }
    }
}