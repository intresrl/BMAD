import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { noAuthGuard } from './no-auth.guard';
import { TokenService } from '../services/token.service';

describe('noAuthGuard', () => {
  let tokenService: TokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: TokenService,
          useValue: { isAuthenticated: vi.fn() },
        },
      ],
    });

    tokenService = TestBed.inject(TokenService);
  });

  it('should allow access when user is not authenticated', () => {
    vi.spyOn(tokenService, 'isAuthenticated').mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      noAuthGuard({} as never, {} as never),
    );

    expect(result).toBe(true);
  });

  it('should redirect to /dashboard when user is authenticated', () => {
    vi.spyOn(tokenService, 'isAuthenticated').mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      noAuthGuard({} as never, {} as never),
    );

    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/dashboard');
  });
});
