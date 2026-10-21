package lab.shop.orders;

/** An item in the Catalog that can be ordered. Price is in whole euro cents. */
record Product(String id, String name, long priceCents) {
}
