export interface Ingredient {
  id: string;
  name: string;
  unit: string;
  price: number;
  createdAt: string;
  updatedAt: string;
}

export interface IngredientCreateRequest {
  name: string;
  unit: string;
  price: number;
}
