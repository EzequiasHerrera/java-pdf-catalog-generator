package pdf.components;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Cell;
import utils.ExcelUtils;
import utils.PDFUtils;
import utils.ValidationUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import service.PDFGenerationStats;

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

    public static Image build(Cell codigoCell, File carpetaImagenes, float imageSize, Consumer<String> log, PDFGenerationStats stats) throws Exception {

        final String codigoValue = PDFUtils.safeText(ExcelUtils.getCellValue(codigoCell), "[SIN CÓDIGO]");
        final String cleanCode = codigoValue.replace(",", ".");

        if (ValidationUtils.isNumeric(cleanCode)) {
            double codigoNum = Double.parseDouble(cleanCode);
            String codigoImagen = (codigoNum % 1 == 0) ? String.format("%.0f", codigoNum) : String.valueOf(codigoNum);
            boolean existe = false;

            for (String ext : EXTENSIONS) {
                Path path = Paths.get(carpetaImagenes.toString(), codigoImagen + ext);
                if (Files.isRegularFile(path)) {
                    existe = true;
                    try {
                        BufferedImage original = ImageIO.read(path.toFile());
                        if (original == null) {
                            stats.imagenNoLeida++;
                            log.accept("No se pudo leer la imagen: " + codigoImagen + ext + "\n");
                            continue;
                        }

                        final int width = original.getWidth();
                        final int height = original.getHeight();
                        final int threshold = 240;

                        // Obtenemos todos los píxeles de una sola vez (mucho más rápido)
                        int[] pixels = original.getRGB(0, 0, width, height, null, 0, width);

                        int left = width, right = -1, top = height, bottom = -1;

                        // Un solo bucle lineal sobre todos los píxeles
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

                        if (right >= left && bottom >= top) {
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
                        } else {
                            stats.imagenEnBlanco++;
                            log.accept("La imagen parece estar completamente en blanco: " + codigoImagen + ext + "\n");
                        }
                    } catch (Exception e) {
                        stats.errorImagen++;
                        log.accept("Error al procesar imagen " + codigoImagen + ext + ": " + e.getMessage() + "\n");
                    }
                }
            }
            if (!existe) {
                stats.sinImagen++;
                log.accept("No existe la imagen para el producto: " + codigoImagen + "\n");
            }
        } else {
            stats.codigoNoNumerico++;
            log.accept("El código del producto no es un número: " + cleanCode + "\n");
        }

        return new Image(sinImagen)
                .scaleToFit(imageSize, imageSize)
                .setAutoScale(false)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
    }

}
