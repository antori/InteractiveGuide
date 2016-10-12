package opendial.modules.utils;

public interface Observable {
	
	public void subscribe(Observer o);
	public void notifyObservers();
	public Object getState();

}
