package enums;

import com.itextpdf.kernel.geom.PageSize;

public enum PageType {

    A4(PageSize.A4.getWidth(), PageSize.A4.getHeight()),
    A3(PageSize.A3.getWidth(), PageSize.A3.getHeight()),
    LETTER(PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight()),
    LEGAL(PageSize.LEGAL.getWidth(), PageSize.LEGAL.getHeight()),
    A5(PageSize.A5.getWidth(), PageSize.A5.getHeight()),
    A6(PageSize.A6.getWidth(), PageSize.A6.getHeight()),
    TABLOID(PageSize.TABLOID.getWidth(), PageSize.TABLOID.getHeight()),
    EXECUTIVE(PageSize.EXECUTIVE.getWidth(), PageSize.EXECUTIVE.getHeight());

    private final float width;
    private final float height;

    PageType(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public PageSize toPageSize() {
        return new PageSize(width, height);
    }

    public PageSize toLandscape() {
        return new PageSize(height, width);
    }

}
