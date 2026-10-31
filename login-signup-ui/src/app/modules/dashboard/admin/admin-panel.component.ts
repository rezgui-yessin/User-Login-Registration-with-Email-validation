import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-panel',
  templateUrl: './admin-panel.component.html',
  styleUrls: ['./admin-panel.component.scss']
})
export class AdminPanelComponent {
  adminStats = [
    { label: 'Total Users', value: '1,284' },
    { label: 'Active Sessions', value: '348' },
    { label: 'Failed Logins', value: '19' },
    { label: 'Open Tickets', value: '6' }
  ];
}
