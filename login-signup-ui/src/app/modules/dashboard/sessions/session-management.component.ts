import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TokenService } from '../../../services/token/token.service';

@Component({
  selector: 'app-session-management',
  templateUrl: './session-management.component.html',
  styleUrls: ['./session-management.component.scss']
})
export class SessionManagementComponent {
  email = '';
  message = '';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {
    this.email = this.tokenService.getEmail();
  }

  revokeAllSessions(): void {
    this.http.post('/auth/sessions/revoke-all', { email: this.email }).subscribe({
      next: () => {
        this.message = 'All sessions were revoked successfully.';
        this.tokenService.clear();
        window.location.href = '/login';
      },
      error: () => {
        this.message = 'Unable to revoke sessions right now.';
      }
    });
  }
}
