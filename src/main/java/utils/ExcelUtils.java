package utils;

import org.apache.poi.ss.usermodel.*;

public class ExcelUtils {

    // Función que detecta si una fila está vacía
    public static boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            final org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                // Si es string, comprobar que no sea solo espacios
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
                    continue; // sigue buscando
                }
                return false; // hay contenido real
            }
        }
        return true;
    }

    // Función que devuelve el valor de una celda
    public static String getCellValue(Cell cell) throws Exception {
        if (cell == null) {
            return "";
        }

        final CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) { // date
                    return cell.getDateCellValue().toString();
                } else { // numeric
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue();
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            return cell.getDateCellValue().toString();
                        } else {
                            return String.valueOf(cell.getNumericCellValue());
                        }
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    case ERROR:
                        return "0";
                    default:
                        return "";
                }
            case ERROR:
                throw new Exception("Error en la celda fila: " + cell.getAddress().getRow() + 1 + " columna: "
                        + cell.getAddress().getColumn() + 1);
            case BLANK:
            default:
                return "";
        }
    }

    // Verifica si el Excel contiene las 4 columnas CODIGO PRODUCTO PRECIO UXB
    public static boolean isValidExcel(Row firstRow) throws Exception {
        if (firstRow == null || firstRow.getLastCellNum() < 4) {
            throw new Exception("Verifique que la hoja tenga los 4 encabezados en orden: 'Código', 'Nombre', 'Precio' y 'Unidad por Bulto'.");
        }
        return true;
    }

    // Cuenta cantidad de productos en el Excel
    public static int countRowsInFile(Sheet sheet) {
        int rowCount = 0;
        for (Row row : sheet) {
            if (row != null && !ExcelUtils.isEmptyRow(row)) {
                rowCount++;
            }
        }

        return rowCount;
    }

}
