export interface StatusHistoryItem {
  timestamp: string;
  changedBy: string;
  previousStage?: string;
  newStage: string;
  changeReason?: string;
  changeDescription?: string;
}

export interface Document {
  name: string;
  type: string;
  url: string;
}

export interface Step {
  stepId: string;
  name: string;
  description?: string;
  status: 'Not Started' | 'In Progress' | 'Completed' | 'Blocked' | 'Skipped';
  order: number;
  startDate?: string;
  completionDate?: string;
  dueDate?: string;
  assignedTo?: string;
  notes?: string;
  metadata?: Record<string, any>;
}

export interface SubTransaction {
  subTransactionId: string;
  name: string;
  description?: string;
  status: string;
  startDate: string;
  dueDate?: string;
  completionDate?: string;
  assignedTo?: string;
  priority?: string;
  progress?: number;
  dependencies?: string[];
  notes?: string;
  documents?: Document[];
  metadata?: Record<string, any>;
}

export interface Status {
  statusId: string;
  clientId: string;
  advisorId: string;
  statusType: string;
  currentStage: string;
  statusSummary: string;
  createdDate: string;
  lastUpdatedDate: string;
  createdBy: string;
  lastUpdatedBy: string;
  sourceId?: string;
  trackingId: string;
  sourceSystemUrl?: string;
  
  statusDetails?: string;
  statusHistory?: StatusHistoryItem[];
  relatedDocuments?: Document[];
  requiredActions?: string[];
  completedActions?: string[];
  estimatedCompletionDate?: string;
  actualCompletionDate?: string;
  priority?: string;
  category?: string;
  subCategory?: string;
  
  householdId?: string;
  relatedClientIds?: string[];
  beneficiaryIds?: string[];
  relationshipTypes?: Record<string, string>;
  
  subTransactions?: SubTransaction[];
  
  // Step tracking properties
  steps?: Step[];
  currentStepId?: string;
  totalSteps?: number;
  completedSteps?: number;
  
  metadata?: Record<string, any>;
  tags?: Record<string, string>;
} 