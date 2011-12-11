import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NoRedPimple extends Thread {
	private AtomicBoolean done = new AtomicBoolean(false);
	private AtomicInteger value = new AtomicInteger(0);

	@Override
	public void run() {
		while (!done.get()) {//A
			Thread.yield();
		}
		System.out.println(value.get()); //D
	}

	public void done() {
		done.set(true);
	}

	public void setValue(int value) {
		this.value.set(value);
	}

	public static void main(String[] args) {
		NoRedPimple r = new NoRedPimple();
		r.start();
		r.setValue(1); //B
		r.done(); //C
	}
}
