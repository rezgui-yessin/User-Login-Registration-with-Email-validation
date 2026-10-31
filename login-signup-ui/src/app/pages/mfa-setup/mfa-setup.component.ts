import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TokenService } from '../../services/token/token.service';

@Component({
  selector: 'app-mfa-setup',
  templateUrl: './mfa-setup.component.html',
  styleUrls: ['./mfa-setup.component.scss']
})
export class MfaSetupComponent implements OnInit {
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
    this.email = this.tokenService.getEmail() || '';
  }

  setupMfa(): void {
    this.http.post<{ secret: string; otpauthUrl: string; enabled: boolean }>(
      '/auth/mfa/setup',
      { email: this.email }
    ).subscribe({
      next: (res) => {
        this.secret = res.secret;
        this.otpauthUrl = res.otpauthUrl;
        this.enabled = res.enabled;
        this.statusMessage = 'Scan the QR code with your authenticator app.';
      },
      error: () => {
        this.statusMessage = 'Unable to set up MFA for this account.';
      }
    });
  }

  verifyMfa(): void {
    this.http.post('/auth/mfa/verify', {
      email: this.email,
      code: this.verificationCode
    }).subscribe({
      next: () => {
        this.enabled = true;
        this.statusMessage = '2FA verification successful.';
      },
      error: () => {
        this.statusMessage = 'Invalid MFA code. Please try again.';
      }
    });
  }
}
