import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { DashboardComponent } from './dashboard.component';
import { StatusTrackerService } from '../../services/status-tracker.service';
import { Status } from '../../models/status.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let statusServiceSpy: jasmine.SpyObj<StatusTrackerService>;

  const mockStatuses: Status[] = [
    {
      statusId: 'status1',
      clientId: 'client123',
      advisorId: 'advisor456',
      statusType: 'Application',
      currentStage: 'In Progress',
      statusSummary: 'Loan Application',
      createdDate: '2023-01-01T10:00:00Z',
      lastUpdatedDate: '2023-01-02T14:30:00Z',
      createdBy: 'system',
      lastUpdatedBy: 'advisor456',
      sourceId: 'SRC123',
      trackingId: 'TRK123',
      sourceSystemUrl: 'https://example.com/source/SRC123'
    },
    {
      statusId: 'status2',
      clientId: 'client123',
      advisorId: 'advisor456',
      statusType: 'Review',
      currentStage: 'Completed',
      statusSummary: 'Annual Review',
      createdDate: '2023-02-01T10:00:00Z',
      lastUpdatedDate: '2023-02-02T14:30:00Z',
      createdBy: 'system',
      lastUpdatedBy: 'advisor456',
      sourceId: 'SRC456',
      trackingId: 'TRK456',
      sourceSystemUrl: 'https://example.com/source/SRC456'
    }
  ];

  beforeEach(async () => {
    const spy = jasmine.createSpyObj<StatusTrackerService>('StatusTrackerService', ['getClientStatuses']);

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: StatusTrackerService, useValue: spy }
      ]
    });

    await TestBed.compileComponents();
    statusServiceSpy = TestBed.inject(StatusTrackerService) as jasmine.SpyObj<StatusTrackerService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    statusServiceSpy.getClientStatuses.and.returnValue(of(mockStatuses));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load statuses on init', () => {
    expect(statusServiceSpy.getClientStatuses).toHaveBeenCalled();
    expect(component.recentStatuses.length).toBe(2);
    expect(component.loading).toBeFalse();
  });

  it('should filter statuses by type', () => {
    // Setup spy to return filtered results
    statusServiceSpy.getClientStatuses.and.returnValue(of([mockStatuses[0]]));
    
    // Call filter method
    component.filterByType('In Progress');
    
    // Verify filter was applied
    expect(component.selectedStatusType).toBe('In Progress');
    expect(statusServiceSpy.getClientStatuses).toHaveBeenCalledWith('client123', 'In Progress');
    expect(component.recentStatuses.length).toBe(1);
  });

  it('should paginate results correctly', () => {
    // Create more mock data to test pagination
    const manyStatuses: Status[] = Array(15).fill(null).map((_, i) => ({
      ...mockStatuses[0],
      statusId: `status${i + 1}`
    }));
    
    // Update component with many statuses
    component.recentStatuses = manyStatuses;
    component.itemsPerPage = 10;
    component.currentPage = 1;
    
    // Check first page
    expect(component.paginatedStatuses.length).toBe(10);
    expect(component.totalPages).toBe(2);
    
    // Check second page
    component.changePage(2);
    expect(component.currentPage).toBe(2);
    expect(component.paginatedStatuses.length).toBe(5);
  });

  it('should handle empty results', () => {
    statusServiceSpy.getClientStatuses.and.returnValue(of([]));
    component.loadStatuses();
    expect(component.recentStatuses.length).toBe(0);
    expect(component.loading).toBeFalse();
  });
}); 