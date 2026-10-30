export interface Account {
  id: string; customerReference: string; status: 'ACTIVE' | 'BLOCKED' | string;
  currency: string; balance: number; createdAt: string;
}
export interface Transaction {
  id: string; accountId: string; amount: number; currency: string; merchantCategory?: string;
  country?: string; createdAt: string; type: string; transferId?: string; counterpartyAccountId?: string;
}
export interface AuditEntry { id: number; messageKey: string; partitionNo: number; offsetNo: number; payload: string; receivedAt: string; }
export interface ChatMessage { from: 'user' | 'bot'; text: string; }
