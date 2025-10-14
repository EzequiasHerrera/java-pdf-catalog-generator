package pdf;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;

public class PDFStyleDefaults {

    public static final String BASE_FONT = "Helvetica";
    public static final String BASE_FONT_BOLD = "Helvetica-Bold";

    // Tamaños de fuente
    public static final float FONT_SIZE_CODIGO = 6;
    public static final float FONT_SIZE_PRODUCTO = 6;
    public static final float FONT_SIZE_PRECIO = 7;
    public static final float FONT_SIZE_UXB = 6;

    // Color por defecto para el texto
    public static final Color BASE_COLOR = ColorConstants.BLACK;
    public static final Color WHITE_COLOR = ColorConstants.WHITE;
    public static final Color GRAY_COLOR = ColorConstants.LIGHT_GRAY;
    public static final Color BLACK_COLOR = new DeviceRgb(28, 28, 28);
    public static final Color LIGHT_GRAY = new DeviceRgb(235, 235, 235);

}
