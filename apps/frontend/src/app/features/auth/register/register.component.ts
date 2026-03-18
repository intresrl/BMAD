import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../auth.service';
import { TokenService } from '../../../core/services/token.service';

@Component({
  selector: 'fc-register',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });
  protected readonly isSubmitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);
    this.serverError.set(null);
    const { email, password } = this.form.getRawValue();
    this.authService.register(email, password).subscribe({
      next: (response) => {
        this.tokenService.setToken(response.accessToken);
        this.router.navigate(['/onboarding']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.serverError.set(
          err?.error?.detail ?? 'Errore durante la registrazione. Riprova.',
        );
      },
    });
  }
}
