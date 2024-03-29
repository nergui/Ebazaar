package business.rulesbeans;

import business.externalinterfaces.DynamicBean;
import business.externalinterfaces.Address;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AddressBean implements DynamicBean {
	private Address addr;

	public AddressBean(Address addr) {
		this.addr = addr;
	}
	
	//////////// bean interface for address
	public String getCity() {
        return addr.getCity();
    }
 
    public String getState() {
        return addr.getState();
    }
 
     public String getStreet() {
        return addr.getStreet1();
    }
 
    public String getZip() {
        return addr.getZip();
    }	
	
	///////////property change listener code
    private PropertyChangeSupport pcs = 
    	new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener pcl){
	 	pcs.addPropertyChangeListener(pcl);
	}
	public void removePropertyChangeListener(PropertyChangeListener pcl){	
    	pcs.removePropertyChangeListener(pcl);
    }
}
