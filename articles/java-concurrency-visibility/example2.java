public class NoRedPimple extends Thread {
	private boolean done;
	private int value;

	@Override
	public void run() {
		boolean tmp = false;
		while (!tmp) {
			synchronized (this) {
				tmp = done; //A
			}
            Thread.yield();
		}
        synchronized (this) {
            System.out.println(value);  //D
        }
	}

	synchronized public void done() {
		done = true;
	}

	synchronized public void setValue(int value) {
		this.value = value;
	}

	public static void main(String[] args) {
		NoRedPimple r = new NoRedPimple();
		r.start();
		r.setValue(1); //B
		r.done(); //C
	}
}