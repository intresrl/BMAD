import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { noAuthGuard } from './core/guards/no-auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
    canActivate: [noAuthGuard],
  },
  {
    path: 'onboarding',
    loadComponent: () =>
      import('./features/onboarding/onboarding.component').then(
        (m) => m.OnboardingComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'dashboard',
    loadChildren: () =>
      import('./features/dashboard/dashboard.routes').then(
        (m) => m.DASHBOARD_ROUTES,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'warehouse',
    loadChildren: () =>
      import('./features/warehouse/warehouse.routes').then(
        (m) => m.WAREHOUSE_ROUTES,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'recipes',
    loadChildren: () =>
      import('./features/recipes/recipes.routes').then(
        (m) => m.RECIPES_ROUTES,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'scans',
    loadChildren: () =>
      import('./features/scans/scans.routes').then((m) => m.SCANS_ROUTES),
    canActivate: [authGuard],
  },
  {
    path: 'report',
    loadChildren: () =>
      import('./features/report/report.routes').then((m) => m.REPORT_ROUTES),
    canActivate: [authGuard],
  },
  {
    path: 'admin',
    loadChildren: () =>
      import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
    canActivate: [authGuard],
  },
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: 'auth/login' },
];
