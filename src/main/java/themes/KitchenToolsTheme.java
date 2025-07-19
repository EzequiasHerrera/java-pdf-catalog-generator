package themes;

import java.net.MalformedURLException;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;

import utils.PDFUtils;

public class KitchenToolsTheme {
    public static Theme getTheme() throws MalformedURLException {
        return new Theme(
                new DeviceRgb(255, 134, 28), // Title text
                new DeviceRgb(59, 30, 9), // Subtitle text
                new DeviceRgb(255, 135, 12), // Code Background color
                new DeviceRgb(72, 65, 151), // Code Text Color
                new DeviceRgb(162, 171, 145), // Card border Color
                new DeviceRgb(255, 255, 255), // Card Text Color

                ImageDataFactory.create(PDFUtils.class.getResource("/images/backgroundKT.png").toExternalForm()),
                ImageDataFactory.create(PDFUtils.class.getResource("/images/backgroundwhiteKT.png").toExternalForm()),
                ImageDataFactory.create(PDFUtils.class.getResource("/images/logoKT.png").toExternalForm()));
    }
}