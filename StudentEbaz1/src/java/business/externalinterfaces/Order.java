
package business.externalinterfaces;

import java.util.List;


public interface Order {
    public List<OrderItem> getOrderItems();    
    public String getOrderDate();		
    public Integer getOrderId();		
    public String getTotalPrice();
    public Address getShipAddress();
    public Address getBillAddress();
    public CreditCard getPaymentInfo();
    
 
	
}



