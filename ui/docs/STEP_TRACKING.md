# Step Tracking Feature

## Overview

The Step Tracking feature enhances the Status Tracker application by providing a visual and detailed way to track progress through multi-step workflows. This feature is particularly useful for complex financial processes that involve multiple stages, such as account opening, trade settlement, and portfolio rebalancing.

## Key Components

### 1. Step Indicator Component

The `StepIndicatorComponent` is a reusable Angular component that visualizes progress through a series of steps. It supports:

- Horizontal and vertical layouts
- Compact and detailed views
- Dynamic styling based on step status
- Progress calculation and visualization

```typescript
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
  @Input() compact: boolean = false;
  @Input() horizontal: boolean = true;
  
  // Methods for calculating progress, determining step status, etc.
}
```

### 2. Step Data Model

The Step model extends the Status data model to include step-specific information:

```typescript
export interface Step {
  stepId: string;
  name: string;
  description?: string;
  status: string; // 'Completed', 'In Progress', 'Not Started', 'Blocked', 'Skipped'
  order: number;
  startDate?: string;
  completionDate?: string;
  dueDate?: string;
  assignedTo?: string;
  notes?: string;
  metadata?: Record<string, string>;
}
```

### 3. Status Model Extensions

The Status model has been extended to include step tracking information:

```typescript
export interface Status {
  // Existing fields...
  
  // Step tracking fields
  steps?: Step[];
  currentStepId?: string;
  totalSteps?: number;
  completedSteps?: number;
}
```

## Integration Points

### Dashboard Component

The Dashboard component displays a compact step indicator for statuses that have steps:

```html
<div *ngIf="status.steps && status.steps.length > 0" class="status-steps">
  <div class="steps-header">
    <span class="steps-title">Progress</span>
    <span class="steps-count">{{ status.completedSteps || 0 }}/{{ status.totalSteps }}</span>
  </div>
  <app-step-indicator
    [steps]="status.steps"
    [currentStepId]="status.currentStepId"
    [totalSteps]="status.totalSteps || 0"
    [completedSteps]="status.completedSteps || 0"
    [horizontal]="true"
    [compact]="true">
  </app-step-indicator>
</div>
```

### Status Details Component

The Status Details component displays a detailed step indicator and step information:

```html
<div class="section" *ngIf="status.steps && status.steps.length > 0">
  <h3>Progress Tracker</h3>
  
  <!-- Horizontal Step Indicator -->
  <div class="horizontal-step-indicator">
    <app-step-indicator
      [steps]="status.steps"
      [currentStepId]="status.currentStepId"
      [totalSteps]="status.totalSteps || 0"
      [completedSteps]="status.completedSteps || 0"
      [horizontal]="true"
      [compact]="false">
    </app-step-indicator>
  </div>
  
  <!-- Step Details -->
  <div class="step-details-list">
    <!-- Step detail items -->
  </div>
</div>
```

## Styling

The step indicator uses a consistent color scheme to indicate step status:

- **Completed**: Green (#34c759)
- **In Progress**: Blue (#007aff)
- **Not Started**: Gray (#8e8e93)
- **Blocked**: Red (#ff3b30)
- **Skipped**: Orange (#ff9500)

## Usage Example

To add step tracking to a status, include the following data in the status object:

```javascript
{
  "statusId": "status-005",
  "statusSummary": "Investment Portfolio Rebalancing",
  // Other status fields...
  
  // Step tracking data
  "steps": [
    {
      "stepId": "step-001",
      "name": "Analysis",
      "description": "Analyze current portfolio allocation",
      "status": "Completed",
      "order": 1,
      "startDate": "2025-02-15T09:00:00Z",
      "completionDate": "2025-02-16T14:30:00Z",
      "assignedTo": "Sarah Johnson",
      "notes": "Current portfolio is 65% equities, 30% bonds, 5% cash"
    },
    // Additional steps...
  ],
  "currentStepId": "step-003",
  "totalSteps": 5,
  "completedSteps": 2
}
```

## Best Practices

1. **Step Order**: Always ensure steps have a logical order (1-indexed)
2. **Step IDs**: Use unique IDs for each step
3. **Current Step**: Always set the `currentStepId` to a valid step ID
4. **Step Counts**: Keep `totalSteps` and `completedSteps` in sync with the actual steps array
5. **Step Status**: Use consistent status values ('Completed', 'In Progress', 'Not Started', 'Blocked', 'Skipped')

## Future Enhancements

Planned enhancements for the step tracking feature include:

1. **Step Actions**: Add ability to take actions directly from steps (e.g., approve, reject)
2. **Step Dependencies**: Add support for step dependencies and prerequisites
3. **Step Notifications**: Add notifications for step status changes
4. **Step Templates**: Add support for predefined step templates for common workflows
5. **Step Analytics**: Add analytics to track step completion times and bottlenecks 