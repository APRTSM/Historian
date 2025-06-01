    public void transfer(int debitAccountId, int creditAccountId, String amount) {
        Optional<Account> debitedAccountWrapper = accountRepository.findById(debitAccountId);
        Optional<Account> creditedAccountWrapper = accountRepository.findById(creditAccountId);
        
        Account debitedAccount = debitedAccountWrapper.orElseThrow(NoSuchElementException::new);
        Account creditedAccount = creditedAccountWrapper.orElseThrow(NoSuchElementException::new);
        
        if (debitedAccount.isDisabled() || creditedAccount.isDisabled()){
            throw new IllegalStateException("Account disabled");
        }

        BigDecimal debitedAccountBalance = calculateDebitedBalance(debitedAccount, amount);
        BigDecimal creditedAccountBalance = calculateCreditedBalance(creditedAccount, amount);
        
        debitedAccount.setBalance(debitedAccountBalance);
        creditedAccount.setBalance(creditedAccountBalance);
        
        accountRepository.save(debitedAccount);
        accountRepository.save(creditedAccount);
    }
