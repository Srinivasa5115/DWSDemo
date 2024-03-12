package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.utils.ValidationMessages;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;


@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }


  @PostMapping(path="/account/{fromAccountId}/transfers")
  public ResponseEntity<?> transferAmount(@PathVariable String fromAccountId, @RequestBody Account transferAccount) {
    //validation checks
    if (transferAccount == null || transferAccount.getAccountId() == null || transferAccount.getBalance() == null) {
      return ResponseEntity.badRequest().body(ValidationMessages.TO_TRANSFER_BOTH_TO_ACCOUNT_ID_AND_BALANCE_ARE_NEEDED);
    }
    //check balance is positive
    if (transferAccount.getBalance().compareTo(new BigDecimal("0.0")) < 1) {
      return ResponseEntity.badRequest().body(ValidationMessages.TRANSFER_AMOUNT_SHOULD_BE_GREATER_THAN_0);
    }

    //check transferTo account is not the same as from account
    if (fromAccountId.equals(transferAccount.getAccountId())) {
      return ResponseEntity.badRequest().body(ValidationMessages.FROM_ACCOUNT_ID_AND_TO_ACCOUNT_ID_CANNOT_BE_SAME);
    }

    //check transferTo account id exist
    Account toAccount = accountsService.getAccount(transferAccount.getAccountId());

    //check fromAccountId exists
    Account fromAccount = accountsService.getAccount(fromAccountId);


    //check from account has that much balance
    if (fromAccount.getBalance().compareTo(transferAccount.getBalance()) < 0) {
      return ResponseEntity.badRequest().body(ValidationMessages.FROM_ACCOUNT_ID_DOES_NOT_HAVE_SUFFICIENT_FUNDS);
    }

    try {
      accountsService.transfer(fromAccount, toAccount, transferAccount.getBalance());
    } catch(Exception ex) {
      // message is sent back to user.
      return ResponseEntity.badRequest().body(ValidationMessages.MONEY_TRANSFER_TRANSACTION_FAILED_PLEASE_TRY_AGAIN);
    }
    return ResponseEntity.ok().body(ValidationMessages.TRANSFER_SUCCESSFUL);
  }

}
