package com.bancolombia.training.transactions.model;

import java.util.UUID;

/*
{
  "accountId": "11111111-1111-4111-8111-111111111111",
  "type": "DEPOSIT",
  "amount": "50000.00",
  "currency": "COP"
}
 */
public record TransactionRequest (
    UUID accountId,
    Type type,
    String amount,
    String currency 
) {

    
}
