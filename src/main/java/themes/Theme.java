package themes;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.Color;

public class Theme {
    public final Color titleTextColor;
    public final Color subtitleTextColor;
    public final Color codeBackgroundColor;
    public final Color codeTextColor;
    public final Color cardBorderColor;
    public final Color cardTextColor;

    public final ImageData backgroundFirstPageImage;
    public final ImageData backgroundImage;
    public final ImageData logoImage;

    public Theme(
            Color titleTextColor,
            Color subtitleTextColor,
            Color codeBackgroundColor,
            Color codeTextColor,
            Color cardBorderColor,
            Color cardTextColor,

            ImageData backgroundFirstPageImage,
            ImageData backgroundImage,
            ImageData logoImage) {

        this.titleTextColor = titleTextColor;
        this.subtitleTextColor = subtitleTextColor;
        this.codeBackgroundColor = codeBackgroundColor;
        this.codeTextColor = codeTextColor;
        this.cardBorderColor = cardBorderColor;
        this.cardTextColor = cardTextColor;

        this.backgroundFirstPageImage = backgroundFirstPageImage;
        this.backgroundImage = backgroundImage;
        this.logoImage = logoImage;
    }
}
