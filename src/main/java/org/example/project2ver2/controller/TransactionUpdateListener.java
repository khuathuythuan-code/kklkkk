package org.example.project2ver2.controller;

import org.example.project2ver2.model.Transaction;

public interface TransactionUpdateListener {
    void onTransactionUpdated(Transaction transaction);

    void onTransactionDeleted(Transaction transaction);
}