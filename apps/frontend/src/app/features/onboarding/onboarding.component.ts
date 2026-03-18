import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'fc-onboarding',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<p>Benvenuto! (Onboarding — prossimamente)</p>`,
})
export class OnboardingComponent {}
