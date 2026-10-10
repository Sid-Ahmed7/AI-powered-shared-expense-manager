package com.pactole.services.password;

import com.pactole.entities.Account;

public interface IPasswordService {

    void requirePassword(Account account, String password);
    String encodePassword(String password);
    void validateNewPassword(String password, Account account);
}
