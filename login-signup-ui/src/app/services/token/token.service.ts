import { AuthenticationService } from '../services/services';

export class TokenService {
  set token(token: string) {
    localStorage.setItem('token', token);
  }

  get token(): string {
    return localStorage.getItem('token') as string;
  }

  set accessToken(token: string) {
    localStorage.setItem('accessToken', token);
  }

  get accessToken(): string {
    return localStorage.getItem('accessToken') as string;
  }

  set refreshToken(token: string) {
    localStorage.setItem('refreshToken', token);
  }

  get refreshToken(): string {
    return localStorage.getItem('refreshToken') as string;
  }

  clear() {
    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }
}
