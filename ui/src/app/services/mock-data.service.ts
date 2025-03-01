import { Injectable } from '@angular/core';
import { Status, StatusHistoryItem, SubTransaction, Step } from '../models/status.model';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MockDataService {
  private mockStatuses: Status[] = [
    {
      statusId: 'status-001',
      clientId: 'client-123',
      advisorId: 'advisor-456',
      statusType: 'Application',
      currentStage: 'In Progress',
      statusSummary: 'Mortgage Application',
      createdDate: '2023-05-15T10:30:00Z',
      lastUpdatedDate: '2023-05-18T14:45:00Z',
      createdBy: 'system',
      lastUpdatedBy: 'advisor-456',
      sourceId: 'MORT-789',
      trackingId: 'TRK-001',
      sourceSystemUrl: 'https://mortgage-system.example.com/applications/MORT-789',
      statusDetails: 'Client has submitted initial application documents. Pending income verification.',
      statusHistory: [
        {
          timestamp: '2023-05-15T10:30:00Z',
          changedBy: 'system',
          newStage: 'Initiated',
          changeDescription: 'Application created in system'
        },
        {
          timestamp: '2023-05-16T09:15:00Z',
          changedBy: 'client-123',
          previousStage: 'Initiated',
          newStage: 'Document Upload',
          changeDescription: 'Client uploaded required documents'
        },
        {
          timestamp: '2023-05-18T14:45:00Z',
          changedBy: 'advisor-456',
          previousStage: 'Document Upload',
          newStage: 'In Progress',
          changeReason: 'Document review',
          changeDescription: 'Initial document review completed, proceeding with income verification'
        }
      ],
      relatedDocuments: [
        { name: 'Income Statement', type: 'PDF', url: 'https://example.com/docs/income-statement.pdf' },
        { name: 'Property Valuation', type: 'PDF', url: 'https://example.com/docs/property-valuation.pdf' }
      ],
      requiredActions: [
        'Verify employment details',
        'Complete credit check'
      ],
      completedActions: [
        'Submit application',
        'Upload identification documents'
      ],
      estimatedCompletionDate: '2023-06-15T00:00:00Z',
      priority: 'High',
      category: 'Mortgage',
      subCategory: 'Residential',
      metadata: {
        propertyAddress: '123 Main St, Anytown, USA',
        loanAmount: '$350,000',
        loanType: '30-year fixed'
      },
      subTransactions: [
        {
          subTransactionId: 'sub-001-1',
          name: 'Income Verification',
          description: 'Verify client income sources and employment history',
          status: 'In Progress',
          startDate: '2023-05-18T15:00:00Z',
          dueDate: '2023-05-22T17:00:00Z',
          assignedTo: 'processor-789',
          priority: 'High',
          progress: 60,
          notes: 'Employer verification call scheduled for tomorrow. W2 forms received and validated.',
          documents: [
            { name: 'W2 Forms', type: 'PDF', url: 'https://example.com/docs/w2-forms.pdf' },
            { name: 'Pay Stubs', type: 'PDF', url: 'https://example.com/docs/pay-stubs.pdf' }
          ],
          metadata: {
            incomeSource1: 'Primary Employment',
            incomeAmount1: '$95,000',
            incomeSource2: 'Rental Property',
            incomeAmount2: '$24,000'
          }
        },
        {
          subTransactionId: 'sub-001-2',
          name: 'Credit Check',
          description: 'Perform comprehensive credit analysis',
          status: 'Completed',
          startDate: '2023-05-16T10:00:00Z',
          completionDate: '2023-05-17T14:30:00Z',
          assignedTo: 'underwriter-456',
          priority: 'High',
          progress: 100,
          notes: 'Credit check completed. Score meets requirements for preferred rate.',
          documents: [
            { name: 'Credit Report', type: 'PDF', url: 'https://example.com/docs/credit-report.pdf' }
          ],
          metadata: {
            creditScore: '742',
            creditBureau: 'Experian',
            debtToIncomeRatio: '32%'
          }
        },
        {
          subTransactionId: 'sub-001-3',
          name: 'Property Appraisal',
          description: 'Schedule and complete property appraisal',
          status: 'Pending',
          startDate: '2023-05-17T09:00:00Z',
          dueDate: '2023-05-25T17:00:00Z',
          assignedTo: 'appraiser-123',
          priority: 'Medium',
          progress: 30,
          notes: 'Appraisal scheduled for 05/23/2023. Initial property details collected.',
          documents: [
            { name: 'Property Details', type: 'PDF', url: 'https://example.com/docs/property-details.pdf' }
          ],
          dependencies: ['sub-001-1'],
          metadata: {
            appraisalCompany: 'ValueRight Appraisals',
            appraisalDate: '2023-05-23',
            appraisalFee: '$450'
          }
        },
        {
          subTransactionId: 'sub-001-4',
          name: 'Title Search',
          description: 'Perform title search and verify property ownership',
          status: 'Not Started',
          startDate: '2023-05-19T09:00:00Z',
          dueDate: '2023-05-26T17:00:00Z',
          assignedTo: 'title-agent-567',
          priority: 'Medium',
          progress: 0,
          dependencies: ['sub-001-3'],
          metadata: {
            titleCompany: 'ClearTitle Services',
            countyRecords: 'Anytown County',
            titleInsuranceFee: '$850'
          }
        },
        {
          subTransactionId: 'sub-001-5',
          name: 'Underwriting Review',
          description: 'Final review of all documentation and approval decision',
          status: 'Not Started',
          startDate: '2023-05-26T09:00:00Z',
          dueDate: '2023-06-02T17:00:00Z',
          assignedTo: 'underwriter-789',
          priority: 'High',
          progress: 0,
          dependencies: ['sub-001-1', 'sub-001-2', 'sub-001-3', 'sub-001-4'],
          metadata: {
            reviewType: 'Comprehensive',
            approvalLevel: 'Senior Underwriter'
          }
        },
        {
          subTransactionId: 'sub-001-6',
          name: 'Closing Preparation',
          description: 'Prepare all closing documents and schedule closing',
          status: 'Not Started',
          startDate: '2023-06-05T09:00:00Z',
          dueDate: '2023-06-12T17:00:00Z',
          assignedTo: 'closing-agent-234',
          priority: 'Medium',
          progress: 0,
          dependencies: ['sub-001-5'],
          metadata: {
            closingLocation: 'Main Branch Office',
            estimatedClosingCosts: '$5,200'
          }
        }
      ],
      steps: [
        {
          stepId: 'step-001-1',
          name: 'Application Submission',
          description: 'Submit initial mortgage application',
          status: 'Completed',
          order: 1,
          startDate: '2023-05-15T10:30:00Z',
          completionDate: '2023-05-15T11:45:00Z',
          assignedTo: 'client-123',
          notes: 'Client submitted application through online portal'
        },
        {
          stepId: 'step-001-2',
          name: 'Document Collection',
          description: 'Gather required documentation',
          status: 'Completed',
          order: 2,
          startDate: '2023-05-15T12:00:00Z',
          completionDate: '2023-05-16T09:15:00Z',
          assignedTo: 'client-123',
          notes: 'All required documents uploaded'
        },
        {
          stepId: 'step-001-3',
          name: 'Initial Review',
          description: 'Review application and documents',
          status: 'Completed',
          order: 3,
          startDate: '2023-05-16T10:00:00Z',
          completionDate: '2023-05-18T14:45:00Z',
          assignedTo: 'advisor-456',
          notes: 'Application and documents reviewed, proceeding to verification'
        },
        {
          stepId: 'step-001-4',
          name: 'Verification Process',
          description: 'Verify income, credit, and property details',
          status: 'In Progress',
          order: 4,
          startDate: '2023-05-18T15:00:00Z',
          dueDate: '2023-05-26T17:00:00Z',
          assignedTo: 'processor-789',
          notes: 'Income verification in progress, credit check completed, property appraisal scheduled'
        },
        {
          stepId: 'step-001-5',
          name: 'Underwriting',
          description: 'Underwriting review and decision',
          status: 'Not Started',
          order: 5,
          dueDate: '2023-06-02T17:00:00Z',
          assignedTo: 'underwriter-789'
        },
        {
          stepId: 'step-001-6',
          name: 'Closing',
          description: 'Prepare closing documents and complete closing',
          status: 'Not Started',
          order: 6,
          dueDate: '2023-06-12T17:00:00Z',
          assignedTo: 'closing-agent-234'
        },
        {
          stepId: 'step-001-7',
          name: 'Funding',
          description: 'Disburse loan funds',
          status: 'Not Started',
          order: 7,
          dueDate: '2023-06-14T17:00:00Z',
          assignedTo: 'funding-agent-567'
        }
      ],
      currentStepId: 'step-001-4',
      totalSteps: 7,
      completedSteps: 3
    },
    {
      statusId: 'status-002',
      clientId: 'client-123',
      advisorId: 'advisor-456',
      statusType: 'Review',
      currentStage: 'Completed',
      statusSummary: 'Annual Financial Review',
      createdDate: '2023-04-10T13:20:00Z',
      lastUpdatedDate: '2023-04-12T16:30:00Z',
      createdBy: 'advisor-456',
      lastUpdatedBy: 'advisor-456',
      sourceId: 'REV-456',
      trackingId: 'TRK-002',
      sourceSystemUrl: 'https://review-system.example.com/reviews/REV-456',
      statusDetails: 'Annual financial review completed with client. Portfolio adjustments recommended.',
      statusHistory: [
        {
          timestamp: '2023-04-10T13:20:00Z',
          changedBy: 'advisor-456',
          newStage: 'Scheduled',
          changeDescription: 'Annual review scheduled'
        },
        {
          timestamp: '2023-04-11T10:00:00Z',
          changedBy: 'advisor-456',
          previousStage: 'Scheduled',
          newStage: 'In Progress',
          changeDescription: 'Meeting with client in progress'
        },
        {
          timestamp: '2023-04-12T16:30:00Z',
          changedBy: 'advisor-456',
          previousStage: 'In Progress',
          newStage: 'Completed',
          changeReason: 'Review completion',
          changeDescription: 'Annual review completed, recommendations provided to client'
        }
      ],
      relatedDocuments: [
        { name: 'Portfolio Summary', type: 'PDF', url: 'https://example.com/docs/portfolio-summary.pdf' },
        { name: 'Investment Recommendations', type: 'DOCX', url: 'https://example.com/docs/investment-recommendations.docx' }
      ],
      requiredActions: [],
      completedActions: [
        'Review current investments',
        'Discuss financial goals',
        'Provide recommendations'
      ],
      estimatedCompletionDate: '2023-04-12T00:00:00Z',
      priority: 'Medium',
      category: 'Financial Planning',
      subCategory: 'Annual Review',
      metadata: {
        portfolioValue: '$750,000',
        recommendedChanges: 'Increase bond allocation by 5%'
      }
    },
    {
      statusId: 'status-003',
      clientId: 'client-123',
      advisorId: 'advisor-456',
      statusType: 'Application',
      currentStage: 'Pending',
      statusSummary: 'Credit Card Application',
      createdDate: '2023-05-20T09:15:00Z',
      lastUpdatedDate: '2023-05-21T11:30:00Z',
      createdBy: 'client-123',
      lastUpdatedBy: 'system',
      sourceId: 'CC-123',
      trackingId: 'TRK-003',
      sourceSystemUrl: 'https://creditcard-system.example.com/applications/CC-123',
      statusDetails: 'Credit card application submitted. Awaiting credit check results.',
      statusHistory: [
        {
          timestamp: '2023-05-20T09:15:00Z',
          changedBy: 'client-123',
          newStage: 'Initiated',
          changeDescription: 'Credit card application submitted'
        },
        {
          timestamp: '2023-05-21T11:30:00Z',
          changedBy: 'system',
          previousStage: 'Initiated',
          newStage: 'Pending',
          changeDescription: 'Application sent for credit check'
        }
      ],
      relatedDocuments: [
        { name: 'Credit Card Terms', type: 'PDF', url: 'https://example.com/docs/credit-card-terms.pdf' }
      ],
      requiredActions: [
        'Complete credit check',
        'Verify identity'
      ],
      completedActions: [
        'Submit application'
      ],
      estimatedCompletionDate: '2023-05-25T00:00:00Z',
      priority: 'Medium',
      category: 'Banking',
      subCategory: 'Credit Card',
      metadata: {
        cardType: 'Rewards Platinum',
        creditLimit: '$10,000',
        annualFee: '$95'
      }
    },
    {
      statusId: 'status-004',
      clientId: 'client-123',
      advisorId: 'advisor-789',
      statusType: 'Application',
      currentStage: 'Rejected',
      statusSummary: 'Personal Loan Application',
      createdDate: '2023-04-05T14:20:00Z',
      lastUpdatedDate: '2023-04-08T10:15:00Z',
      createdBy: 'client-123',
      lastUpdatedBy: 'advisor-789',
      sourceId: 'LOAN-456',
      trackingId: 'TRK-004',
      sourceSystemUrl: 'https://loan-system.example.com/applications/LOAN-456',
      statusDetails: 'Personal loan application rejected due to insufficient income documentation.',
      statusHistory: [
        {
          timestamp: '2023-04-05T14:20:00Z',
          changedBy: 'client-123',
          newStage: 'Initiated',
          changeDescription: 'Loan application submitted'
        },
        {
          timestamp: '2023-04-06T09:30:00Z',
          changedBy: 'system',
          previousStage: 'Initiated',
          newStage: 'Document Review',
          changeDescription: 'Application documents under review'
        },
        {
          timestamp: '2023-04-08T10:15:00Z',
          changedBy: 'advisor-789',
          previousStage: 'Document Review',
          newStage: 'Rejected',
          changeReason: 'Insufficient documentation',
          changeDescription: 'Application rejected due to missing income verification documents'
        }
      ],
      relatedDocuments: [
        { name: 'Loan Application', type: 'PDF', url: 'https://example.com/docs/loan-application.pdf' },
        { name: 'Rejection Letter', type: 'PDF', url: 'https://example.com/docs/rejection-letter.pdf' }
      ],
      requiredActions: [],
      completedActions: [
        'Submit application',
        'Review rejection reasons'
      ],
      estimatedCompletionDate: '2023-04-15T00:00:00Z',
      priority: 'Low',
      category: 'Banking',
      subCategory: 'Personal Loan',
      metadata: {
        loanAmount: '$25,000',
        loanPurpose: 'Debt Consolidation',
        rejectionReason: 'Insufficient income documentation'
      }
    },
    {
      statusId: 'status-005',
      clientId: 'client-123',
      advisorId: 'advisor-456',
      statusType: 'Request',
      currentStage: 'In Progress',
      statusSummary: 'Investment Portfolio Rebalancing',
      createdDate: '2023-05-25T08:45:00Z',
      lastUpdatedDate: '2023-05-26T13:20:00Z',
      createdBy: 'advisor-456',
      lastUpdatedBy: 'advisor-456',
      sourceId: 'REB-789',
      trackingId: 'TRK-005',
      sourceSystemUrl: 'https://investment-system.example.com/rebalancing/REB-789',
      statusDetails: 'Quarterly portfolio rebalancing in progress. Adjusting asset allocation to match target.',
      statusHistory: [
        {
          timestamp: '2023-05-25T08:45:00Z',
          changedBy: 'advisor-456',
          newStage: 'Initiated',
          changeDescription: 'Portfolio rebalancing initiated'
        },
        {
          timestamp: '2023-05-26T13:20:00Z',
          changedBy: 'advisor-456',
          previousStage: 'Initiated',
          newStage: 'In Progress',
          changeReason: 'Market analysis',
          changeDescription: 'Market analysis completed, proceeding with rebalancing'
        }
      ],
      relatedDocuments: [
        { name: 'Current Allocation', type: 'PDF', url: 'https://example.com/docs/current-allocation.pdf' },
        { name: 'Target Allocation', type: 'PDF', url: 'https://example.com/docs/target-allocation.pdf' }
      ],
      requiredActions: [
        'Execute trades',
        'Confirm transactions',
        'Generate rebalancing report'
      ],
      completedActions: [
        'Analyze current allocation',
        'Determine target allocation'
      ],
      estimatedCompletionDate: '2023-05-30T00:00:00Z',
      priority: 'High',
      category: 'Investment',
      subCategory: 'Portfolio Management',
      metadata: {
        portfolioValue: '$1,250,000',
        targetEquityAllocation: '65%',
        targetBondAllocation: '30%',
        targetCashAllocation: '5%'
      },
      subTransactions: [
        {
          subTransactionId: 'sub-005-1',
          name: 'Portfolio Analysis',
          description: 'Analyze current portfolio allocation and performance',
          status: 'Completed',
          startDate: '2023-05-25T08:45:00Z',
          completionDate: '2023-05-25T14:30:00Z',
          assignedTo: 'analyst-345',
          priority: 'High',
          progress: 100,
          notes: 'Current allocation is 70% equity, 25% bonds, 5% cash. Performance is 2% below benchmark.',
          documents: [
            { name: 'Current Allocation Report', type: 'PDF', url: 'https://example.com/docs/current-allocation-report.pdf' },
            { name: 'Performance Analysis', type: 'XLSX', url: 'https://example.com/docs/performance-analysis.xlsx' }
          ],
          metadata: {
            currentEquityAllocation: '70%',
            currentBondAllocation: '25%',
            currentCashAllocation: '5%',
            yearToDateReturn: '4.2%',
            benchmarkReturn: '6.3%'
          }
        },
        {
          subTransactionId: 'sub-005-2',
          name: 'Rebalancing Strategy',
          description: 'Develop strategy for rebalancing portfolio to target allocation',
          status: 'Completed',
          startDate: '2023-05-25T15:00:00Z',
          completionDate: '2023-05-26T10:15:00Z',
          assignedTo: 'advisor-456',
          priority: 'High',
          progress: 100,
          notes: 'Strategy approved to reduce equity exposure by 5% and increase bond allocation by 5%.',
          documents: [
            { name: 'Rebalancing Strategy', type: 'PDF', url: 'https://example.com/docs/rebalancing-strategy.pdf' }
          ],
          dependencies: ['sub-005-1'],
          metadata: {
            equityReduction: '5%',
            bondIncrease: '5%',
            estimatedTradingCosts: '$1,200',
            estimatedTaxImpact: 'Minimal - most trades in tax-advantaged accounts'
          }
        },
        {
          subTransactionId: 'sub-005-3',
          name: 'Trade Execution',
          description: 'Execute trades to implement rebalancing strategy',
          status: 'In Progress',
          startDate: '2023-05-26T13:20:00Z',
          dueDate: '2023-05-29T16:00:00Z',
          assignedTo: 'trader-789',
          priority: 'High',
          progress: 40,
          notes: 'Equity sales completed. Bond purchases in progress.',
          dependencies: ['sub-005-2'],
          metadata: {
            equitySalesCompleted: 'Yes',
            bondPurchasesCompleted: 'No',
            tradesExecuted: '4 of 7',
            averageExecutionQuality: 'Good'
          }
        },
        {
          subTransactionId: 'sub-005-4',
          name: 'Client Confirmation',
          description: 'Prepare and send confirmation of rebalancing to client',
          status: 'Not Started',
          startDate: '2023-05-29T16:00:00Z',
          dueDate: '2023-05-30T17:00:00Z',
          assignedTo: 'advisor-456',
          priority: 'Medium',
          progress: 0,
          dependencies: ['sub-005-3'],
          metadata: {
            communicationMethod: 'Email and Phone Call',
            scheduledFollowUp: '2023-06-15'
          }
        }
      ],
      steps: [
        {
          stepId: 'step-005-1',
          name: 'Analysis',
          description: 'Analyze current portfolio allocation and performance',
          status: 'Completed',
          order: 1,
          startDate: '2023-05-25T08:45:00Z',
          completionDate: '2023-05-25T14:30:00Z',
          assignedTo: 'analyst-345',
          notes: 'Portfolio analysis completed. Current allocation is 70% equity, 25% bonds, 5% cash.'
        },
        {
          stepId: 'step-005-2',
          name: 'Strategy Development',
          description: 'Develop rebalancing strategy',
          status: 'Completed',
          order: 2,
          startDate: '2023-05-25T15:00:00Z',
          completionDate: '2023-05-26T10:15:00Z',
          assignedTo: 'advisor-456',
          notes: 'Strategy approved to reduce equity exposure by 5% and increase bond allocation by 5%.'
        },
        {
          stepId: 'step-005-3',
          name: 'Trade Execution',
          description: 'Execute trades to implement rebalancing strategy',
          status: 'In Progress',
          order: 3,
          startDate: '2023-05-26T13:20:00Z',
          dueDate: '2023-05-29T16:00:00Z',
          assignedTo: 'trader-789',
          notes: 'Equity sales completed. Bond purchases in progress.'
        },
        {
          stepId: 'step-005-4',
          name: 'Client Confirmation',
          description: 'Prepare and send confirmation of rebalancing to client',
          status: 'Not Started',
          order: 4,
          dueDate: '2023-05-30T17:00:00Z',
          assignedTo: 'advisor-456'
        },
        {
          stepId: 'step-005-5',
          name: 'Performance Review',
          description: 'Review portfolio performance after rebalancing',
          status: 'Not Started',
          order: 5,
          dueDate: '2023-06-15T17:00:00Z',
          assignedTo: 'advisor-456'
        }
      ],
      currentStepId: 'step-005-3',
      totalSteps: 5,
      completedSteps: 2
    },
    {
      statusId: 'status-006',
      clientId: 'client-123',
      advisorId: 'advisor-456',
      statusType: 'Review',
      currentStage: 'Scheduled',
      statusSummary: 'Retirement Planning Session',
      createdDate: '2023-06-01T09:00:00Z',
      lastUpdatedDate: '2023-06-01T09:00:00Z',
      createdBy: 'advisor-456',
      lastUpdatedBy: 'advisor-456',
      sourceId: 'RET-123',
      trackingId: 'TRK-006',
      sourceSystemUrl: 'https://planning-system.example.com/retirement/RET-123',
      statusDetails: 'Comprehensive retirement planning session scheduled with client to review goals and strategies.',
      statusHistory: [
        {
          timestamp: '2023-06-01T09:00:00Z',
          changedBy: 'advisor-456',
          newStage: 'Scheduled',
          changeDescription: 'Retirement planning session scheduled for June 15, 2023'
        }
      ],
      relatedDocuments: [
        { name: 'Retirement Questionnaire', type: 'PDF', url: 'https://example.com/docs/retirement-questionnaire.pdf' }
      ],
      requiredActions: [
        'Complete retirement questionnaire',
        'Gather current retirement account statements',
        'Prepare retirement goals discussion'
      ],
      completedActions: [
        'Schedule planning session'
      ],
      estimatedCompletionDate: '2023-06-15T00:00:00Z',
      priority: 'Medium',
      category: 'Financial Planning',
      subCategory: 'Retirement',
      metadata: {
        clientAge: '45',
        targetRetirementAge: '65',
        currentRetirementSavings: '$550,000'
      }
    },
    {
      statusId: 'status-007',
      clientId: 'client-123',
      advisorId: 'advisor-789',
      statusType: 'Planning',
      currentStage: 'In Progress',
      statusSummary: 'Retirement Planning',
      createdDate: '2023-05-01T11:00:00Z',
      lastUpdatedDate: '2023-05-03T15:30:00Z',
      createdBy: 'advisor-789',
      lastUpdatedBy: 'advisor-789',
      sourceId: 'PLAN-123',
      trackingId: 'TRK-007',
      sourceSystemUrl: 'https://planning-system.example.com/plans/PLAN-123',
      statusDetails: 'Comprehensive retirement planning in progress. Analyzing current portfolio and future needs.',
      statusHistory: [
        {
          timestamp: '2023-05-01T11:00:00Z',
          changedBy: 'advisor-789',
          newStage: 'Initiated',
          changeDescription: 'Retirement planning initiated'
        },
        {
          timestamp: '2023-05-03T15:30:00Z',
          changedBy: 'advisor-789',
          previousStage: 'Initiated',
          newStage: 'In Progress',
          changeReason: 'Initial analysis',
          changeDescription: 'Initial retirement scenario analysis in progress'
        }
      ],
      relatedDocuments: [
        { name: 'Current Portfolio', type: 'PDF', url: 'https://example.com/docs/current-portfolio.pdf' },
        { name: 'Retirement Goals', type: 'DOCX', url: 'https://example.com/docs/retirement-goals.docx' }
      ],
      metadata: {
        clientAge: '52',
        targetRetirementAge: '65',
        currentSavings: '$450,000'
      }
    }
  ];

  constructor() { }

  getMockStatuses(): Status[] {
    return this.mockStatuses;
  }

  getMockStatus(statusId: string): Status | undefined {
    return this.mockStatuses.find(status => status.statusId === statusId);
  }

  searchMockStatuses(criteria: any): Status[] {
    return this.mockStatuses.filter(status => {
      // Simple search implementation
      for (const key in criteria) {
        if (criteria[key] && status[key as keyof Status]) {
          const statusValue = String(status[key as keyof Status]).toLowerCase();
          const criteriaValue = String(criteria[key]).toLowerCase();
          
          if (!statusValue.includes(criteriaValue)) {
            return false;
          }
        }
      }
      return true;
    });
  }

  // Step Management Methods
  
  addStepsToStatus(statusId: string, steps: Step[]): Observable<Status> {
    const statusIndex = this.mockStatuses.findIndex(s => s.statusId === statusId);
    if (statusIndex === -1) {
      return of(this.mockStatuses[0]);
    }
    
    const status = { ...this.mockStatuses[statusIndex] };
    
    if (!status.steps) {
      status.steps = [];
    }
    
    // Assign order numbers if not provided
    steps.forEach((step, index) => {
      if (!step.order) {
        step.order = (status.steps?.length || 0) + index + 1;
      }
    });
    
    status.steps = [...status.steps, ...steps];
    status.totalSteps = status.steps.length;
    
    // Count completed steps
    status.completedSteps = status.steps.filter(step => step.status === 'Completed').length;
    
    // Update the status in the mock data
    this.mockStatuses[statusIndex] = status;
    
    return of(status);
  }
  
  updateStep(statusId: string, stepId: string, stepData: Partial<Step>): Observable<Status> {
    const statusIndex = this.mockStatuses.findIndex(s => s.statusId === statusId);
    if (statusIndex === -1 || !this.mockStatuses[statusIndex].steps) {
      return of(this.mockStatuses[0]);
    }
    
    const status = { ...this.mockStatuses[statusIndex] };
    const stepIndex = status.steps!.findIndex(s => s.stepId === stepId);
    
    if (stepIndex === -1) {
      return of(status);
    }
    
    // Update the step
    status.steps![stepIndex] = {
      ...status.steps![stepIndex],
      ...stepData
    };
    
    // Recalculate completed steps
    status.completedSteps = status.steps!.filter(step => step.status === 'Completed').length;
    
    // Update the status in the mock data
    this.mockStatuses[statusIndex] = status;
    
    return of(status);
  }
  
  setCurrentStep(statusId: string, stepId: string): Observable<Status> {
    const statusIndex = this.mockStatuses.findIndex(s => s.statusId === statusId);
    if (statusIndex === -1 || !this.mockStatuses[statusIndex].steps) {
      return of(this.mockStatuses[0]);
    }
    
    const status = { ...this.mockStatuses[statusIndex] };
    const stepExists = status.steps!.some(s => s.stepId === stepId);
    
    if (!stepExists) {
      return of(status);
    }
    
    status.currentStepId = stepId;
    
    // Update the status in the mock data
    this.mockStatuses[statusIndex] = status;
    
    return of(status);
  }
  
  completeStep(statusId: string, stepId: string, notes?: string): Observable<Status> {
    const statusIndex = this.mockStatuses.findIndex(s => s.statusId === statusId);
    if (statusIndex === -1 || !this.mockStatuses[statusIndex].steps) {
      return of(this.mockStatuses[0]);
    }
    
    const status = { ...this.mockStatuses[statusIndex] };
    const stepIndex = status.steps!.findIndex(s => s.stepId === stepId);
    
    if (stepIndex === -1) {
      return of(status);
    }
    
    // Update the step
    status.steps![stepIndex] = {
      ...status.steps![stepIndex],
      status: 'Completed',
      completionDate: new Date().toISOString(),
      notes: notes || status.steps![stepIndex].notes
    };
    
    // Recalculate completed steps
    status.completedSteps = status.steps!.filter(step => step.status === 'Completed').length;
    
    // If this was the current step, move to the next step
    if (status.currentStepId === stepId) {
      const nextSteps = status.steps!
        .filter(s => s.status !== 'Completed' && s.status !== 'Skipped')
        .sort((a, b) => a.order - b.order);
      
      if (nextSteps.length > 0) {
        status.currentStepId = nextSteps[0].stepId;
      }
    }
    
    // Update the status in the mock data
    this.mockStatuses[statusIndex] = status;
    
    return of(status);
  }
  
  skipStep(statusId: string, stepId: string, reason: string): Observable<Status> {
    const statusIndex = this.mockStatuses.findIndex(s => s.statusId === statusId);
    if (statusIndex === -1 || !this.mockStatuses[statusIndex].steps) {
      return of(this.mockStatuses[0]);
    }
    
    const status = { ...this.mockStatuses[statusIndex] };
    const stepIndex = status.steps!.findIndex(s => s.stepId === stepId);
    
    if (stepIndex === -1) {
      return of(status);
    }
    
    // Update the step
    status.steps![stepIndex] = {
      ...status.steps![stepIndex],
      status: 'Skipped',
      notes: reason + (status.steps![stepIndex].notes ? '\n\nPrevious notes: ' + status.steps![stepIndex].notes : '')
    };
    
    // If this was the current step, move to the next step
    if (status.currentStepId === stepId) {
      const nextSteps = status.steps!
        .filter(s => s.status !== 'Completed' && s.status !== 'Skipped')
        .sort((a, b) => a.order - b.order);
      
      if (nextSteps.length > 0) {
        status.currentStepId = nextSteps[0].stepId;
      }
    }
    
    // Update the status in the mock data
    this.mockStatuses[statusIndex] = status;
    
    return of(status);
  }
  
  blockStep(statusId: string, stepId: string, reason: string): Observable<Status> {
    const statusIndex = this.mockStatuses.findIndex(s => s.statusId === statusId);
    if (statusIndex === -1 || !this.mockStatuses[statusIndex].steps) {
      return of(this.mockStatuses[0]);
    }
    
    const status = { ...this.mockStatuses[statusIndex] };
    const stepIndex = status.steps!.findIndex(s => s.stepId === stepId);
    
    if (stepIndex === -1) {
      return of(status);
    }
    
    // Update the step
    status.steps![stepIndex] = {
      ...status.steps![stepIndex],
      status: 'Blocked',
      notes: 'BLOCKED: ' + reason + (status.steps![stepIndex].notes ? '\n\nPrevious notes: ' + status.steps![stepIndex].notes : '')
    };
    
    // Update the status in the mock data
    this.mockStatuses[statusIndex] = status;
    
    return of(status);
  }
  
  reorderSteps(statusId: string, stepOrder: {stepId: string, order: number}[]): Observable<Status> {
    const statusIndex = this.mockStatuses.findIndex(s => s.statusId === statusId);
    if (statusIndex === -1 || !this.mockStatuses[statusIndex].steps) {
      return of(this.mockStatuses[0]);
    }
    
    const status = { ...this.mockStatuses[statusIndex] };
    
    // Update the order of each step
    stepOrder.forEach(item => {
      const stepIndex = status.steps!.findIndex(s => s.stepId === item.stepId);
      if (stepIndex !== -1) {
        status.steps![stepIndex].order = item.order;
      }
    });
    
    // Sort steps by order
    status.steps = status.steps!.sort((a, b) => a.order - b.order);
    
    // Update the status in the mock data
    this.mockStatuses[statusIndex] = status;
    
    return of(status);
  }
} 