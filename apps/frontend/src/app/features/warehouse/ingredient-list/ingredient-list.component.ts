import {
  ChangeDetectionStrategy,
  Component,
  inject,
} from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatDialog } from '@angular/material/dialog';
import { BreakpointObserver } from '@angular/cdk/layout';
import { DecimalPipe } from '@angular/common';
import { IngredientService } from '../ingredient.service';
import { IngredientFormComponent } from '../ingredient-form/ingredient-form.component';
import { Ingredient } from '../../../shared/models/ingredient.model';

@Component({
  selector: 'fc-ingredient-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ingredient-list.component.html',
  styleUrl: './ingredient-list.component.scss',
  imports: [MatButtonModule, DecimalPipe],
})
export class IngredientListComponent {
  private readonly ingredientService = inject(IngredientService);
  private readonly bottomSheet = inject(MatBottomSheet);
  private readonly dialog = inject(MatDialog);
  private readonly breakpointObserver = inject(BreakpointObserver);

  protected readonly ingredients = rxResource({
    stream: () => this.ingredientService.getAll(),
  });

  openAddForm(): void {
    const isMobile = this.breakpointObserver.isMatched('(max-width: 767px)');

    if (isMobile) {
      const ref = this.bottomSheet.open(IngredientFormComponent);
      ref.instance.saved.subscribe(() => {
        ref.dismiss();
        this.ingredients.reload();
      });
    } else {
      const ref = this.dialog.open(IngredientFormComponent, {
        width: '480px',
      });
      ref.componentInstance.saved.subscribe(() => {
        ref.close();
        this.ingredients.reload();
      });
    }
  }

  openEditForm(ingredient: Ingredient): void {
    const isMobile = this.breakpointObserver.isMatched('(max-width: 767px)');

    if (isMobile) {
      const ref = this.bottomSheet.open(IngredientFormComponent, {
        data: ingredient,
      });
      ref.instance.saved.subscribe(() => {
        ref.dismiss();
        this.ingredients.reload();
      });
    } else {
      const ref = this.dialog.open(IngredientFormComponent, {
        width: '480px',
        data: ingredient,
      });
      ref.componentInstance.saved.subscribe(() => {
        ref.close();
        this.ingredients.reload();
      });
    }
  }
}
