package utils;

public class ValidationUtils {

    /**
     * Verifica si un string es un número válido (float positivo)
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null || strNum.trim().isEmpty()) {
            return false;
        }
        try {
            float value = Float.parseFloat(strNum);
            return value >= 0 && !Float.isInfinite(value) && !Float.isNaN(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
