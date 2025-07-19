package themes;

import java.net.MalformedURLException;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;

import utils.PDFUtils;

public class LineageTheme {
    public static Theme getTheme() throws MalformedURLException {
        return new Theme(
                new DeviceRgb(66, 67, 154), // Title text
                new DeviceRgb(66, 67, 154), // Subtitle text
                new DeviceRgb(66, 67, 154), // Code Background color
                new DeviceRgb(255, 255, 255), // Code Text Color
                new DeviceRgb(125, 143, 195), // Card border Color
                new DeviceRgb(0, 0, 0), // Card Text Color

                ImageDataFactory.create(PDFUtils.class.getResource("/images/background.png").toExternalForm()),
                ImageDataFactory.create(PDFUtils.class.getResource("/images/backgroundwhite.png").toExternalForm()),
                ImageDataFactory.create(PDFUtils.class.getResource("/images/lineaGE.png").toExternalForm()));
    }
}
