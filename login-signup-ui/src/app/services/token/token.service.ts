import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  set accessToken(token: string) {
    localStorage.setItem('accessToken', token);
  }

  get accessToken(): string {
    return localStorage.getItem('accessToken') || '';
  }

  set refreshToken(token: string) {
    localStorage.setItem('refreshToken', token);
  }

  get refreshToken(): string {
    return localStorage.getItem('refreshToken') || '';
  }

  set token(token: string) {
    localStorage.setItem('token', token);
  }

  get token(): string {
    return localStorage.getItem('token') || '';
  }

  clear(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  isLoggedIn(): boolean {
    return !!(this.accessToken || this.token);
  }

  getTokenPayload(): any {
    const token = this.accessToken || this.token;
    if (!token) {
      return {};
    }

    try {
      const payload = token.split('.')[1];
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(normalized));
    } catch (error) {
      return {};
    }
  }

  getRoles(): string[] {
    const payload = this.getTokenPayload();
    const authorities = payload.authorities ?? payload.roles ?? [];
    if (Array.isArray(authorities)) {
      return authorities;
    }
    return authorities ? [authorities] : [];
  }

  hasRole(role: string): boolean {
    const roleName = role.toUpperCase();
    return this.getRoles().some((r: string) => r.toUpperCase().includes(roleName));
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  getEmail(): string {
    return this.getTokenPayload().sub || this.getTokenPayload().email || '';
  }
}
