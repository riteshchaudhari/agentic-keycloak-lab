package lab.shop.catalog;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CatalogController {

	private final Catalog catalog;

	CatalogController(Catalog catalog) {
		this.catalog = catalog;
	}

	@GetMapping("/products")
	List<Product> products() {
		return catalog.all();
	}

}
