
package business.ordersubsystem;

import java.util.List;

import business.externalinterfaces.Address;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;


class OrderImpl implements Order{
    private Integer orderId;
    private String orderDate;
    private String totalPrice;
    private List<OrderItem> orderItems;
	private Address shipAddress;
	private Address billAddress;
	private CreditCard creditCard;    
    
    OrderImpl(Integer orderId,String orderDate,String totalPrice){
        
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
    }
    public void setOrderItems(List<OrderItem> orderItems){
    	this.orderItems = orderItems;
    }
    public List<OrderItem> getOrderItems(){
        return orderItems;
    }
    
    

	public String getOrderDate() {
		return orderDate;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public String getTotalPrice() {
		return totalPrice;
	}


    /**
     * @return Returns the billAddress.
     */
    public Address getBillAddress() {
        return billAddress;
    }
    /**
     * @param billAddress The billAddress to set.
     */
    public void setBillAddress(Address billAddress) {
        this.billAddress = billAddress;
    }
 
    /**
     * @param creditCard The creditCard to set.
     */
    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }
    /**
     * @return Returns the shipAddress.
     */
    public Address getShipAddress() {
        return shipAddress;
    }
    /**
     * @param shipAddress The shipAddress to set.
     */
    public void setShipAddress(Address shipAddress) {
        this.shipAddress = shipAddress;
    }

    public CreditCard getPaymentInfo() {
 
        return creditCard;
    }
     public int count() {
         int count=0;
         for(Object o:orderItems){
             count++;
         }
        return count;
    }


}
