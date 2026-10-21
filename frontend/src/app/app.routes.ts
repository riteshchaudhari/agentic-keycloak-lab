import { Routes } from '@angular/router';

import { Products } from './products';

// The shell renders routes only once logged in; logged out, the visitor sees just the login button.
export const routes: Routes = [
  { path: '', component: Products },
  { path: '**', redirectTo: '' },
];
