package com.pactole.services.passwordReset;

public interface IPasswordResetService {
    
    void requestReset(String email);
    void resetPassword(String token, String newPassword);
}
