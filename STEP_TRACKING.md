# Step Tracking Feature

This document provides detailed information about the step tracking feature implemented in the Status Tracker application.

## Overview

The step tracking feature allows users to track the progress of multi-step workflows within the Status Tracker application. Each status can have a series of steps, each with its own status, metadata, and progress tracking.

## Key Features

1. **Visual Progress Tracking**: Horizontal and vertical step indicators
2. **Step Status Management**: Complete, skip, or block steps
3. **Step Metadata**: Store and display step-specific information
4. **Embedding Integration**: Embedding-driven insights for each step
5. **Responsive Design**: Mobile-friendly step indicators

## Data Model

Each status can have an array of steps with the following structure:

```typescript
interface Step {
  id: string;
  name: string;
  description: string;
  status: 'COMPLETED' | 'IN_PROGRESS' | 'NOT_STARTED' | 'BLOCKED' | 'SKIPPED';
  startedAt?: number;
  completedAt?: number;
  dueDate?: number;
  assignedTo?: string;
  metadata?: {
    [key: string]: any;
  };
}

interface Status {
  // ... existing status fields
  steps?: Step[];
  currentStepId?: string;
  totalSteps?: number;
  completedSteps?: number;
}
```

## UI Components

### Step Indicator Component

The `StepIndicatorComponent` is a reusable Angular component that displays a visual representation of steps and their statuses.

#### Properties

- `steps`: Array of Step objects
- `currentStepId`: ID of the current active step
- `totalSteps`: Total number of steps
- `completedSteps`: Number of completed steps
- `horizontal`: Boolean to toggle between horizontal and vertical layout
- `compact`: Boolean to toggle between compact and detailed view

#### Usage

```html
<app-step-indicator
  [steps]="status.steps"
  [currentStepId]="status.currentStepId"
  [totalSteps]="status.totalSteps"
  [completedSteps]="status.completedSteps"
  [horizontal]="true"
  [compact]="false">
</app-step-indicator>
```

### Step Details Component

The Status Details page includes a detailed view of each step, including:

1. Step name and description
2. Step status with color coding
3. Step metadata (custom fields)
4. Step actions (complete, skip, block)
5. Step timeline (started, completed, due dates)

## Dashboard Integration

The Dashboard displays a compact step indicator for statuses that have steps, showing:

1. Progress percentage
2. Number of completed steps vs. total steps
3. Visual indicator of step statuses
4. Color coding for overall progress

## API Methods

The Status Service includes the following methods for step management:

```typescript
// Get step status class
getStepStatusClass(step: Step): string

// Get step connector class
getStepConnectorClass(index: number, steps: Step[]): string

// Calculate progress percentage
getProgressPercentage(completedSteps: number, totalSteps: number): number

// Get current step number
getCurrentStepNumber(steps: Step[], currentStepId: string): number

// Complete a step
completeStep(statusId: string, stepId: string): Observable<Status>

// Skip a step
skipStep(statusId: string, stepId: string): Observable<Status>

// Block a step
blockStep(statusId: string, stepId: string): Observable<Status>
```

## EmbeddingFunction Integration

Each step can include embedding-driven insights in its metadata:

```json
{
  "stepId": "step-001",
  "name": "Portfolio Analysis",
  "status": "COMPLETED",
  "metadata": {
    "currentAllocation": "70% stocks, 20% bonds, 10% cash",
    "performanceYTD": "+8.3%",
    "riskAssessment": "Slightly higher risk than target profile",
    "embeddingInsight": "Client profile embedding indicates moderate risk tolerance with focus on growth",
    "similarClients": ["client-003", "client-042", "client-078"],
    "similarityScores": [0.92, 0.87, 0.85]
  }
}
```

The EmbeddingFunction analyzes each step and provides:

1. Recommendations based on similar workflows
2. Risk assessments based on client profiles
3. Optimization suggestions for step execution
4. Predictions for step completion times

## CSS Styling

The step indicator uses a consistent color scheme:

- **Completed**: Green (#00AA55)
- **In Progress**: Blue (#0066CC)
- **Not Started**: Gray (#666666)
- **Blocked**: Red (#FF5555)
- **Skipped**: Orange (#FFAA00)

Responsive design ensures proper display on all devices:

```css
/* Horizontal layout for larger screens */
@media (min-width: 768px) {
  .step-indicator.horizontal {
    flex-direction: row;
  }
}

/* Vertical layout for smaller screens */
@media (max-width: 767px) {
  .step-indicator.horizontal {
    flex-direction: column;
  }
}
```

## Example Workflows

### 1. Investment Portfolio Rebalancing

A typical investment portfolio rebalancing workflow includes these steps:

1. **Portfolio Analysis**: Analyze current portfolio allocation and performance
2. **Strategy Development**: Develop rebalancing strategy based on client profile
3. **Client Approval**: Present strategy to client for approval
4. **Trade Execution**: Execute trades to implement the strategy
5. **Performance Review**: Review portfolio after rebalancing

### 2. Client Onboarding

A client onboarding workflow includes these steps:

1. **Document Collection**: Gather required client documents
2. **KYC/AML Verification**: Complete Know Your Customer and Anti-Money Laundering checks
3. **Account Setup**: Create and configure client accounts
4. **Advisor Assignment**: Assign appropriate financial advisor
5. **Initial Consultation**: Conduct initial client consultation
6. **Investment Strategy**: Develop initial investment strategy

## Best Practices

1. **Step Naming**: Use clear, concise names for steps
2. **Step Descriptions**: Provide detailed descriptions for each step
3. **Metadata Usage**: Use metadata for step-specific information
4. **Status Updates**: Keep step statuses up-to-date
5. **Due Dates**: Set realistic due dates for steps
6. **Assignment**: Assign steps to specific team members

## Implementation Details

### Angular Components

The step tracking feature includes the following Angular components:

1. `StepIndicatorComponent`: Reusable component for displaying step progress
2. `StatusDetailsComponent`: Enhanced to display step details
3. `DashboardComponent`: Updated to show step indicators for applicable statuses

### Services

The following services support step tracking:

1. `StatusService`: Enhanced with step management methods
2. `EmbeddingService`: Provides embedding-driven insights for steps

### Mock Data

The mock data service includes sample step data for demonstration:

```typescript
const MOCK_STATUSES: Status[] = [
  {
    id: 'status-005',
    title: 'Investment Portfolio Rebalancing',
    // ... other status fields
    steps: [
      {
        id: 'step-001',
        name: 'Portfolio Analysis',
        description: 'Analyze current portfolio allocation and performance',
        status: 'COMPLETED',
        startedAt: Date.now() - 48 * 60 * 60 * 1000,
        completedAt: Date.now() - 36 * 60 * 60 * 1000,
        metadata: {
          currentAllocation: '70% stocks, 20% bonds, 10% cash',
          performanceYTD: '+8.3%',
          riskAssessment: 'Slightly higher risk than target profile'
        }
      },
      // ... other steps
    ],
    currentStepId: 'step-003',
    totalSteps: 5,
    completedSteps: 2
  }
];
```

## Future Enhancements

Planned enhancements to the step tracking feature include:

1. **Step Dependencies**: Define dependencies between steps
2. **Automated Step Transitions**: Automatically transition between steps based on conditions
3. **Step Templates**: Reusable step templates for common workflows
4. **Step Notifications**: Notifications for step assignments and due dates
5. **Step Analytics**: Analytics on step completion times and bottlenecks

## Troubleshooting

Common issues and solutions:

1. **Steps Not Displaying**: Ensure status has steps array, currentStepId, totalSteps, and completedSteps
2. **Step Indicator Not Updating**: Verify step status changes are properly saved
3. **Inconsistent Step Count**: Check that totalSteps matches the length of the steps array
4. **Missing Step Metadata**: Ensure metadata is properly structured as a key-value object

## References

- [Status Tracker Documentation](STATUS_TRACKER.md)
- [EmbeddingFunction Integration](INTEGRATION.md)
- [UI Components](ui/src/app/components/README.md) 