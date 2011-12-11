public class Account {
	public static final int BOUND = 10000;
	private int balance;

	public Account(int balance) {
		this.balance = balance;
	}

	synchronized public boolean withdraw(int amount) throws InterruptedException{
        while(balance<amount)
            wait();// no money, wait
        balance -= amount;
        notifyAll();// not full, notify
        return true;
	}

	synchronized public void deposit(int amount) throws InterruptedException{
        while(balance+amount >BOUND)
            wait();//full, wait
        balance +=amount;
        notifyAll();// has money, notify
	}
}