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

                        // ✂️ Recortar 50px de cada lado
                        int crop = 100;
                        
                        int newWidth = original.getWidth() - 2 * crop;
                        int newHeight = original.getHeight() - 2 * crop;

                        // Validación por si la imagen es muy chica
                        if (newWidth > 0 && newHeight > 0) {
                            BufferedImage cropped = original.getSubimage(crop, crop, newWidth, newHeight);

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
