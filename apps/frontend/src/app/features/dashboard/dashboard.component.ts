import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'fc-dashboard',
  template: `<div class="p-4 text-white">Dashboard (prossimamente)</div>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {}
