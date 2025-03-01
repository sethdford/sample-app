import { Component, OnInit } from '@angular/core';
import { StatusTrackerService } from '../../services/status-tracker.service';
import { Status } from '../../models/status.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  recentStatuses: Status[] = [];
  loading = true;
  error = false;
  
  // Filters
  statusTypes = ['All', 'In Progress', 'Completed', 'Pending', 'Rejected'];
  selectedStatusType = 'All';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  
  constructor(private statusService: StatusTrackerService) { }

  ngOnInit(): void {
    this.loadStatuses();
  }
  
  loadStatuses(): void {
    this.loading = true;
    this.error = false;
    
    // For demo purposes, we're using a fixed client ID
    const clientId = 'client-123';
    const statusType = this.selectedStatusType !== 'All' ? this.selectedStatusType : undefined;
    
    this.statusService.getClientStatuses(clientId, statusType)
      .subscribe({
        next: (statuses: Status[]) => {
          this.recentStatuses = statuses;
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading statuses', err);
          this.error = true;
          this.loading = false;
        }
      });
  }
  
  filterByType(type: string): void {
    this.selectedStatusType = type;
    this.loadStatuses();
  }
  
  get paginatedStatuses(): Status[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.recentStatuses.slice(startIndex, startIndex + this.itemsPerPage);
  }
  
  get totalPages(): number {
    return Math.ceil(this.recentStatuses.length / this.itemsPerPage);
  }
  
  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }
} 