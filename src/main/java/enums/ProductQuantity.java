package enums;

public enum ProductQuantity {

    TWO(2, 380),
    FOUR(4, 190),
    TWELVE(12, 90),
    TWENTY(20, 60);

    private final int quantity;
    private final float imageSize;

    ProductQuantity(int quantity, float imageSize) {
        this.quantity = quantity;
        this.imageSize = imageSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getImageSize() {
        return imageSize;
    }

    public static ProductQuantity fromQuantity(int quantity) throws Exception {
        for (ProductQuantity pq : values()) {
            if (pq.quantity == quantity) {
                return pq;
            }
        }
        throw new Exception("La cantidad de productos debe ser: " + ProductQuantity.values());
    }

}
