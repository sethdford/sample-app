# Status Tracker UI

An Apple-inspired Angular Micro Frontend (MFE) for the Status Tracker API.

## Overview

This UI provides a modern, clean interface for interacting with the Status Tracker API. It allows users to:

- View a dashboard of recent statuses
- Filter statuses by type
- View detailed information about individual statuses
- See status history in a timeline view
- Access source systems directly via the sourceSystemUrl

## Design System

The UI implements an Apple-inspired design system with:

- Clean, minimalist aesthetics
- Ample white space
- Subtle shadows and rounded corners
- Consistent typography and color palette
- Responsive design for all screen sizes

## Project Structure

```
ui/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── dashboard/
│   │   │   ├── status-details/
│   │   │   └── search/
│   │   ├── models/
│   │   ├── services/
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.component.scss
│   │   ├── app.module.ts
│   │   └── app-routing.module.ts
│   ├── assets/
│   │   ├── styles/
│   │   │   └── apple-design-system.scss
│   │   └── images/
│   ├── styles.scss
│   ├── index.html
│   └── main.ts
├── package.json
├── tsconfig.json
├── tsconfig.spec.json
├── karma.conf.js
└── README.md
```

## Getting Started

### Prerequisites

- Node.js (v14+)
- npm (v6+)

### Installation

1. Navigate to the UI directory:
   ```
   cd StatusTrackerFunction/ui
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Start the development server:
   ```
   npm start
   ```

4. Open your browser to `http://localhost:4200`

## Building for Production

To build the application for production:

```
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## Running Tests

The application includes comprehensive unit tests to ensure functionality and prevent regressions.

### Running Tests Locally

To run tests in watch mode during development:

```
npm test
```

This will open a Chrome browser and run the tests, watching for changes.

### Running Tests in CI

To run tests in a CI environment (headless mode):

```
npm run test:ci
```

### Test Coverage

The tests cover:

- Components: Dashboard, Status Details, Search
- Services: StatusTrackerService
- Models: Status model validation
- Routing: Navigation between components

### Test Structure

Each test file follows a consistent structure:

1. **Setup**: Configuring the testing module with necessary dependencies
2. **Mock Data**: Providing realistic test data
3. **Test Cases**: Individual tests for specific functionality
4. **Cleanup**: Verifying and resetting after tests

All tests use proper TypeScript typing to ensure type safety and improve code quality.

## TypeScript Fixes

The following TypeScript fixes were applied to ensure proper type checking:

1. **Explicit Parameter Types**: Added explicit type annotations to all parameters in component methods
2. **Route Definitions**: Changed `Routes` type to `any[]` to resolve compatibility issues
3. **HTTP Client**: Updated HTTP client imports to ensure proper module loading
4. **Test Configuration**: Fixed test setup to properly initialize the Angular testing environment

These changes ensure that the TypeScript compiler can properly validate the code and catch potential errors during development.

## Integration with Status Tracker API

This UI is designed to work with the Status Tracker API. It consumes the following endpoints:

- `GET /statuses/{statusId}` - Get a status by ID
- `GET /statuses/source/{sourceId}` - Get a status by source ID
- `GET /statuses/tracking/{trackingId}` - Get a status by tracking ID
- `GET /statuses/client/{clientId}` - Get all statuses for a client
- `GET /statuses/advisor/{advisorId}/clients` - Get all client statuses for an advisor
- `POST /statuses/search` - Search for statuses based on criteria

## Features

### Dashboard

The dashboard provides a quick overview of recent statuses with:
- Status summary and current stage
- Client and advisor information
- Source system links
- Filtering by status type
- Pagination for large result sets

### Status Details

The status details view shows comprehensive information about a status:
- Basic information (IDs, dates)
- Status details and metadata
- Related documents with links
- Complete status history in a timeline view
- Direct link to source system 
- Step tracking for multi-step workflows

### Step Tracking

The application includes a robust step tracking feature for multi-step workflows:

- Visual progress indicator showing completed and pending steps
- Detailed step information including:
  - Step name and description
  - Current status (Completed, In Progress, Not Started, Blocked, Skipped)
  - Start and completion dates
  - Assigned personnel
  - Step notes and additional details
- Both horizontal and vertical layout options
- Compact view for dashboard integration
- Full detailed view in the status details page

Step tracking is implemented using a reusable component that can be integrated into any part of the application. The component supports:

- Dynamic step rendering based on status data
- Progress calculation and visualization
- Status-based styling (color coding for different step statuses)
- Responsive design for all screen sizes 