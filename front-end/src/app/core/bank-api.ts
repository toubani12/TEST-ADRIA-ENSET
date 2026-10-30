import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Account, AuditEntry, Transaction } from './models';

@Injectable({ providedIn: 'root' })
export class BankApi {
  private http = inject(HttpClient);

  accounts(): Observable<Account[]> { return this.http.get<Account[]>('/api/accounts'); }
  createAccount(customerReference: string, currency: string, initialBalance: number) {
    return this.http.post<Account>('/api/accounts', { customerReference, currency, initialBalance });
  }
  block(id: string) { return this.http.put<Account>(`/api/accounts/${id}/block`, {}); }
  activate(id: string) { return this.http.put<Account>(`/api/accounts/${id}/activate`, {}); }

  transactions(accountId?: string): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(accountId ? `/api/transactions/account/${accountId}` : '/api/transactions');
  }
  payment(accountId: string, amount: number, currency: string, merchantCategory: string, country: string) {
    return this.http.post<Transaction>('/api/transactions', { accountId, amount, currency, merchantCategory, country });
  }
  deposit(accountId: string, amount: number, currency: string) {
    return this.http.post<Transaction>('/api/transactions/deposits', { accountId, amount, currency });
  }
  transfer(fromAccountId: string, toAccountId: string, amount: number, currency: string) {
    return this.http.post('/api/transactions/transfers', { fromAccountId, toAccountId, amount, currency });
  }

  audits(): Observable<AuditEntry[]> { return this.http.get<AuditEntry[]>('/api/audits'); }
  chat(message: string) { return this.http.post<{ answer: string }>('/api/chat', { message }); }
}
