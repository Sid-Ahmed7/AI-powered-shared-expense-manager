package com.pactole.services.password;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pactole.entities.Account;
import com.pactole.exceptions.Errors;
import com.pactole.security.Limits;
import com.pactole.security.PasswordPolicy;
import com.pactole.security.RateLimiter;

@Service 
public class PasswordService implements IPasswordService {

    private final RateLimiter rateLimiter;
    private final PasswordPolicy passwordPolicy;
    private final Limits limits;
    private final PasswordEncoder passwordEncoder; 

    public PasswordService(RateLimiter rateLimiter, PasswordPolicy passwordPolicy, Limits limits, PasswordEncoder passwordEncoder) {
        this.rateLimiter = rateLimiter;
        this.passwordPolicy = passwordPolicy;
        this.limits = limits;
        this.passwordEncoder = passwordEncoder;
    }

    public void requirePassword(Account account, String password) {
        long wait = rateLimiter.tryAcquire(limits.loginPerEmail, account.getEmail());
        if (wait > 0) {
            throw new Errors.TooManyRequests("rate.limit.exceeded", wait);
        }
        if (password == null || !passwordEncoder.matches(password, account.getPassword())) {
            throw new Errors.Forbidden("auth.password.current.invalid");
        }
    }
    
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void validateNewPassword(String password, Account account) {
        passwordPolicy.validate(password, account.getEmail(), account.getFullName());
        if (passwordEncoder.matches(password, account.getPassword())) {
            throw new Errors.Validation("auth.password.same");
        }
    }
    
}
