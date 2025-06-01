    public void disable(int accountId) {
        Optional<Account> wrappedAccount = accountRepository.findById(accountId);
        Account account = wrappedAccount.orElseThrow(NoSuchElementException::new);
        account.setDisabled(true);
        accountRepository.save(account);
    }
