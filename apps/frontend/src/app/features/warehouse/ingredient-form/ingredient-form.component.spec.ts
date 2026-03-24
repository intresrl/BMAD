import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MAT_BOTTOM_SHEET_DATA } from '@angular/material/bottom-sheet';
import { of, throwError } from 'rxjs';
import { IngredientFormComponent } from './ingredient-form.component';
import { IngredientService } from '../ingredient.service';
import { Ingredient } from '../../../shared/models/ingredient.model';

describe('IngredientFormComponent', () => {
  const mockIngredient: Ingredient = {
    id: '123',
    name: 'Farina 00',
    unit: 'kg',
    price: 1.25,
    createdAt: '2026-03-24T10:00:00Z',
    updatedAt: '2026-03-24T10:00:00Z',
  };

  function createComponent(dialogData: Ingredient | null = null, bottomSheetData: Ingredient | null = null) {
    TestBed.configureTestingModule({
      imports: [IngredientFormComponent],
      providers: [
        { provide: IngredientService, useValue: { create: vi.fn(), update: vi.fn() } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
        { provide: MAT_DIALOG_DATA, useValue: dialogData },
        { provide: MAT_BOTTOM_SHEET_DATA, useValue: bottomSheetData },
      ],
    });

    const fixture = TestBed.createComponent(IngredientFormComponent);
    fixture.detectChanges();
    return {
      fixture,
      component: fixture.componentInstance,
      ingredientService: TestBed.inject(IngredientService),
      snackBar: TestBed.inject(MatSnackBar),
    };
  }

  describe('create mode (no data injected)', () => {
    it('should create in create mode when no data injected', () => {
      const { component } = createComponent();
      expect(component).toBeTruthy();
      expect((component as any).isEditMode).toBe(false);
    });

    it('should have empty form fields in create mode', () => {
      const { component } = createComponent();
      const form = (component as any).form;
      expect(form.controls.name.value).toBe('');
      expect(form.controls.unit.value).toBe('kg');
      expect(form.controls.price.value).toBeNull();
    });

    it('should call service.create on submit in create mode', () => {
      const { component, ingredientService } = createComponent();
      vi.spyOn(ingredientService, 'create').mockReturnValue(of(mockIngredient));

      const form = (component as any).form;
      form.patchValue({ name: 'Farina 00', unit: 'kg', price: 1.25 });
      component.submit();

      expect(ingredientService.create).toHaveBeenCalledWith({ name: 'Farina 00', unit: 'kg', price: 1.25 });
    });

    it('should show snackbar "Ingrediente aggiunto" on create success', () => {
      const { component, ingredientService, snackBar } = createComponent();
      vi.spyOn(ingredientService, 'create').mockReturnValue(of(mockIngredient));

      const form = (component as any).form;
      form.patchValue({ name: 'Farina 00', unit: 'kg', price: 1.25 });
      component.submit();

      expect(snackBar.open).toHaveBeenCalledWith('Ingrediente aggiunto', '', { duration: 3000 });
    });
  });

  describe('edit mode (dialog data)', () => {
    it('should create in edit mode when dialog data injected', () => {
      const { component } = createComponent(mockIngredient);
      expect((component as any).isEditMode).toBe(true);
    });

    it('should pre-populate form with ingredient data', () => {
      const { component } = createComponent(mockIngredient);
      const form = (component as any).form;
      expect(form.controls.name.value).toBe('Farina 00');
      expect(form.controls.unit.value).toBe('kg');
      expect(form.controls.price.value).toBe(1.25);
    });

    it('should call service.update on submit in edit mode', () => {
      const { component, ingredientService } = createComponent(mockIngredient);
      vi.spyOn(ingredientService, 'update').mockReturnValue(of(mockIngredient));

      const form = (component as any).form;
      form.patchValue({ name: 'Farina 0', unit: 'g', price: 2.0 });
      component.submit();

      expect(ingredientService.update).toHaveBeenCalledWith('123', { name: 'Farina 0', unit: 'g', price: 2.0 });
    });

    it('should show snackbar "Ingrediente aggiornato" on edit success', () => {
      const { component, ingredientService, snackBar } = createComponent(mockIngredient);
      vi.spyOn(ingredientService, 'update').mockReturnValue(of(mockIngredient));

      component.submit();

      expect(snackBar.open).toHaveBeenCalledWith('Ingrediente aggiornato', '', { duration: 3000 });
    });
  });

  describe('edit mode (bottom sheet data)', () => {
    it('should create in edit mode when bottom sheet data injected', () => {
      const { component } = createComponent(null, mockIngredient);
      expect((component as any).isEditMode).toBe(true);
    });

    it('should pre-populate form from bottom sheet data', () => {
      const { component } = createComponent(null, mockIngredient);
      const form = (component as any).form;
      expect(form.controls.name.value).toBe('Farina 00');
    });
  });

  describe('validation', () => {
    it('should not submit when form is invalid', () => {
      const { component, ingredientService } = createComponent();

      component.submit();

      expect(ingredientService.create).not.toHaveBeenCalled();
    });

    it('should not submit with zero price', () => {
      const { component, ingredientService } = createComponent();
      const form = (component as any).form;
      form.patchValue({ name: 'Test', unit: 'kg', price: 0 });

      component.submit();

      expect(ingredientService.create).not.toHaveBeenCalled();
    });
  });

  describe('error handling', () => {
    it('should set nameServerError on 422 response', () => {
      const { component, ingredientService } = createComponent();
      const errorResponse = { status: 422, error: { detail: 'Duplicate name' } };
      vi.spyOn(ingredientService, 'create').mockReturnValue(
        throwError(() => errorResponse),
      );

      const form = (component as any).form;
      form.patchValue({ name: 'Pomodoro', unit: 'kg', price: 2.5 });
      component.submit();

      expect((component as any).nameServerError()).toBe('Duplicate name');
    });

    it('should clear nameServerError when name field changes (M3 fix)', () => {
      const { component, ingredientService } = createComponent();
      const errorResponse = { status: 422, error: { detail: 'Duplicate name' } };
      vi.spyOn(ingredientService, 'create').mockReturnValue(
        throwError(() => errorResponse),
      );

      const form = (component as any).form;
      form.patchValue({ name: 'Pomodoro', unit: 'kg', price: 2.5 });
      component.submit();
      expect((component as any).nameServerError()).toBe('Duplicate name');

      form.controls.name.setValue('Basilico');
      expect((component as any).nameServerError()).toBeNull();
    });

    it('should set generic server error on non-422 response', () => {
      const { component, ingredientService } = createComponent();
      vi.spyOn(ingredientService, 'create').mockReturnValue(
        throwError(() => ({ status: 500 })),
      );

      const form = (component as any).form;
      form.patchValue({ name: 'Test', unit: 'kg', price: 1.0 });
      component.submit();

      expect((component as any).serverError()).toBe('Si è verificato un errore. Riprova.');
    });
  });
});
