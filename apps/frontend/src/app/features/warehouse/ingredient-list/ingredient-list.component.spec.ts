import { TestBed, ComponentFixture } from '@angular/core/testing';
import { MatBottomSheet, MatBottomSheetRef } from '@angular/material/bottom-sheet';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { BreakpointObserver } from '@angular/cdk/layout';
import { EventEmitter } from '@angular/core';
import { IngredientListComponent } from './ingredient-list.component';
import { IngredientService } from '../ingredient.service';
import { IngredientFormComponent } from '../ingredient-form/ingredient-form.component';
import { Ingredient } from '../../../shared/models/ingredient.model';
import { of } from 'rxjs';

describe('IngredientListComponent', () => {
  let fixture: ComponentFixture<IngredientListComponent>;
  let component: IngredientListComponent;
  let ingredientService: IngredientService;
  let bottomSheet: MatBottomSheet;
  let dialog: MatDialog;
  let breakpointObserver: BreakpointObserver;

  const mockIngredients: Ingredient[] = [
    { id: '1', name: 'Farina 00', unit: 'kg', price: 1.25, createdAt: '2026-03-24T10:00:00Z', updatedAt: '2026-03-24T10:00:00Z' },
    { id: '2', name: 'Pomodoro', unit: 'kg', price: 2.5, createdAt: '2026-03-24T10:00:00Z', updatedAt: '2026-03-24T10:00:00Z' },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [IngredientListComponent],
      providers: [
        { provide: IngredientService, useValue: { getAll: vi.fn(() => of(mockIngredients)) } },
        { provide: MatBottomSheet, useValue: { open: vi.fn() } },
        { provide: MatDialog, useValue: { open: vi.fn() } },
        { provide: BreakpointObserver, useValue: { isMatched: vi.fn(() => false) } },
      ],
    });

    fixture = TestBed.createComponent(IngredientListComponent);
    component = fixture.componentInstance;
    ingredientService = TestBed.inject(IngredientService);
    bottomSheet = TestBed.inject(MatBottomSheet);
    dialog = TestBed.inject(MatDialog);
    breakpointObserver = TestBed.inject(BreakpointObserver);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('openEditForm', () => {
    const ingredient = mockIngredients[0];

    it('should open MatDialog on desktop with ingredient data', () => {
      vi.spyOn(breakpointObserver, 'isMatched').mockReturnValue(false);
      const savedEmitter = new EventEmitter<void>();
      const mockDialogRef = {
        componentInstance: { saved: savedEmitter },
        close: vi.fn(),
      } as unknown as MatDialogRef<IngredientFormComponent>;
      vi.spyOn(dialog, 'open').mockReturnValue(mockDialogRef);

      component.openEditForm(ingredient);

      expect(dialog.open).toHaveBeenCalledWith(IngredientFormComponent, {
        width: '480px',
        data: ingredient,
      });
    });

    it('should open MatBottomSheet on mobile with ingredient data', () => {
      vi.spyOn(breakpointObserver, 'isMatched').mockReturnValue(true);
      const savedEmitter = new EventEmitter<void>();
      const mockSheetRef = {
        instance: { saved: savedEmitter },
        dismiss: vi.fn(),
      } as unknown as MatBottomSheetRef<IngredientFormComponent>;
      vi.spyOn(bottomSheet, 'open').mockReturnValue(mockSheetRef);

      component.openEditForm(ingredient);

      expect(bottomSheet.open).toHaveBeenCalledWith(IngredientFormComponent, {
        data: ingredient,
      });
    });
  });

  describe('openAddForm', () => {
    it('should open MatDialog on desktop without data', () => {
      vi.spyOn(breakpointObserver, 'isMatched').mockReturnValue(false);
      const savedEmitter = new EventEmitter<void>();
      const mockDialogRef = {
        componentInstance: { saved: savedEmitter },
        close: vi.fn(),
      } as unknown as MatDialogRef<IngredientFormComponent>;
      vi.spyOn(dialog, 'open').mockReturnValue(mockDialogRef);

      component.openAddForm();

      expect(dialog.open).toHaveBeenCalledWith(IngredientFormComponent, {
        width: '480px',
      });
    });

    it('should open MatBottomSheet on mobile without data', () => {
      vi.spyOn(breakpointObserver, 'isMatched').mockReturnValue(true);
      const savedEmitter = new EventEmitter<void>();
      const mockSheetRef = {
        instance: { saved: savedEmitter },
        dismiss: vi.fn(),
      } as unknown as MatBottomSheetRef<IngredientFormComponent>;
      vi.spyOn(bottomSheet, 'open').mockReturnValue(mockSheetRef);

      component.openAddForm();

      expect(bottomSheet.open).toHaveBeenCalledWith(IngredientFormComponent);
    });
  });
});
