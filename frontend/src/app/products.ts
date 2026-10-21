import { CurrencyPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

/** An item in the Catalog that can be ordered. Price is in whole euro cents. */
interface Product {
  id: string;
  name: string;
  priceCents: number;
}

@Component({
  selector: 'app-products',
  imports: [CurrencyPipe],
  template: `
    <h1>Products</h1>
    <ul data-testid="products">
      @for (product of products(); track product.id) {
        <li>
          <span class="name">{{ product.name }}</span>
          <span class="price">{{ product.priceCents / 100 | currency: 'EUR' : 'symbol' : '1.2-2' : 'en' }}</span>
        </li>
      }
    </ul>
  `,
})
export class Products {
  protected readonly products = toSignal(inject(HttpClient).get<Product[]>('/api/products'), { initialValue: [] });
}
