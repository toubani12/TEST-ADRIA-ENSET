import { Component, inject, signal, computed } from '@angular/core';
import { DecimalPipe, DatePipe, SlicePipe, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { BankApi } from './core/bank-api';
import { Account, AuditEntry, ChatMessage, Transaction } from './core/models';

type Tab = 'comptes' | 'operations' | 'historique' | 'audit' | 'assistant';

@Component({
  selector: 'app-root',
  imports: [FormsModule, DecimalPipe, DatePipe, SlicePipe, UpperCasePipe],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private api = inject(BankApi);

  tab = signal<Tab>('comptes');
  accounts = signal<Account[]>([]);
  transactions = signal<Transaction[]>([]);
  audits = signal<AuditEntry[]>([]);
  selected = signal<string>('');
  message = signal<{ ok: boolean; text: string } | null>(null);

  totalBalance = computed(() => this.accounts().reduce((s, a) => s + a.balance, 0));
  activeCount = computed(() => this.accounts().filter(a => a.status === 'ACTIVE').length);

  // formulaires
  newAccount = { customerReference: '', currency: 'MAD', initialBalance: 0 };
  op = { kind: 'payment', accountId: '', toAccountId: '', amount: 100, currency: 'MAD', merchantCategory: 'GROCERY', country: 'MA' };

  // assistant
  chat = signal<ChatMessage[]>([{ from: 'bot', text: 'Bonjour, je suis l\'assistant ADRIA. Demandez-moi un solde, un paiement ou un virement.' }]);
  question = '';
  thinking = signal(false);

  constructor() { this.refresh(); }

  refresh() {
    this.api.accounts().subscribe({ next: a => this.accounts.set(a), error: e => this.fail(e) });
    this.api.transactions(this.selected() || undefined).subscribe({ next: t => this.transactions.set(t), error: () => {} });
    this.api.audits().subscribe({ next: a => this.audits.set(a), error: () => {} });
  }

  show(t: Tab) { this.tab.set(t); this.refresh(); }

  createAccount() {
    const f = this.newAccount;
    this.api.createAccount(f.customerReference, f.currency, +f.initialBalance).subscribe({
      next: a => { this.ok(`Compte ${a.customerReference} créé`); this.newAccount = { customerReference: '', currency: 'MAD', initialBalance: 0 }; this.refresh(); },
      error: e => this.fail(e)
    });
  }

  toggle(a: Account) {
    (a.status === 'ACTIVE' ? this.api.block(a.id) : this.api.activate(a.id)).subscribe({
      next: () => { this.ok(`Compte ${a.customerReference} ${a.status === 'ACTIVE' ? 'bloqué' : 'activé'}`); this.refresh(); },
      error: e => this.fail(e)
    });
  }

  submitOperation() {
    const o = this.op;
    const call = o.kind === 'deposit' ? this.api.deposit(o.accountId, +o.amount, o.currency)
      : o.kind === 'transfer' ? this.api.transfer(o.accountId, o.toAccountId, +o.amount, o.currency)
      : this.api.payment(o.accountId, +o.amount, o.currency, o.merchantCategory, o.country);
    call.subscribe({ next: () => { this.ok('Opération enregistrée'); this.refresh(); }, error: e => this.fail(e) });
  }

  filterHistory(id: string) { this.selected.set(id); this.refresh(); }

  ref(id?: string) { return this.accounts().find(a => a.id === id)?.customerReference ?? (id ?? '').slice(0, 8); }

  ask() {
    const q = this.question.trim();
    if (!q || this.thinking()) return;
    this.chat.update(c => [...c, { from: 'user', text: q }]);
    this.question = '';
    this.thinking.set(true);
    this.api.chat(q).subscribe({
      next: r => { this.chat.update(c => [...c, { from: 'bot', text: r.answer }]); this.thinking.set(false); this.refresh(); },
      error: e => { this.chat.update(c => [...c, { from: 'bot', text: 'Erreur : ' + this.errText(e) }]); this.thinking.set(false); }
    });
  }

  private ok(text: string) { this.message.set({ ok: true, text }); }
  private fail(e: HttpErrorResponse) { this.message.set({ ok: false, text: this.errText(e) }); }
  private errText(e: HttpErrorResponse) { return e.error?.detail || e.error?.message || e.message; }
}
