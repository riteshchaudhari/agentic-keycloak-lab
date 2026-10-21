package lab.shop.orders;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ProductsController {

	private final CatalogClient catalog;

	ProductsController(CatalogClient catalog) {
		this.catalog = catalog;
	}

	@GetMapping("/api/products")
	List<Product> products() {
		return catalog.products();
	}

}
