package pdf.components;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Cell;
import utils.ExcelUtils;
import utils.PDFUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImagenComponent {

    private static final String[] EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp"};
    private static final ImageData sinImagen;

    static {
        try {
            sinImagen = ImageDataFactory.create(PDFUtils.class.getResource("/images/SINIMAGEN.jpg").toExternalForm());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Image build(Cell codigoCell, File carpetaImagenes, float imageSize, StringBuilder log) throws Exception {

        final String codigoValue = PDFUtils.safeText(ExcelUtils.getCellValue(codigoCell), "[SIN CÓDIGO]");
        final String cleanCode = codigoValue.replace(",", ".");

        if (esNumero(cleanCode)) {
            double codigoNum = Double.parseDouble(cleanCode);
            String codigoImagen = (codigoNum % 1 == 0) ? String.format("%.0f", codigoNum) : String.valueOf(codigoNum);
            boolean existe = false;

            for (String ext : EXTENSIONS) {
                Path path = Paths.get(carpetaImagenes.toString(), codigoImagen + ext);
                if (Files.isRegularFile(path)) {
                    existe = true;
                    BufferedImage original = ImageIO.read(path.toFile());
                    if (original == null) continue;

                    final int width = original.getWidth();
                    final int height = original.getHeight();
                    final int threshold = 240;

                    // ⚡ Obtenemos todos los píxeles de una sola vez (mucho más rápido)
                    int[] pixels = original.getRGB(0, 0, width, height, null, 0, width);

                    int left = width, right = -1, top = height, bottom = -1;

                    // 🚀 Un solo bucle lineal sobre todos los píxeles
                    for (int y = 0, idx = 0; y < height; y++) {
                        for (int x = 0; x < width; x++, idx++) {
                            int rgb = pixels[idx];
                            int r = (rgb >> 16) & 0xff;
                            int g = (rgb >> 8) & 0xff;
                            int b = rgb & 0xff;

                            if (r < threshold || g < threshold || b < threshold) {
                                if (x < left) left = x;
                                if (x > right) right = x;
                                if (y < top) top = y;
                                if (y > bottom) bottom = y;
                            }
                        }
                    }

                    if (right > left && bottom > top) {
                        int marginVertical = 50;
                        top = Math.max(0, top - marginVertical);
                        bottom = Math.min(height - 1, bottom + marginVertical);

                        BufferedImage cropped = original.getSubimage(left, top, right - left + 1, bottom - top + 1);

                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            ImageIO.write(cropped, "png", baos);
                            return new Image(ImageDataFactory.create(baos.toByteArray()))
                                    .scaleToFit(imageSize, imageSize)
                                    .setAutoScale(false)
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
                        }
                    }
                }
            }
            if (!existe) {
                log.append("No existe la imagen para el producto: ").append(codigoImagen).append('\n');
            }
        } else {
            log.append("El código del producto no es un número: ").append(cleanCode).append('\n');
        }

        return new Image(sinImagen)
                .scaleToFit(imageSize, imageSize)
                .setAutoScale(false)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
    }

    public static boolean esNumero(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // version de Ezequias
//    public static Image build(Cell codigoCell, File carpetaImagenes, float imageSize) throws Exception {
//        try {
//            String codigoValue = PDFUtils.safeText(PDFUtils.getCellValue(codigoCell), "[SIN CÓDIGO]");
//            String cleanCode = codigoValue.replace(",", ".");
//            double codigoNum = Double.parseDouble(cleanCode);
//            String codigoImagen = (codigoNum % 1 == 0) ? String.format("%.0f", codigoNum) : String.valueOf(codigoNum);
//
//            for (String ext : EXTENSIONS) {
//                Path path = Paths.get(carpetaImagenes.toString(), codigoImagen + ext);
//                if (Files.exists(path) && Files.isRegularFile(path)) {
//                    // 📸 Leer la imagen como BufferedImage
//                    BufferedImage original = ImageIO.read(path.toFile());
//                    if (original != null) {
//                        // 🚩 Recorte automático de bordes blancos
//                        int left = 0, right = original.getWidth() - 1; //original.getWidth() - 1 es el borde derecho
//                        int top = 0, bottom = original.getHeight() - 1; //original.getHeight() - 1 es el borde inferior
//                        int threshold = 240; // Ajusta según el fondo (255 es blanco puro)
//
//                        // Buscar el borde superior
//                        outer:
//                        for (int y = 0; y < original.getHeight(); y++) {
//                            for (int x = 0; x < original.getWidth(); x++) {
//                                int rgb = original.getRGB(x, y);
//                                int r = (rgb >> 16) & 0xff;
//                                int g = (rgb >> 8) & 0xff;
//                                int b = rgb & 0xff;
//                                if (r < threshold || g < threshold || b < threshold) {
//                                    top = y;
//                                    break outer;
//                                }
//                            }
//                        }
//                        // Borde inferior
//                        outer:
//                        for (int y = original.getHeight() - 1; y >= 0; y--) {
//                            for (int x = 0; x < original.getWidth(); x++) {
//                                int rgb = original.getRGB(x, y);
//                                int r = (rgb >> 16) & 0xff;
//                                int g = (rgb >> 8) & 0xff;
//                                int b = rgb & 0xff;
//                                if (r < threshold || g < threshold || b < threshold) {
//                                    bottom = y;
//                                    break outer;
//                                }
//                            }
//                        }
//                        // Borde izquierdo
//                        outer:
//                        for (int x = 0; x < original.getWidth(); x++) {
//                            for (int y = top; y <= bottom; y++) {
//                                int rgb = original.getRGB(x, y);
//                                int r = (rgb >> 16) & 0xff;
//                                int g = (rgb >> 8) & 0xff;
//                                int b = rgb & 0xff;
//                                if (r < threshold || g < threshold || b < threshold) {
//                                    left = x;
//                                    break outer;
//                                }
//                            }
//                        }
//                        // Borde derecho
//                        outer:
//                        for (int x = original.getWidth() - 1; x >= 0; x--) {
//                            for (int y = top; y <= bottom; y++) {
//                                int rgb = original.getRGB(x, y);
//                                int r = (rgb >> 16) & 0xff;
//                                int g = (rgb >> 8) & 0xff;
//                                int b = rgb & 0xff;
//                                if (r < threshold || g < threshold || b < threshold) {
//                                    right = x;
//                                    break outer;
//                                }
//                            }
//                        }
//
//                        int marginVertical = 50; // píxeles de margen arriba y abajo
//
//                        top = Math.max(0, top - marginVertical);
//                        bottom = Math.min(original.getHeight() - 1, bottom + marginVertical);
//
//                        // left y right sin margen extra
//                        // left = Math.max(0, left - marginHorizontal); // NO USAR
//                        // right = Math.min(original.getWidth() - 1, right + marginHorizontal); // NO USAR
//
//                        int newWidth = right - left + 1;
//                        int newHeight = bottom - top + 1;
//
//                        if (newWidth > 0 && newHeight > 0) {
//                            BufferedImage cropped = original.getSubimage(left, top, newWidth, newHeight);
//
//                            // 🎯 Convertir la BufferedImage recortada a Image de iText
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            ImageIO.write(cropped, "png", baos);
//                            baos.flush();
//
//                            byte[] imageBytes = baos.toByteArray();
//                            baos.close();
//
//                            return new Image(ImageDataFactory.create(imageBytes))
//                                    .scaleToFit(imageSize, imageSize)
//                                    .setAutoScale(false)
//                                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw e;
//        }
//
//        return new Image(sinImagen)
//                .scaleToFit(imageSize, imageSize)
//                .setAutoScale(false)
//                .setHorizontalAlignment(HorizontalAlignment.CENTER);
//    }

}
