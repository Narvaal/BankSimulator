package br.com.ale.application.api;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.exception.AccountNotFoundException;
import br.com.ale.domain.exception.ArtifactUnitNotFoundException;
import br.com.ale.domain.exception.BusinessRuleException;
import br.com.ale.domain.exception.InvalidCredentialsException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;

class ApiExceptionHandlerTest {

  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void invalidCredentialsShouldMapTo401() {
    var response = handler.handleInvalidCredentials(new InvalidCredentialsException("bad login"));
    assertEquals(401, response.getStatusCode().value());
    assertEquals("bad login", response.getBody().get("error"));
  }

  @Test
  void unauthorizedOperationShouldMapTo403() {
    var response =
        handler.handleUnauthorizedOperation(new UnauthorizedOperationException("not yours"));
    assertEquals(403, response.getStatusCode().value());
  }

  @Test
  void notFoundExceptionsShouldMapTo404() {
    assertEquals(
        404,
        handler
            .handleArtifactUnitNotFound(new ArtifactUnitNotFoundException(1L))
            .getStatusCode()
            .value());
    assertEquals(
        404,
        handler.handleAccountNotFound(new AccountNotFoundException(2L)).getStatusCode().value());
  }

  @Test
  void businessRuleShouldMapTo400() {
    var response = handler.handleBusinessRule(new BusinessRuleException("invalid state"));
    assertEquals(400, response.getStatusCode().value());
    assertEquals("invalid state", response.getBody().get("error"));
  }
}
