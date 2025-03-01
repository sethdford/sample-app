import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StepIndicatorComponent } from '../components/step-indicator/step-indicator.component';

@NgModule({
  declarations: [
    StepIndicatorComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    StepIndicatorComponent
  ]
})
export class SharedModule { } 