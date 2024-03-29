package business.rulesbeans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import middleware.exceptions.DatabaseException;
import business.externalinterfaces.DynamicBean;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.ShoppingCart;
import business.util.Pair;
import business.util.ShoppingCartUtil;

public class FinalOrderBean implements DynamicBean {
	//private static Logger log = Logger.getLogger(null);
    
	private ShoppingCart shopCart;
	public FinalOrderBean(ShoppingCart sc){		
		shopCart = sc;
	}
	
	
	///////bean interface for shopping cart
	
	public Address getShippingAddress() {
		return shopCart.getShippingAddress();
	}
    public Address getBillingAddress(){
		return shopCart.getBillingAddress();
	}
    public CreditCard getPaymentInfo(){
		return shopCart.getPaymentInfo();
	}
    public List<CartItem> getCartItems(){
		return shopCart.getCartItems();
	}
    /** 
     * This is a collection of pairs indicating
     * the quantity requested vs quantity avail
     * for each line item in the shopping cart
     * 
     * If return value is empty, this indicates an error condition
     * @return
     */
    
    public List<Pair> getRequestedAvailableList(){
    	List<Pair> retVal = new ArrayList<Pair>();
    
    	try {
    		retVal = ShoppingCartUtil.computeRequestedAvailableList(shopCart);
    	}
    	catch(DatabaseException ex) {
    		//log.warning("Unable to read database for handling requested/avail rule");
    	}
    	return retVal;
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
