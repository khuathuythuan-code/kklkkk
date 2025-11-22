package com.ExpenseTracker;

import com.ExpenseTracker.model.Transaction;

public interface TransactionUpdateListener {
    void onTransactionUpdated(Transaction transaction);

    void onTransactionDeleted(Transaction transaction);
}
