package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.utils.ValidationMessages;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public void transfer(Account fromAccount, Account toAccount, BigDecimal amount) {

        //preconditions
        if (fromAccount==null || toAccount==null || amount.compareTo(new BigDecimal(0)) <= 0) {
            throw new IllegalArgumentException(ValidationMessages.FROM_ACCOUNT_AND_TO_ACCOUNT_SHOULD_NOT_BE_NULL_AND_TRANSFER_AMOUNT_SHOULD_BE_GREATER_THAN_0);
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(ValidationMessages.FROM_ACCOUNT_DOES_NOT_CONTAIN_SUFFICIENT_FUNDS);
        }

        //update accounts and add a new transaction
        BigDecimal fromAccountBalance = fromAccount.getBalance().subtract(amount);
        fromAccount.setBalance(fromAccountBalance);
        BigDecimal toAccountBalance = toAccount.getBalance().add(fromAccountBalance);
        toAccount.setBalance(toAccountBalance);



    }

}
