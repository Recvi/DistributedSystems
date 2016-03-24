package gr.aueb.cs.ds.worker;

public interface Worker {

	public void initialize();
	
	void waitForTasksThread();
	
	public Object onNewTask(Object argumentTypesToBeDecided);

}