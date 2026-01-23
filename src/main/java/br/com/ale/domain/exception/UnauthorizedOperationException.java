package br.com.ale.domain.exception;

public class UnauthorizedOperationException extends BusinessRuleException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
