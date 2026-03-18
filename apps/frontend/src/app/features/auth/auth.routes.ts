import { Routes } from '@angular/router';
import { RegisterComponent } from './register/register.component';

export const AUTH_ROUTES: Routes = [
  { path: 'register', component: RegisterComponent },
  { path: 'login', loadComponent: () => import('./login/login.component').then(m => m.LoginComponent) },
  { path: 'forgot-password', component: RegisterComponent }, // stub — replaced in Story 1.4
  { path: '', redirectTo: 'register', pathMatch: 'full' },
];
