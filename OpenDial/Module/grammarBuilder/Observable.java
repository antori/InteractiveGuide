package opendial.modules.grammarBuilder;

public interface Observable {
	
	public void subscribe(Observer o);
	public void notifyObservers();
	public Object getState();

}
