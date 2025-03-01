import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { StatusDetailsComponent } from './status-details.component';
import { SharedModule } from '../../shared/shared.module';

const routes: any[] = [
  { path: '', component: StatusDetailsComponent }
];

@NgModule({
  declarations: [
    StatusDetailsComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    SharedModule
  ]
})
export class StatusDetailsModule { } 