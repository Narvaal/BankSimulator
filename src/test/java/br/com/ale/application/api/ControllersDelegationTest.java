package br.com.ale.application.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import br.com.ale.application.account.querry.GetAccountDetailsUseCase;
import br.com.ale.application.account.querry.GetPublicAccountProfileUseCase;
import br.com.ale.application.account.querry.SearchAccountsByNameUseCase;
import br.com.ale.application.account.usecase.ChangePasswordUseCase;
import br.com.ale.application.account.usecase.CreateAccountUseCase;
import br.com.ale.application.account.usecase.DepositAccountUseCase;
import br.com.ale.application.account.usecase.RequestPasswordResetUseCase;
import br.com.ale.application.claim.usecase.ClaimArtifactUnitUseCase;
import br.com.ale.application.claim.usecase.GetNextClaimUseCase;
import br.com.ale.application.client.query.GetClientProfileUseCase;
import br.com.ale.application.marketplace.query.GetArtifactByIdUseCase;
import br.com.ale.application.marketplace.query.GetArtifactListingByIdUseCase;
import br.com.ale.application.marketplace.query.GetArtifactUnitByIdUseCase;
import br.com.ale.application.marketplace.query.ListActiveArtifactListingsUseCase;
import br.com.ale.application.marketplace.query.ListArtifactBundleItemsUseCase;
import br.com.ale.application.marketplace.query.ListArtifactBundlesUseCase;
import br.com.ale.application.marketplace.query.ListArtifactListingsByOwnerUseCase;
import br.com.ale.application.marketplace.query.ListArtifactPriceHistoryByArtifactIdUseCase;
import br.com.ale.application.marketplace.query.ListArtifactUnitsByOwnerUseCase;
import br.com.ale.application.marketplace.query.ListArtifactsUseCase;
import br.com.ale.application.marketplace.usecase.CancelArtifactOfferUseCase;
import br.com.ale.application.marketplace.usecase.CreateArtifactBundleUseCase;
import br.com.ale.application.marketplace.usecase.CreateArtifactOfferUseCase;
import br.com.ale.application.marketplace.usecase.CreateArtifactUnitForAccountUseCase;
import br.com.ale.application.marketplace.usecase.PurchaseArtifactUseCase;
import br.com.ale.application.transaction.query.ListTransfersByAccountUseCase;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.auth.AuthCookieService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.auth.JwtService;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

/** Controllers são camada de delegação — verifica roteamento de parâmetros para os use cases. */
class ControllersDelegationTest {

  private final AuthCookieService cookieService = new AuthCookieService();

  private MockHttpServletRequest authenticated() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("AUTH_TOKEN", "jwt-token"));
    return request;
  }

  @Test
  void accountControllerShouldDelegateToUseCases() {
    var createUseCase = mock(CreateAccountUseCase.class);
    var resetRequestUseCase = mock(RequestPasswordResetUseCase.class);
    var changePasswordUseCase = mock(ChangePasswordUseCase.class);
    var detailsUseCase = mock(GetAccountDetailsUseCase.class);
    var profileUseCase = mock(GetPublicAccountProfileUseCase.class);
    var searchUseCase = mock(SearchAccountsByNameUseCase.class);

    var controller =
        new AccountController(
            cookieService,
            createUseCase,
            resetRequestUseCase,
            changePasswordUseCase,
            detailsUseCase,
            profileUseCase,
            searchUseCase);

    var response =
        controller.create(
            new CreateAccountApiRequest("John", "j@t.com", "Str0ng!Pass", null, null, false, null));
    assertNotNull(response.text());
    verify(createUseCase).execute(any());

    controller.me(authenticated());
    verify(detailsUseCase).execute("jwt-token");

    controller.requestReset(new EmailRequest("j@t.com"));
    verify(resetRequestUseCase).execute(any());

    controller.resetPassword(new CreateResetPasswordRequest("N3w!Pass99", "tok"));
    verify(changePasswordUseCase).execute(any());

    controller.profile(7L);
    verify(profileUseCase).execute(7L);

    controller.search("john", 0, 10);
    verify(searchUseCase).execute("john", 0, 10);
  }

  @Test
  void accountOperationsControllerShouldResolveClientIdFromBodyOrToken() {
    var depositUseCase = mock(DepositAccountUseCase.class);
    var accountService = mock(AccountService.class);
    var jwtService = mock(JwtService.class);
    var controller = new AccountOperationsController(depositUseCase, accountService, jwtService);

    Account account =
        new Account(
            1L,
            2L,
            "111",
            AccountType.DEFAULT,
            BigDecimal.ZERO,
            AccountStatus.ACTIVE,
            "pk",
            Instant.now());
    when(accountService.getAccountByClientId(anyLong())).thenReturn(Optional.of(account));
    when(jwtService.extractClientId(anyString())).thenReturn(5L);

    assertEquals(
        200,
        controller
            .deposit(new AccountOperationsController.AdminDepositRequest(2L, null, "10.00"))
            .getStatusCode()
            .value());
    assertEquals(
        200,
        controller
            .deposit(new AccountOperationsController.AdminDepositRequest(null, "jwt", "10.00"))
            .getStatusCode()
            .value());
    assertEquals(
        400,
        controller
            .deposit(new AccountOperationsController.AdminDepositRequest(null, null, "10.00"))
            .getStatusCode()
            .value());
    verify(accountService, times(2)).credit(eq("111"), any());
  }

  @Test
  void artifactQueryControllerShouldDelegate() {
    var getById = mock(GetArtifactByIdUseCase.class);
    var priceHistory = mock(ListArtifactPriceHistoryByArtifactIdUseCase.class);
    var list = mock(ListArtifactsUseCase.class);
    var bundles = mock(ListArtifactBundlesUseCase.class);
    var bundleItems = mock(ListArtifactBundleItemsUseCase.class);
    var createUnit = mock(CreateArtifactUnitForAccountUseCase.class);
    var createBundle = mock(CreateArtifactBundleUseCase.class);

    var controller =
        new ArtifactQueryController(
            getById, priceHistory, list, bundles, bundleItems, createUnit, createBundle);

    controller.getById(3L);
    verify(getById).execute(3L);

    controller.createUnity(3L, new CreateArtifactUnitApiRequest(9L, "body-token"), "Bearer h");
    verify(createUnit).execute(any());

    controller.list();
    verify(list).execute();

    controller.listBundles(0, 20);
    verify(bundles).execute(0, 20);

    controller.createBundle(new CreateArtifactBundleApiRequest(List.of(), "weekly-1"));
    verify(createBundle).execute(any());

    controller.listBundleItems(1L, 0, 20);
    verify(bundleItems).execute(1L, 0, 20);

    controller.priceHistory(4L);
    verify(priceHistory).execute(4L);
  }

  @Test
  void listingAndOfferControllersShouldDelegate() {
    var cancel = mock(CancelArtifactOfferUseCase.class);
    var create = mock(CreateArtifactOfferUseCase.class);
    var offerController = new ArtifactOfferController(cancel, create, cookieService);

    offerController.create(
        new CreateArtifactOfferApiRequest(1L, 2L, BigDecimal.TEN, null), authenticated());
    verify(create).execute(any());

    offerController.cancel(new CancelArtifactOfferApiRequest(1L, 3L), authenticated());
    verify(cancel).execute(any());

    var getListing = mock(GetArtifactListingByIdUseCase.class);
    var listActive = mock(ListActiveArtifactListingsUseCase.class);
    var listByOwner = mock(ListArtifactListingsByOwnerUseCase.class);
    var listingController =
        new ArtifactListingQueryController(cookieService, getListing, listActive, listByOwner);

    listingController.getById(5L);
    verify(getListing).execute(5L);

    listingController.list(0, 10, null, null, null, null, null, new MockHttpServletRequest());
    verify(listActive).execute(isNull(), any(ArtifactListingFilter.class), eq(0), eq(10));

    listingController.user(0, 10, authenticated());
    verify(listByOwner).execute("jwt-token", 0, 10);
  }

  @Test
  void unitClaimPurchaseAndMiscControllersShouldDelegate() {
    var listUnits = mock(ListArtifactUnitsByOwnerUseCase.class);
    var getUnit = mock(GetArtifactUnitByIdUseCase.class);
    var unitController = new ArtifactUnitQueryController(listUnits, getUnit);

    unitController.getById(8L);
    verify(getUnit).execute(8L);
    unitController.listByOwner(2L, 0, 10);
    verify(listUnits).execute(2L, 0, 10);

    var claim = mock(ClaimArtifactUnitUseCase.class);
    var nextClaim = mock(GetNextClaimUseCase.class);
    when(claim.execute(any())).thenReturn(Instant.now());
    var claimController = new ClaimArtifactUnitController(cookieService, claim, nextClaim);

    claimController.claimArtifactUnit(new ClaimArtifactUnitApiRequest(4L), authenticated());
    verify(claim).execute(any());
    claimController.getNextClaim(authenticated());
    verify(nextClaim).execute(any());

    var purchase = mock(PurchaseArtifactUseCase.class);
    var purchaseController = new ArtifactPurchaseController(purchase, cookieService);
    purchaseController.purchase(6L, authenticated());
    verify(purchase).execute(any());

    var clientProfile = mock(GetClientProfileUseCase.class);
    var clientController = new ClientController(clientProfile);
    clientController.me("Bearer abc", null);
    verify(clientProfile).execute("abc");
    clientController.me(null, "param-token");
    verify(clientProfile).execute("param-token");

    var transfers = mock(ListTransfersByAccountUseCase.class);
    var transfersController = new AccountTransfersController(transfers);
    transfersController.listTransfers(3L, "Bearer abc", null);
    verify(transfers).execute(3L, "abc");
  }

  @Test
  void kofiWebhookShouldValidateTokenAndDeposit() {
    var deposit = mock(DepositAccountUseCase.class);
    var controller = new KofiWebhookController(deposit);
    ReflectionTestUtils.setField(controller, "verificationToken", "kofi-secret");

    String valid =
        """
        {"verification_token":"kofi-secret","email":"j@t.com","amount":"5.00","currency":"USD"}
        """;
    assertEquals(200, controller.handleWebhook(valid).getStatusCode().value());
    verify(deposit).execute(any());

    String wrongToken =
        """
        {"verification_token":"wrong","email":"j@t.com","amount":"5.00","currency":"USD"}
        """;
    // IllegalAccessError é Error (não Exception) — escapa do catch do controller
    assertThrows(IllegalAccessError.class, () -> controller.handleWebhook(wrongToken));

    assertEquals(400, controller.handleWebhook("not-json").getStatusCode().value());
  }
}
