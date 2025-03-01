import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { StatusDetailsComponent } from './status-details.component';
import { StatusTrackerService } from '../../services/status-tracker.service';
import { Status, StatusHistoryItem } from '../../models/status.model';

describe('StatusDetailsComponent', () => {
  let component: StatusDetailsComponent;
  let fixture: ComponentFixture<StatusDetailsComponent>;
  let statusServiceSpy: jasmine.SpyObj<StatusTrackerService>;

  const mockStatus: Status = {
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
    sourceSystemUrl: 'https://example.com/source/SRC123',
    statusHistory: [
      {
        timestamp: '2023-01-01T10:00:00Z',
        changedBy: 'system',
        newStage: 'Initiated',
        changeDescription: 'Status created'
      },
      {
        timestamp: '2023-01-02T14:30:00Z',
        changedBy: 'advisor456',
        previousStage: 'Initiated',
        newStage: 'In Progress',
        changeReason: 'Application started',
        changeDescription: 'Client has started the application process'
      }
    ]
  };

  beforeEach(async () => {
    const spy = jasmine.createSpyObj<StatusTrackerService>('StatusTrackerService', ['getStatus']);
    
    await TestBed.configureTestingModule({
      declarations: [StatusDetailsComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: StatusTrackerService, useValue: spy },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: 'status1' })
          }
        }
      ]
    });

    await TestBed.compileComponents();
    statusServiceSpy = TestBed.inject(StatusTrackerService) as jasmine.SpyObj<StatusTrackerService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusDetailsComponent);
    component = fixture.componentInstance;
    statusServiceSpy['getStatus'].and.returnValue(of(mockStatus));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load status details on init', () => {
    expect(statusServiceSpy['getStatus']).toHaveBeenCalledWith('status1');
    expect(component.status).toEqual(mockStatus);
    expect(component.loading).toBeFalse();
  });

  it('should sort status history by timestamp in descending order', () => {
    const sortedHistory: StatusHistoryItem[] = component.getStatusHistorySorted();
    expect(sortedHistory.length).toBe(2);
    expect(sortedHistory[0].timestamp).toBe('2023-01-02T14:30:00Z');
    expect(sortedHistory[1].timestamp).toBe('2023-01-01T10:00:00Z');
  });

  it('should handle empty status history', () => {
    component.status = { ...mockStatus, statusHistory: undefined };
    const sortedHistory: StatusHistoryItem[] = component.getStatusHistorySorted();
    expect(sortedHistory.length).toBe(0);
  });

  it('should open source system URL in new window', () => {
    // Spy on window.open
    spyOn(window, 'open');
    
    // Call the method
    component.openSourceSystem();
    
    // Verify window.open was called with the correct URL
    expect(window.open).toHaveBeenCalledWith('https://example.com/source/SRC123', '_blank');
  });

  it('should not open window if sourceSystemUrl is not defined', () => {
    // Spy on window.open
    spyOn(window, 'open');
    
    // Set sourceSystemUrl to undefined
    component.status = { ...mockStatus, sourceSystemUrl: undefined };
    
    // Call the method
    component.openSourceSystem();
    
    // Verify window.open was not called
    expect(window.open).not.toHaveBeenCalled();
  });
}); 