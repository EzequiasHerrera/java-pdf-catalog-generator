package service;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

public class TableBuilder {
    public static Table createConfiguredTable(float pageHeight, int productsPerPage) {
        if (productsPerPage <= 4) {
            return new Table(UnitValue
                    .createPercentArray(1)).useAllAvailableWidth();
        } else if (productsPerPage <= 12) {
            return new Table(UnitValue
                    .createPercentArray(3)).useAllAvailableWidth();
        } else {
            return new Table(UnitValue
                    .createPercentArray(5)).useAllAvailableWidth();
        }

    }

}
