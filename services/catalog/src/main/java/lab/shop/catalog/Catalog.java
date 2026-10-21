package lab.shop.catalog;

import java.util.List;

import org.springframework.stereotype.Component;

/** The fixed, read-only set of Products that can be ordered. */
@Component
class Catalog {

	private static final List<Product> PRODUCTS = List.of(
			new Product("p-mug", "Coffee Mug", 1250),
			new Product("p-tee", "Lab T-Shirt", 2400),
			new Product("p-sticker", "Sticker Pack", 499),
			new Product("p-notebook", "Notebook", 899),
			new Product("p-bottle", "Water Bottle", 1999));

	List<Product> all() {
		return PRODUCTS;
	}

}
