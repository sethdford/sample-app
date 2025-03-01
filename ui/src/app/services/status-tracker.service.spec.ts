import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpRequest } from '@angular/common/http';

import { StatusTrackerService } from './status-tracker.service';
import { Status } from '../models/status.model';

describe('StatusTrackerService', () => {
  let service: StatusTrackerService;
  let httpMock: HttpTestingController;

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
    sourceSystemUrl: 'https://example.com/source/SRC123'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [StatusTrackerService]
    });
    
    service = TestBed.inject(StatusTrackerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get a status by ID', () => {
    const statusId = 'status1';
    
    service.getStatus(statusId).subscribe((status: Status) => {
      expect(status).toEqual(mockStatus);
    });
    
    const req = httpMock.expectOne(`${service['apiUrl']}/status/${statusId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStatus);
  });

  it('should get a status by source ID', () => {
    const sourceId = 'SRC123';
    
    service.getStatusBySourceId(sourceId).subscribe((status: Status) => {
      expect(status).toEqual(mockStatus);
    });
    
    const req = httpMock.expectOne(`${service['apiUrl']}/status/source?sourceId=${sourceId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStatus);
  });

  it('should get a status by tracking ID', () => {
    const trackingId = 'TRK123';
    
    service.getStatusByTrackingId(trackingId).subscribe((status: Status) => {
      expect(status).toEqual(mockStatus);
    });
    
    const req = httpMock.expectOne(`${service['apiUrl']}/status/tracking?trackingId=${trackingId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStatus);
  });

  it('should create a status', () => {
    const statusData = {
      clientId: 'client123',
      advisorId: 'advisor456',
      statusType: 'Application',
      statusSummary: 'New Application'
    };
    
    service.createStatus(statusData).subscribe((status: Status) => {
      expect(status).toEqual(mockStatus);
    });
    
    const req = httpMock.expectOne(`${service['apiUrl']}/status`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(statusData);
    req.flush(mockStatus);
  });

  it('should update a status', () => {
    const statusId = 'status1';
    const statusData = {
      currentStage: 'Completed'
    };
    
    service.updateStatus(statusId, statusData).subscribe((status: Status) => {
      expect(status).toEqual(mockStatus);
    });
    
    const req = httpMock.expectOne(`${service['apiUrl']}/status/${statusId}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(statusData);
    req.flush(mockStatus);
  });

  it('should get client statuses', () => {
    const clientId = 'client123';
    const statusType = 'Application';
    const fromDate = '2023-01-01';
    const toDate = '2023-01-31';
    
    service.getClientStatuses(clientId, statusType, fromDate, toDate).subscribe((statuses: Status[]) => {
      expect(statuses).toEqual([mockStatus]);
    });
    
    const req = httpMock.expectOne(
      `${service['apiUrl']}/client/${clientId}/statuses?statusType=${statusType}&fromDate=${fromDate}&toDate=${toDate}`
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockStatus]);
  });

  it('should get advisor client statuses', () => {
    const advisorId = 'advisor456';
    
    service.getAdvisorClientStatuses(advisorId).subscribe((clientStatuses: Record<string, Status[]>) => {
      expect(clientStatuses).toEqual({ 'client123': [mockStatus] });
    });
    
    const req = httpMock.expectOne(`${service['apiUrl']}/advisor/${advisorId}/client-statuses`);
    expect(req.request.method).toBe('GET');
    req.flush({ 'client123': [mockStatus] });
  });

  it('should search statuses', () => {
    const searchCriteria = {
      clientId: 'client123',
      currentStage: 'In Progress'
    };
    
    service.searchStatuses(searchCriteria).subscribe((statuses: Status[]) => {
      expect(statuses).toEqual([mockStatus]);
    });
    
    const req = httpMock.expectOne(`${service['apiUrl']}/statuses/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(searchCriteria);
    req.flush([mockStatus]);
  });
}); 