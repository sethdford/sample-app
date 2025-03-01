import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Status, Step } from '../models/status.model';
import { MockDataService } from './mock-data.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StatusTrackerService {
  private apiUrl = environment.apiUrl;
  private useMockData = true; // Set to false to use real API

  constructor(
    private http: HttpClient,
    private mockDataService: MockDataService
  ) { }

  // Create a new status
  createStatus(statusData: any): Observable<Status> {
    if (this.useMockData) {
      // Return the first mock status as a placeholder
      return of(this.mockDataService.getMockStatuses()[0]);
    }
    return this.http.post<Status>(`${this.apiUrl}/status`, statusData);
  }

  // Get a status by ID
  getStatus(statusId: string): Observable<Status> {
    if (this.useMockData) {
      const status = this.mockDataService.getMockStatus(statusId);
      if (status) {
        return of(status);
      }
      // Return the first mock status if not found
      return of(this.mockDataService.getMockStatuses()[0]);
    }
    return this.http.get<Status>(`${this.apiUrl}/status/${statusId}`);
  }

  // Get a status by source ID
  getStatusBySourceId(sourceId: string): Observable<Status> {
    if (this.useMockData) {
      const statuses = this.mockDataService.getMockStatuses();
      const status = statuses.find(s => s.sourceId === sourceId);
      if (status) {
        return of(status);
      }
      // Return the first mock status if not found
      return of(statuses[0]);
    }
    const params = new HttpParams().set('sourceId', sourceId);
    return this.http.get<Status>(`${this.apiUrl}/status/source`, { params });
  }

  // Get a status by tracking ID
  getStatusByTrackingId(trackingId: string): Observable<Status> {
    if (this.useMockData) {
      const statuses = this.mockDataService.getMockStatuses();
      const status = statuses.find(s => s.trackingId === trackingId);
      if (status) {
        return of(status);
      }
      // Return the first mock status if not found
      return of(statuses[0]);
    }
    const params = new HttpParams().set('trackingId', trackingId);
    return this.http.get<Status>(`${this.apiUrl}/status/tracking`, { params });
  }

  // Update a status
  updateStatus(statusId: string, statusData: any): Observable<Status> {
    if (this.useMockData) {
      const status = this.mockDataService.getMockStatus(statusId);
      if (status) {
        return of({ ...status, ...statusData });
      }
      // Return the first mock status if not found
      return of(this.mockDataService.getMockStatuses()[0]);
    }
    return this.http.put<Status>(`${this.apiUrl}/status/${statusId}`, statusData);
  }

  // Get all statuses for a client
  getClientStatuses(clientId: string, statusType?: string, fromDate?: string, toDate?: string): Observable<Status[]> {
    if (this.useMockData) {
      const statuses = this.mockDataService.getMockStatuses();
      let filteredStatuses = statuses.filter(s => s.clientId === clientId);
      
      if (statusType) {
        filteredStatuses = filteredStatuses.filter(s => s.currentStage === statusType);
      }
      
      // Simple date filtering (not implementing for mock data)
      return of(filteredStatuses);
    }
    
    let params = new HttpParams();
    if (statusType) params = params.set('statusType', statusType);
    if (fromDate) params = params.set('fromDate', fromDate);
    if (toDate) params = params.set('toDate', toDate);
    
    return this.http.get<Status[]>(`${this.apiUrl}/client/${clientId}/statuses`, { params });
  }

  // Get all client statuses for an advisor
  getAdvisorClientStatuses(advisorId: string, statusType?: string, fromDate?: string, toDate?: string): Observable<Record<string, Status[]>> {
    if (this.useMockData) {
      const statuses = this.mockDataService.getMockStatuses();
      let filteredStatuses = statuses.filter(s => s.advisorId === advisorId);
      
      if (statusType) {
        filteredStatuses = filteredStatuses.filter(s => s.currentStage === statusType);
      }
      
      // Group by clientId
      const result: Record<string, Status[]> = {};
      filteredStatuses.forEach(status => {
        if (!result[status.clientId]) {
          result[status.clientId] = [];
        }
        result[status.clientId].push(status);
      });
      
      return of(result);
    }
    
    let params = new HttpParams();
    if (statusType) params = params.set('statusType', statusType);
    if (fromDate) params = params.set('fromDate', fromDate);
    if (toDate) params = params.set('toDate', toDate);
    
    return this.http.get<Record<string, Status[]>>(`${this.apiUrl}/advisor/${advisorId}/client-statuses`, { params });
  }

  // Search for statuses
  searchStatuses(searchCriteria: any): Observable<Status[]> {
    if (this.useMockData) {
      return of(this.mockDataService.searchMockStatuses(searchCriteria));
    }
    return this.http.post<Status[]>(`${this.apiUrl}/statuses/search`, searchCriteria);
  }

  // Step Management Methods
  
  // Add steps to a status
  addStepsToStatus(statusId: string, steps: Step[]): Observable<Status> {
    if (this.useMockData) {
      return this.mockDataService.addStepsToStatus(statusId, steps);
    }
    return this.http.post<Status>(`${this.apiUrl}/status/${statusId}/steps`, { steps });
  }
  
  // Update a step in a status
  updateStep(statusId: string, stepId: string, stepData: Partial<Step>): Observable<Status> {
    if (this.useMockData) {
      return this.mockDataService.updateStep(statusId, stepId, stepData);
    }
    return this.http.put<Status>(`${this.apiUrl}/status/${statusId}/steps/${stepId}`, stepData);
  }
  
  // Set current step
  setCurrentStep(statusId: string, stepId: string): Observable<Status> {
    if (this.useMockData) {
      return this.mockDataService.setCurrentStep(statusId, stepId);
    }
    return this.http.put<Status>(`${this.apiUrl}/status/${statusId}/current-step`, { stepId });
  }
  
  // Complete a step
  completeStep(statusId: string, stepId: string, notes?: string): Observable<Status> {
    if (this.useMockData) {
      return this.mockDataService.completeStep(statusId, stepId, notes);
    }
    return this.http.put<Status>(`${this.apiUrl}/status/${statusId}/steps/${stepId}/complete`, { notes });
  }
  
  // Skip a step
  skipStep(statusId: string, stepId: string, reason: string): Observable<Status> {
    if (this.useMockData) {
      return this.mockDataService.skipStep(statusId, stepId, reason);
    }
    return this.http.put<Status>(`${this.apiUrl}/status/${statusId}/steps/${stepId}/skip`, { reason });
  }
  
  // Block a step
  blockStep(statusId: string, stepId: string, reason: string): Observable<Status> {
    if (this.useMockData) {
      return this.mockDataService.blockStep(statusId, stepId, reason);
    }
    return this.http.put<Status>(`${this.apiUrl}/status/${statusId}/steps/${stepId}/block`, { reason });
  }
  
  // Reorder steps
  reorderSteps(statusId: string, stepOrder: {stepId: string, order: number}[]): Observable<Status> {
    if (this.useMockData) {
      return this.mockDataService.reorderSteps(statusId, stepOrder);
    }
    return this.http.put<Status>(`${this.apiUrl}/status/${statusId}/steps/reorder`, { stepOrder });
  }
} 