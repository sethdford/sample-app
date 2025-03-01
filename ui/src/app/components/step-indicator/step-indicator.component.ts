import { Component, Input } from '@angular/core';
import { Step } from '../../models/status.model';

@Component({
  selector: 'app-step-indicator',
  templateUrl: './step-indicator.component.html',
  styleUrls: ['./step-indicator.component.scss']
})
export class StepIndicatorComponent {
  @Input() steps: Step[] = [];
  @Input() currentStepId: string | undefined;
  @Input() totalSteps: number = 0;
  @Input() completedSteps: number = 0;
  @Input() compact: boolean = false; // For dashboard view
  @Input() horizontal: boolean = true; // For left-to-right layout

  // Get the current step number (1-indexed)
  getCurrentStepNumber(): number {
    if (!this.steps || !this.currentStepId) {
      return 1;
    }
    
    const currentStep = this.steps.find(step => step.stepId === this.currentStepId);
    return currentStep ? currentStep.order : 1;
  }
  
  // Calculate the progress percentage
  getProgressPercentage(): number {
    if (!this.totalSteps || this.totalSteps === 0) {
      return 0;
    }
    
    return (this.completedSteps || 0) / this.totalSteps * 100;
  }
  
  // Get CSS class for step number indicator based on status
  getStepStatusClass(step: Step): string {
    if (step.status === 'Completed') {
      return 'step-completed';
    } else if (step.status === 'In Progress') {
      return 'step-in-progress';
    } else if (step.status === 'Blocked') {
      return 'step-blocked';
    } else if (step.status === 'Skipped') {
      return 'step-skipped';
    } else {
      return 'step-not-started';
    }
  }
  
  // Get CSS class for step connector
  getStepConnectorClass(step: Step): string {
    if (step.status === 'Completed') {
      return 'connector-completed';
    } else if (step.status === 'In Progress') {
      return 'connector-in-progress';
    } else {
      return 'connector-not-started';
    }
  }
  
  // Check if this is the last step
  isLastStep(step: Step): boolean {
    if (!this.steps || this.steps.length === 0) {
      return true;
    }
    
    const sortedSteps = [...this.steps].sort((a, b) => b.order - a.order);
    return sortedSteps.length > 0 && step.order === sortedSteps[0].order;
  }
}
