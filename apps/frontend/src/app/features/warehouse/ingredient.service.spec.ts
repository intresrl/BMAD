import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { IngredientService } from './ingredient.service';
import { Ingredient } from '../../shared/models/ingredient.model';

describe('IngredientService', () => {
  let service: IngredientService;
  let httpMock: HttpTestingController;

  const mockIngredient: Ingredient = {
    id: '123',
    name: 'Farina 00',
    unit: 'kg',
    price: 1.25,
    createdAt: '2026-03-24T10:00:00Z',
    updatedAt: '2026-03-24T10:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(IngredientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAll', () => {
    it('should GET /api/v1/ingredients', () => {
      service.getAll().subscribe((result) => {
        expect(result).toEqual([mockIngredient]);
      });

      const req = httpMock.expectOne('/api/v1/ingredients');
      expect(req.request.method).toBe('GET');
      req.flush([mockIngredient]);
    });
  });

  describe('create', () => {
    it('should POST /api/v1/ingredients with request body', () => {
      const request = { name: 'Farina 00', unit: 'kg', price: 1.25 };

      service.create(request).subscribe((result) => {
        expect(result).toEqual(mockIngredient);
      });

      const req = httpMock.expectOne('/api/v1/ingredients');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockIngredient);
    });
  });

  describe('update', () => {
    it('should PUT /api/v1/ingredients/{id} with request body', () => {
      const request = { name: 'Farina 0', unit: 'g', price: 2.0 };
      const updated = { ...mockIngredient, ...request };

      service.update('123', request).subscribe((result) => {
        expect(result).toEqual(updated);
      });

      const req = httpMock.expectOne('/api/v1/ingredients/123');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(request);
      req.flush(updated);
    });
  });
});
