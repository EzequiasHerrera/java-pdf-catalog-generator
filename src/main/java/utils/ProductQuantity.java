package utils;

public enum ProductQuantity {

    TWO(2, "380"),
    FOUR(4, "190"),
    TWELVE(12, "90"),
    TWENTY(20, "60");

    private final int quantity;
    private final String imageSize;

    ProductQuantity(int quantity, String imageSize) {
        this.quantity = quantity;
        this.imageSize = imageSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageSize() {
        return imageSize;
    }

    public static ProductQuantity fromQuantity(int quantity) throws Exception {
        for (ProductQuantity pq : values()) {
            if (pq.quantity == quantity) {
                return pq;
            }
        }
        throw new Exception("La cantidad de productos debe ser 2, 4, 12 o 20.");
    }

}
