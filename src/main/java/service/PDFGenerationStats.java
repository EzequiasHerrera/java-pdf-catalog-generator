package service;

public class PDFGenerationStats {

    public int productosGenerados;
    public int sinImagen;
    public int imagenEnBlanco;
    public int imagenNoLeida;
    public int errorImagen;
    public int codigoNoNumerico;
    public int filasIgnoradas;

    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Resumen ---\n");
        sb.append("Productos generados: ").append(productosGenerados).append("\n");
        if (sinImagen > 0)
            sb.append("Sin imagen: ").append(sinImagen).append("\n");
        if (imagenEnBlanco > 0)
            sb.append("Imagen en blanco: ").append(imagenEnBlanco).append("\n");
        if (imagenNoLeida > 0)
            sb.append("Imagen no leída: ").append(imagenNoLeida).append("\n");
        if (errorImagen > 0)
            sb.append("Error de imagen: ").append(errorImagen).append("\n");
        if (codigoNoNumerico > 0)
            sb.append("Código no numérico: ").append(codigoNoNumerico).append("\n");
        if (filasIgnoradas > 0)
            sb.append("Filas ignoradas (sin código): ").append(filasIgnoradas).append("\n");
        return sb.toString();
    }

}
