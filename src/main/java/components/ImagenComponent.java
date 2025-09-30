package components;

import java.io.File;
import org.apache.poi.ss.usermodel.Cell;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import utils.PDFUtils;

public class ImagenComponent {
    private static final String[] EXTENSIONS = { ".jpg", ".jpeg", ".png", ".bmp" };

    public static Image build(Cell codigoCell, File carpetaImagenes, float imageSize, Image fallbackImage) {
        try {
            String codigoValue = PDFUtils.safeText(PDFUtils.getCellValue(codigoCell), "[SIN CÓDIGO]");
            String cleanCode = codigoValue.replace(",", ".");
            double codigoNum = Double.parseDouble(cleanCode);
            String codigoImagen = (codigoNum % 1 == 0) ? String.format("%.0f", codigoNum) : String.valueOf(codigoNum);

            for (String ext : EXTENSIONS) {
                File file = new File(carpetaImagenes, codigoImagen + ext);
                if (file.isFile()) {

                    // 📸 Leer la imagen como BufferedImage
                    BufferedImage original = ImageIO.read(file);
                    if (original != null) {
                        // 🚩 Recorte automático de bordes blancos
                        int left = 0, right = original.getWidth() - 1; //original.getWidth() - 1 es el borde derecho
                        int top = 0, bottom = original.getHeight() - 1; //original.getHeight() - 1 es el borde inferior
                        int threshold = 240; // Ajusta según el fondo (255 es blanco puro)

                        // Buscar el borde superior
                        outer: for (int y = 0; y < original.getHeight(); y++) {
                            for (int x = 0; x < original.getWidth(); x++) {
                                int rgb = original.getRGB(x, y);
                                int r = (rgb >> 16) & 0xff;
                                int g = (rgb >> 8) & 0xff;
                                int b = rgb & 0xff;
                                if (r < threshold || g < threshold || b < threshold) {
                                    top = y;
                                    break outer;
                                }
                            }
                        }
                        // Borde inferior
                        outer: for (int y = original.getHeight() - 1; y >= 0; y--) {
                            for (int x = 0; x < original.getWidth(); x++) {
                                int rgb = original.getRGB(x, y);
                                int r = (rgb >> 16) & 0xff;
                                int g = (rgb >> 8) & 0xff;
                                int b = rgb & 0xff;
                                if (r < threshold || g < threshold || b < threshold) {
                                    bottom = y;
                                    break outer;
                                }
                            }
                        }
                        // Borde izquierdo
                        outer: for (int x = 0; x < original.getWidth(); x++) {
                            for (int y = top; y <= bottom; y++) {
                                int rgb = original.getRGB(x, y);
                                int r = (rgb >> 16) & 0xff;
                                int g = (rgb >> 8) & 0xff;
                                int b = rgb & 0xff;
                                if (r < threshold || g < threshold || b < threshold) {
                                    left = x;
                                    break outer;
                                }
                            }
                        }
                        // Borde derecho
                        outer: for (int x = original.getWidth() - 1; x >= 0; x--) {
                            for (int y = top; y <= bottom; y++) {
                                int rgb = original.getRGB(x, y);
                                int r = (rgb >> 16) & 0xff;
                                int g = (rgb >> 8) & 0xff;
                                int b = rgb & 0xff;
                                if (r < threshold || g < threshold || b < threshold) {
                                    right = x;
                                    break outer;
                                }
                            }
                        }

                        int marginVertical = 50; // píxeles de margen arriba y abajo

                        top = Math.max(0, top - marginVertical);
                        bottom = Math.min(original.getHeight() - 1, bottom + marginVertical);

                        // left y right sin margen extra
                        // left = Math.max(0, left - marginHorizontal); // NO USAR
                        // right = Math.min(original.getWidth() - 1, right + marginHorizontal); // NO USAR

                        int newWidth = right - left + 1;
                        int newHeight = bottom - top + 1;

                        if (newWidth > 0 && newHeight > 0) {
                            BufferedImage cropped = original.getSubimage(left, top, newWidth, newHeight);

                            // 🎯 Convertir la BufferedImage recortada a Image de iText
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(cropped, "png", baos);
                            baos.flush();

                            byte[] imageBytes = baos.toByteArray();
                            baos.close();

                            return new Image(ImageDataFactory.create(imageBytes))
                                    .scaleToFit(imageSize, imageSize)
                                    .setAutoScale(false)
                                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return fallbackImage
                .scaleToFit(imageSize, imageSize)
                .setAutoScale(false)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
    }
}
