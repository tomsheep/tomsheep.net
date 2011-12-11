public class RedPimple extends Thread{
	private boolean done;
	private int value;

	@Override
	public void run() {
		while(!done) //A
			Thread.yield();
		System.out.println(value); //D
	}

	public void done() {
		done = true;
	}

	public void setValue(int value){
		this.value = value;
	}

	public static void main(String[] args) {
		RedPimple r = new RedPimple();
		r.start();
		r.setValue(1); //B
		r.done(); //C
	}
}