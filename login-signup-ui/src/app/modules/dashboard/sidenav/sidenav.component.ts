import {Component, EventEmitter, HostListener, OnInit, Output} from '@angular/core';
import {navbarData} from "./nav-data";
import {SideNavToggle} from "./sideNavTogle";
import {animate, animation, keyframes, style, transition, trigger} from "@angular/animations";


@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss'],
  animations: [
    // Add any animations you want to use here
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('350ms', style({ opacity: 1 }))
      ]),
      transition(':leave', [
        style({ opacity: 1 }),
        animate('350ms', style({ opacity: 0 }))
      ])
    ]),
    trigger('rotate', [
      transition(':enter', [
        animate('1000ms', keyframes([
          style({ transform: 'rotate(0deg)', offset: 0 }),
          style({ transform: 'rotate(2turn)', offset: 1 })
        ]))
      ])
    ])
  ]
})
export class SidenavComponent implements OnInit{
  @Output() onToggleSideNav:EventEmitter<SideNavToggle>=new EventEmitter()
  collapsed = false;
  screenWidth = 0;
  navData = navbarData;
  @HostListener('window:resize', ['$event'])// Listen to window resize event and update the screen width
  onResize(event: any) {
    this.screenWidth = window.innerWidth; // Update screen width on resize
    if (this.screenWidth <= 768) {
      this.collapsed = false; // Collapse the sidenav on small screens
      this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
    }
  }

  ngOnInit(): void {
    this.screenWidth = window.innerWidth; // Get the screen width on component initialization
  }


  toggleCollapse() {
    this.collapsed = !this.collapsed;
  //   Todo: Emit an event to the parent component
    this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});

  }

  closeSidenav() {
    this.collapsed = false;
    this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
  }


}
