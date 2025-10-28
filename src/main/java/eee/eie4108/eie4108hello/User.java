package eee.eie4108.eie4108hello;

public class User {
  public String name;
  public String email;
  public int balance;
  
  public User() {}
  
  public User(String name, String email, int balance) {
    this.name = name;
    this.email = email;
    this.balance = balance;
  }
  
  public String toString() {
    return String.format("name: %s, email: %s, balance: %d", name, email, balance);
  }
}
