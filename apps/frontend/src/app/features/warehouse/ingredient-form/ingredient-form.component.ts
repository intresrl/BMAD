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
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MAT_BOTTOM_SHEET_DATA } from '@angular/material/bottom-sheet';
import { HttpErrorResponse } from '@angular/common/http';
import { IngredientService } from '../ingredient.service';
import { Ingredient } from '../../../shared/models/ingredient.model';

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
  private readonly dialogData = inject<Ingredient | null>(MAT_DIALOG_DATA, { optional: true });
  private readonly bottomSheetData = inject<Ingredient | null>(MAT_BOTTOM_SHEET_DATA, { optional: true });

  private readonly editIngredient = this.dialogData ?? this.bottomSheetData ?? null;
  protected readonly isEditMode = !!this.editIngredient;

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

  constructor() {
    if (this.editIngredient) {
      this.form.patchValue({
        name: this.editIngredient.name,
        unit: this.editIngredient.unit,
        price: this.editIngredient.price,
      });
    }

    this.form.controls.name.valueChanges.subscribe(() => {
      if (this.nameServerError()) {
        this.nameServerError.set(null);
      }
    });
  }

  submit(): void {
    this.submitted = true;
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.serverError.set(null);
    this.nameServerError.set(null);

    const { name, unit, price } = this.form.getRawValue();
    const obs = this.editIngredient
      ? this.ingredientService.update(this.editIngredient.id, { name, unit, price: price! })
      : this.ingredientService.create({ name, unit, price: price! });

    obs.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.snackBar.open(
          this.isEditMode ? 'Ingrediente aggiornato' : 'Ingrediente aggiunto',
          '',
          { duration: 3000 },
        );
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
