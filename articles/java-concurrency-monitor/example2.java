public class Account {
	private int balance;
	private Object lock = new Object();

	public Account(int balance) {
		this.balance = balance;
	}

	public boolean withdraw(int amount){
		synchronized (lock) {
			if(balance<amount)
				return false;
			balance -= amount;
			return true;
		}
	}

	public void deposit(int amount){
		synchronized (lock) {
			balance +=amount;
		}
	}
}