public class Account {
	private int balance;

	public Account(int balance) {
		this.balance = balance;
	}

	synchronized public boolean withdraw(int amount){
		if(balance<amount)
			return false;
		balance -= amount;
		return true;
	}

	synchronized public void deposit(int amount){
		balance +=amount;
	}
}