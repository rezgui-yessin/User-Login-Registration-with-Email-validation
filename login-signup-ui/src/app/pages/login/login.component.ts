import { Component } from '@angular/core';
import { LoginRequest } from '../../services/models/login-request';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../services/services/authentication.service';
import { TokenService } from '../../services/token/token.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  authRequest: LoginRequest = { email: '', password: '', mfaCode: '' };
  errorMsg: Array<string> = [];

  constructor(
    private router: Router,
    private authService: AuthenticationService,
    private tokenService: TokenService
  ) { }

  login() {
    this.errorMsg = [];
    this.authService.login({ body: this.authRequest }).subscribe({
      next: (res) => {
        const accessToken = res.accessToken ?? res.token;
        const refreshToken = res.refreshToken ?? '';

        if (accessToken) {
          this.tokenService.accessToken = accessToken;
          this.tokenService.token = accessToken;
        }

        if (refreshToken) {
          this.tokenService.refreshToken = refreshToken;
        }

        this.router.navigate(['dashboard']);
      },
      error: (err) => {
        console.log(err);
        if (err.error?.validationErrors) {
          this.errorMsg = Array.from(err.error.validationErrors);
        } else if (err.error?.error) {
          this.errorMsg.push(err.error.error);
        } else if (err.error?.businessErrorDescription) {
          this.errorMsg.push(err.error.businessErrorDescription);
        } else if (err.error?.message) {
          this.errorMsg.push(err.error.message);
        } else {
          this.errorMsg.push('An unexpected error occurred');
        }
      }
    });
  }

  register() {
    this.router.navigate(['/register']);
  }
}
