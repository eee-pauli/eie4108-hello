package eee.eie4108.eie4108hello.lab1;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.StringReader;
import java.util.UUID;

public class LabTestClient {
  
  // CHANGE THIS URL to match your server's address
  private static final String BASE_URI = "http://localhost:8080/bank";
  
  private static Client client;
  private static WebTarget target;
  
  // Test Statistics
  private static int totalTests = 20;
  private static int testsRun = 0;
  private static int testsPassed = 0;
  
  // State persistence for dependent tests
  private static String userA_Id = null;
  private static String userB_Id = null;
  
  // State persistence for Balance Verification
  private static double currentBalanceA = 0.0;
  private static double currentBalanceB = 0.0;
  
  private static final String USER_A_USER = "alice.smith_" + System.currentTimeMillis();
  private static final String USER_A_PASS = "alicePass123";
  private static final String USER_B_USER = "bob.jones_" + System.currentTimeMillis();
  private static final String USER_B_PASS = "bobPass456";
  
  public static void main(String[] args) {
    client = ClientBuilder.newClient();
    target = client.target(BASE_URI);
    
    System.out.println("Starting LabTestClient on " + BASE_URI + "...\n");
    
    // --- User Management Tests ---
    testCreateUserA();                  // Test 1
    testCreateUserB();                  // Test 2
    testCreateDuplicateUser();          // Test 3
    testCreateNegativeDeposit();        // Test 4
    
    testGetUserA();                     // Test 5
    testGetUserWrongPass();             // Test 6
    testGetUserNotFound();              // Test 7
    
    // --- Deposit Tests ---
    testDepositSuccess();               // Test 8
    testDepositNegative();              // Test 9
    testDepositWrongPass();             // Test 10
    
    // --- Withdraw Tests ---
    testWithdrawSuccess();              // Test 11
    testWithdrawOverdraft();            // Test 12
    testWithdrawNegative();             // Test 13
    testWithdrawWrongPass();            // Test 14
    
    // --- Transfer Tests ---
    testTransferSuccess();              // Test 15
    testTransferInsufficient();         // Test 16
    testTransferNegative();             // Test 17
    testTransferSenderNotFound();       // Test 18
    testTransferReceiverNotFound();     // Test 19
    testTransferWrongPass();            // Test 20
    
    // Final Summary
    printSummary();
    client.close();
  }
  
  // ==========================================
  // Test Case Implementations
  // ==========================================
  
  private static void testCreateUserA() {
    double initialDep = 100.00;
    JsonObject json = Json.createObjectBuilder()
                          .add("fullname", "Alice Smith")
                          .add("username", USER_A_USER)
                          .add("password", USER_A_PASS)
                          .add("initialDeposit", initialDep)
                          .build();
    
    Response response = target.path("users")
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    boolean passed = false;
    if (response.getStatus() == 201) {
      JsonObject resBody = parseJson(response);
      if (resBody != null && resBody.containsKey("userId") && resBody.containsKey("balance")) {
        userA_Id = resBody.getString("userId");
        double returnedBalance = resBody.getJsonNumber("balance").doubleValue();
        
        if (isCloseEnough(returnedBalance, initialDep)) {
          currentBalanceA = returnedBalance;
          passed = true;
        }
      }
    }
    logResult("Create User A (Alice)", passed);
  }
  
  private static void testCreateUserB() {
    double initialDep = 50.00;
    JsonObject json = Json.createObjectBuilder()
                          .add("fullname", "Bob Jones")
                          .add("username", USER_B_USER)
                          .add("password", USER_B_PASS)
                          .add("initialDeposit", initialDep)
                          .build();
    
    Response response = target.path("users")
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    boolean passed = false;
    if (response.getStatus() == 201) {
      JsonObject resBody = parseJson(response);
      if (resBody != null && resBody.containsKey("userId") && resBody.containsKey("balance")) {
        userB_Id = resBody.getString("userId");
        double returnedBalance = resBody.getJsonNumber("balance").doubleValue();
        
        if (isCloseEnough(returnedBalance, initialDep)) {
          currentBalanceB = returnedBalance;
          passed = true;
        }
      }
    }
    logResult("Create User B (Bob)", passed);
  }
  
  private static void testCreateDuplicateUser() {
    JsonObject json = Json.createObjectBuilder()
                          .add("fullname", "Alice Imposter")
                          .add("username", USER_A_USER)
                          .add("password", "whatever")
                          .add("initialDeposit", 100.00)
                          .build();
    
    Response response = target.path("users")
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    logResult("Create user with existing username (expect 400)", response.getStatus() == 400);
  }
  
  private static void testCreateNegativeDeposit() {
    JsonObject json = Json.createObjectBuilder()
                          .add("fullname", "Negative Ned")
                          .add("username", "ned")
                          .add("password", "pass")
                          .add("initialDeposit", -10.00)
                          .build();
    
    Response response = target.path("users")
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    logResult("Create user negative deposit (expect 400)", response.getStatus() == 400);
  }
  
  private static void testGetUserA() {
    if (userA_Id == null) { logResult("Get User A (Skip - ID null)", false); return; }
    
    boolean passed = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    logResult("Get User A info & Verify Balance (expect 200)", passed);
  }
  
  private static void testGetUserWrongPass() {
    if (userA_Id == null) { logResult("Get User Wrong Pass (Skip)", false); return; }
    
    Response response = target.path("users/" + userA_Id)
                              .queryParam("x-pass", "WRONG_PASSWORD")
                              .request(MediaType.APPLICATION_JSON)
                              .get();
    
    logResult("Get User with wrong pass (expect 401)", response.getStatus() == 401);
  }
  
  private static void testGetUserNotFound() {
    String fakeId = UUID.randomUUID().toString();
    Response response = target.path("users/" + fakeId)
                              .queryParam("x-pass", "any")
                              .request(MediaType.APPLICATION_JSON)
                              .get();
    
    logResult("Get non-existent user (expect 404)", response.getStatus() == 404);
  }
  
  private static void testDepositSuccess() {
    if (userA_Id == null) { logResult("Deposit Success (Skip)", false); return; }
    
    double amount = 50.00;
    
    Response response = target.path("accounts/" + userA_Id + "/deposit")
                              .queryParam("x-pass", USER_A_PASS)
                              .queryParam("amount", amount)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(""));
    
    boolean passed = false;
    if (response.getStatus() == 200) {
      JsonObject resBody = parseJson(response);
      if (resBody != null && resBody.containsKey("newBalance")) {
        double newBal = resBody.getJsonNumber("newBalance").doubleValue();
        double expected = currentBalanceA + amount;
        
        if (isCloseEnough(newBal, expected)) {
          currentBalanceA = newBal;
          passed = true;
        }
      }
    }
    logResult("Deposit 50.00 to User A (expect 200)", passed);
  }
  
  private static void testDepositNegative() {
    if (userA_Id == null) { logResult("Deposit Negative (Skip)", false); return; }
    
    Response response = target.path("accounts/" + userA_Id + "/deposit")
                              .queryParam("x-pass", USER_A_PASS)
                              .queryParam("amount", -50.00)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(""));
    
    boolean statusOk = (response.getStatus() == 400);
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Deposit negative (expect 400 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  private static void testDepositWrongPass() {
    if (userA_Id == null) { logResult("Deposit Wrong Pass (Skip)", false); return; }
    
    Response response = target.path("accounts/" + userA_Id + "/deposit")
                              .queryParam("x-pass", "WRONG")
                              .queryParam("amount", 10.00)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(""));
    
    boolean statusOk = (response.getStatus() == 401);
    // NEW: Verify that the wrong password attempt didn't change the balance
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Deposit wrong password (expect 401 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  private static void testWithdrawSuccess() {
    if (userA_Id == null) { logResult("Withdraw Success (Skip)", false); return; }
    
    double amount = 20.00;
    
    Response response = target.path("accounts/" + userA_Id + "/withdraw")
                              .queryParam("x-pass", USER_A_PASS)
                              .queryParam("amount", amount)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(""));
    
    boolean passed = false;
    if (response.getStatus() == 200) {
      JsonObject resBody = parseJson(response);
      if (resBody != null && resBody.containsKey("newBalance")) {
        double newBal = resBody.getJsonNumber("newBalance").doubleValue();
        double expected = currentBalanceA - amount;
        
        if (isCloseEnough(newBal, expected)) {
          currentBalanceA = newBal;
          passed = true;
        }
      }
    }
    logResult("Withdraw 20.00 from User A (expect 200)", passed);
  }
  
  private static void testWithdrawOverdraft() {
    if (userA_Id == null) { logResult("Withdraw Overdraft (Skip)", false); return; }
    
    Response response = target.path("accounts/" + userA_Id + "/withdraw")
                              .queryParam("x-pass", USER_A_PASS)
                              .queryParam("amount", 1000.00)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(""));
    
    boolean statusOk = (response.getStatus() == 400);
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Withdraw overdraft (expect 400 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  private static void testWithdrawNegative() {
    if (userA_Id == null) { logResult("Withdraw Negative (Skip)", false); return; }
    
    Response response = target.path("accounts/" + userA_Id + "/withdraw")
                              .queryParam("x-pass", USER_A_PASS)
                              .queryParam("amount", -10.00)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(""));
    
    boolean statusOk = (response.getStatus() == 400);
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Withdraw negative (expect 400 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  private static void testWithdrawWrongPass() {
    if (userA_Id == null) { logResult("Withdraw Wrong Pass (Skip)", false); return; }
    
    Response response = target.path("accounts/" + userA_Id + "/withdraw")
                              .queryParam("x-pass", "WRONG")
                              .queryParam("amount", 10.00)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(""));
    
    boolean statusOk = (response.getStatus() == 401);
    // NEW: Verify balance unchanged
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Withdraw wrong password (expect 401 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  private static void testTransferSuccess() {
    if (userA_Id == null || userB_Id == null) { logResult("Transfer Success (Skip)", false); return; }
    
    double amount = 10.00;
    
    JsonObject json = Json.createObjectBuilder()
                          .add("fromUserId", userA_Id)
                          .add("toUserName", USER_B_USER)
                          .add("amount", amount)
                          .build();
    
    Response response = target.path("accounts/transfer")
                              .queryParam("x-pass", USER_A_PASS)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    boolean passed = false;
    
    if (response.getStatus() == 200) {
      JsonObject resBody = parseJson(response);
      if (resBody != null && resBody.containsKey("fromUserNewBalance")) {
        double newSenderBal = resBody.getJsonNumber("fromUserNewBalance").doubleValue();
        double expectedSenderBal = currentBalanceA - amount;
        
        if (isCloseEnough(newSenderBal, expectedSenderBal)) {
          currentBalanceA = newSenderBal;
          currentBalanceB += amount;
          passed = true;
        }
      }
    }
    
    // Explicitly verify Receiver (User B) Balance via GET request
    if (passed) {
      boolean receiverUpdated = verifyServerBalance(userB_Id, USER_B_PASS, currentBalanceB);
      if (!receiverUpdated) {
        System.out.println("   [Error] Transfer sender OK, but receiver balance did not increase!");
        passed = false;
      }
    }
    
    logResult("Transfer 10.00 A->B (expect 200 & Both Updated)", passed);
  }
  
  private static void testTransferInsufficient() {
    if (userA_Id == null) { logResult("Transfer Insufficient (Skip)", false); return; }
    
    JsonObject json = Json.createObjectBuilder()
                          .add("fromUserId", userA_Id)
                          .add("toUserName", USER_B_USER)
                          .add("amount", 10000.00)
                          .build();
    
    Response response = target.path("accounts/transfer")
                              .queryParam("x-pass", USER_A_PASS)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    boolean statusOk = (response.getStatus() == 400);
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Transfer insufficient (expect 400 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  private static void testTransferNegative() {
    if (userA_Id == null) { logResult("Transfer Negative (Skip)", false); return; }
    
    JsonObject json = Json.createObjectBuilder()
                          .add("fromUserId", userA_Id)
                          .add("toUserName", USER_B_USER)
                          .add("amount", -10.00)
                          .build();
    
    Response response = target.path("accounts/transfer")
                              .queryParam("x-pass", USER_A_PASS)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    boolean statusOk = (response.getStatus() == 400);
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Transfer negative (expect 400 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  private static void testTransferSenderNotFound() {
    JsonObject json = Json.createObjectBuilder()
                          .add("fromUserId", UUID.randomUUID().toString())
                          .add("toUserName", USER_B_USER)
                          .add("amount", 10.00)
                          .build();
    
    Response response = target.path("accounts/transfer")
                              .queryParam("x-pass", "any")
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    logResult("Transfer from non-existent user (expect 404)", response.getStatus() == 404);
  }
  
  private static void testTransferReceiverNotFound() {
    if (userA_Id == null) { logResult("Transfer Rec Not Found (Skip)", false); return; }
    
    JsonObject json = Json.createObjectBuilder()
                          .add("fromUserId", userA_Id)
                          .add("toUserName", "ghost.user")
                          .add("amount", 10.00)
                          .build();
    
    Response response = target.path("accounts/transfer")
                              .queryParam("x-pass", USER_A_PASS)
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    logResult("Transfer to non-existent user (expect 404)", response.getStatus() == 404);
  }
  
  private static void testTransferWrongPass() {
    if (userA_Id == null) { logResult("Transfer Wrong Pass (Skip)", false); return; }
    
    JsonObject json = Json.createObjectBuilder()
                          .add("fromUserId", userA_Id)
                          .add("toUserName", USER_B_USER)
                          .add("amount", 10.00)
                          .build();
    
    Response response = target.path("accounts/transfer")
                              .queryParam("x-pass", "WRONG")
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.json(json.toString()));
    
    boolean statusOk = (response.getStatus() == 401);
    // NEW: Verify balance unchanged
    boolean balanceUnchanged = verifyServerBalance(userA_Id, USER_A_PASS, currentBalanceA);
    
    logResult("Transfer wrong password (expect 401 & unchange balance)", statusOk && balanceUnchanged);
  }
  
  // ==========================================
  // Helper Methods
  // ==========================================
  
  private static void logResult(String description, boolean success) {
    testsRun++;
    if (success) testsPassed++;
    
    String status = success ? "Success" : "Failed";
    System.out.printf("Test #%02d/%d %-70s [%s]%n", testsRun, totalTests, description, status);
  }
  
  private static void printSummary() {
    double percentage = (double) testsPassed / totalTests * 100;
    System.out.printf("Total test passed: %d/%d (%.0f%%)%n", testsPassed, totalTests, percentage);
  }
  
  private static JsonObject parseJson(Response response) {
    String entity = response.readEntity(String.class);
    if (entity == null || entity.isEmpty()) return null;
    try (JsonReader reader = Json.createReader(new StringReader(entity))) {
      return reader.readObject();
    } catch (Exception e) {
      return null;
    }
  }
  
  private static boolean isCloseEnough(double actual, double expected) {
    return Math.abs(actual - expected) < 0.01;
  }
  
  /**
   * Helper to verify a user's balance on the server matches our expectation.
   */
  private static boolean verifyServerBalance(String userId, String password, double expectedBalance) {
    Response response = target.path("users/" + userId)
                              .queryParam("x-pass", password)
                              .request(MediaType.APPLICATION_JSON)
                              .get();
    
    if (response.getStatus() != 200) return false;
    
    JsonObject json = parseJson(response);
    if (json == null || !json.containsKey("balance")) return false;
    
    double actual = json.getJsonNumber("balance").doubleValue();
    return isCloseEnough(actual, expectedBalance);
  }
}