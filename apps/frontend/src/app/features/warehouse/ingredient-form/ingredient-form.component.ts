import {
  ChangeDetectionStrategy,
  Component,
  inject,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { IngredientService } from '../ingredient.service';

interface UnitGroup {
  label: string;
  units: { value: string; label: string }[];
}

@Component({
  selector: 'fc-ingredient-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ingredient-form.component.html',
  styleUrl: './ingredient-form.component.scss',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
})
export class IngredientFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly ingredientService = inject(IngredientService);
  private readonly snackBar = inject(MatSnackBar);

  readonly saved = output<void>();

  protected readonly isSubmitting = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly nameServerError = signal<string | null>(null);

  protected readonly unitGroups: UnitGroup[] = [
    {
      label: 'Peso',
      units: [
        { value: 'kg', label: 'kg' },
        { value: 'g', label: 'g' },
        { value: 'hg', label: 'hg' },
      ],
    },
    {
      label: 'Volume',
      units: [
        { value: 'l', label: 'l' },
        { value: 'cl', label: 'cl' },
        { value: 'ml', label: 'ml' },
      ],
    },
    {
      label: 'Unità',
      units: [
        { value: 'pz', label: 'pz' },
        { value: 'confezione', label: 'confezione' },
        { value: 'porzione', label: 'porzione' },
      ],
    },
  ];

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    unit: ['kg', [Validators.required]],
    price: [null as number | null, [Validators.required, Validators.min(0.0001)]],
  });

  protected submitted = false;

  submit(): void {
    this.submitted = true;
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.serverError.set(null);
    this.nameServerError.set(null);

    const { name, unit, price } = this.form.getRawValue();
    this.ingredientService.create({ name, unit, price: price! }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.snackBar.open('Ingrediente aggiunto', '', { duration: 3000 });
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.status === 422 && err.error?.detail) {
          this.nameServerError.set(err.error.detail);
        } else {
          this.serverError.set('Si è verificato un errore. Riprova.');
        }
      },
    });
  }
}
