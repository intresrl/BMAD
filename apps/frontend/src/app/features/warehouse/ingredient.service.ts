import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Ingredient, IngredientCreateRequest, IngredientUpdateRequest } from '../../shared/models/ingredient.model';

@Injectable({ providedIn: 'root' })
export class IngredientService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<Ingredient[]> {
    return this.http.get<Ingredient[]>('/api/v1/ingredients');
  }

  create(request: IngredientCreateRequest): Observable<Ingredient> {
    return this.http.post<Ingredient>('/api/v1/ingredients', request);
  }

  update(id: string, request: IngredientUpdateRequest): Observable<Ingredient> {
    return this.http.put<Ingredient>(`/api/v1/ingredients/${id}`, request);
  }
}
