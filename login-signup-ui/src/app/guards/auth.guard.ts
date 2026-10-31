import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { TokenService } from '../services/token/token.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const tokenService = inject(TokenService);

  if (tokenService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const tokenService = inject(TokenService);

  if (!tokenService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  if (tokenService.isAdmin()) {
    return true;
  }

  router.navigate(['/dashboard']);
  return false;
};

export const userGuard: CanActivateFn = () => {
  const router = inject(Router);
  const tokenService = inject(TokenService);

  if (tokenService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
