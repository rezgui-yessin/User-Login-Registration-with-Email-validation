import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { TokenService } from '../../services/token/token.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {
  constructor(
    private router: Router,
    private tokenService: TokenService
  ) {}

  logout(): void {
    this.tokenService.clear();
    this.router.navigate(['/login']);
  }

  goToMfaSetup(): void {
    this.router.navigate(['/mfa-setup']);
  }

  goToSessionManagement(): void {
    this.router.navigate(['/dashboard/sessions']);
  }
}
