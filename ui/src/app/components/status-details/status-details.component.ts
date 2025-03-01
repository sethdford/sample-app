import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { StatusTrackerService } from '../../services/status-tracker.service';
import { Status, StatusHistoryItem } from '../../models/status.model';

@Component({
  selector: 'app-status-details',
  templateUrl: './status-details.component.html',
  styleUrls: ['./status-details.component.scss']
})
export class StatusDetailsComponent implements OnInit {
  statusId: string = '';
  status: Status | null = null;
  loading = true;
  error = false;
  
  constructor(
    private route: ActivatedRoute,
    private statusService: StatusTrackerService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe((params: any) => {
      this.statusId = params['id'];
      this.loadStatus();
    });
  }
  
  loadStatus(): void {
    this.loading = true;
    this.error = false;
    
    this.statusService.getStatus(this.statusId)
      .subscribe({
        next: (status: Status) => {
          this.status = status;
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading status', err);
          this.error = true;
          this.loading = false;
        }
      });
  }
  
  getStatusHistorySorted(): StatusHistoryItem[] {
    if (!this.status || !this.status.statusHistory) {
      return [];
    }
    
    return [...this.status.statusHistory].sort((a, b) => {
      return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime();
    });
  }
  
  openSourceSystem(): void {
    if (this.status?.sourceSystemUrl) {
      window.open(this.status.sourceSystemUrl, '_blank');
    }
  }

  // Helper method to get object keys for use in the template
  getObjectKeys(obj: any): string[] {
    return Object.keys(obj || {});
  }
  
  // Complete a step
  completeStep(stepId: string): void {
    if (!this.status) return;
    
    this.statusService.completeStep(this.status.statusId, stepId)
      .subscribe({
        next: (updatedStatus: Status) => {
          this.status = updatedStatus;
        },
        error: (err: any) => {
          console.error('Error completing step', err);
        }
      });
  }
  
  // Skip a step
  skipStep(stepId: string, reason: string): void {
    if (!this.status) return;
    
    this.statusService.skipStep(this.status.statusId, stepId, reason)
      .subscribe({
        next: (updatedStatus: Status) => {
          this.status = updatedStatus;
        },
        error: (err: any) => {
          console.error('Error skipping step', err);
        }
      });
  }
  
  // Block a step
  blockStep(stepId: string, reason: string): void {
    if (!this.status) return;
    
    this.statusService.blockStep(this.status.statusId, stepId, reason)
      .subscribe({
        next: (updatedStatus: Status) => {
          this.status = updatedStatus;
        },
        error: (err: any) => {
          console.error('Error blocking step', err);
        }
      });
  }
} 