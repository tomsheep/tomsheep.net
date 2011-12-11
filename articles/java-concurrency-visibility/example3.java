public class NoRedPimple extends Thread {
	private volatile boolean done;
	private volatile int value;

	@Override
	public void run() {
		while (!done) {//A
			Thread.yield();
		}
		System.out.println(value); //D
	}

	public void done() {
		done = true;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public static void main(String[] args) {
		NoRedPimple r = new NoRedPimple();
		r.start();
		r.setValue(1); //B
		r.done(); //C
	}
}