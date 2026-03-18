import { Routes } from '@angular/router';
import { RegisterComponent } from './register/register.component';

export const AUTH_ROUTES: Routes = [
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: RegisterComponent }, // stub: replaced in Story 1.3
  { path: '', redirectTo: 'register', pathMatch: 'full' },
];
