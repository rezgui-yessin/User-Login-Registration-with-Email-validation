import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TokenService } from '../../services/token/token.service';

@Component({
  selector: 'app-mfa-setup',
  templateUrl: './mfa-setup.component.html',
  styleUrls: ['./mfa-setup.component.scss']
})
export class MfaSetupComponent implements OnInit {
  readonly apiBaseUrl = 'http://localhost:8080';

  email = '';
  verificationCode = '';
  secret = '';
  otpauthUrl = '';
  enabled = false;
  statusMessage = '';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {}

  ngOnInit(): void {
    const tokenEmail = this.tokenService.getEmail();
    this.email = tokenEmail || localStorage.getItem('userEmail') || '';

    if (!this.email) {
      this.statusMessage = 'No user email found. Please login again.';
    }
  }

  setupMfa(): void {
    if (!this.email) {
      this.statusMessage = 'Email is required to set up MFA.';
      return;
    }

    this.http.post<{ secret: string; otpauthUrl: string; enabled: boolean }>(
      `${this.apiBaseUrl}/auth/mfa/setup`,
      { email: this.email }
    ).subscribe({
      next: (res) => {
        this.secret = res.secret;
        this.otpauthUrl = res.otpauthUrl;
        this.enabled = res.enabled;
        this.statusMessage = 'Scan the QR code with your authenticator app.';
      },
      error: (err) => {
        console.error('MFA setup error:', err);
        this.statusMessage = err?.error?.message || 'Unable to set up MFA for this account.';
      }
    });
  }

  verifyMfa(): void {
    if (!this.email || !this.verificationCode) {
      this.statusMessage = 'Email and MFA code are required.';
      return;
    }

    this.http.post(
      `${this.apiBaseUrl}/auth/mfa/verify`,
      {
        email: this.email,
        code: this.verificationCode
      }
    ).subscribe({
      next: () => {
        this.enabled = true;
        this.statusMessage = '2FA verification successful. MFA is now enabled.';
      },
      error: (err) => {
        console.error('MFA verify error:', err);
        this.statusMessage = err?.error?.message || 'Invalid MFA code. Please try again.';
      }
    });
  }
}
