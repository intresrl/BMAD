import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../auth.service';
import { TokenService } from '../../../core/services/token.service';

@Component({
  selector: 'fc-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    RouterLink,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly tokenService = inject(TokenService);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });
  protected readonly isSubmitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  private readonly returnUrl: string;

  constructor() {
    const raw = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard';
    this.returnUrl = raw.startsWith('/') && !raw.startsWith('//') ? raw : '/dashboard';
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);
    this.serverError.set(null);
    const { email, password } = this.form.getRawValue();
    this.authService.login(email, password).subscribe({
      next: (response) => {
        this.tokenService.setToken(response.accessToken);
        this.router.navigateByUrl(this.returnUrl);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.serverError.set('Credenziali non valide. Controlla email e password.');
      },
    });
  }
}
