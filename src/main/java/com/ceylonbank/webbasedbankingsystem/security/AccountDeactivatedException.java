package com.ceylonbank.webbasedbankingsystem.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom exception for deactivated accounts that won't be wrapped by Spring Security
 */
public class AccountDeactivatedException extends AuthenticationException {
    
    public AccountDeactivatedException(String msg) {
        super(msg);
    }
    
    public AccountDeactivatedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
