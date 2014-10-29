
package business.ordersubsystem;

import business.exceptions.BackendException;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.OrderSubsystem;
import business.externalinterfaces.ShoppingCart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import middleware.exceptions.DatabaseException;


public class OrderSubsystemFacade implements OrderSubsystem {
	private static final Logger LOG = 
		Logger.getLogger(OrderSubsystemFacade.class.getPackage().getName());
    CustomerSubsystem cust;
    
    public OrderSubsystemFacade(CustomerSubsystem cust){
        this.cust = cust;
    }
   
    /** Called during login process when customer data is loaded into memory */
    public List<Order> getOrderHistory() throws BackendException { 
        //implement by ginna
        //stubs are given here
        /*OrderImpl orderData = new OrderImpl(1,"11/11/2011","50.00");
        OrderItemImpl item = new OrderItemImpl(1, 1, "3", "50.00");
	List<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(item);
        List<Order> orders = new ArrayList<Order>();
        orders.add(orderData);
        return Collections.unmodifiableList(orders);
       */
        
        //get all orders id (step 2 sequence diagrama)
          DbClassOrder dbclass = new DbClassOrder(this.cust.getCustomerProfile());
          List<Integer> allOrderIds = null;
            try {
                allOrderIds = dbclass.getAllOrderIds(cust.getCustomerProfile());
            } catch (DatabaseException ex) {
                Logger.getLogger(OrderSubsystemFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
            
         // get all order items for 1 order   (step 6 sequence diagrama)
         
            for(Integer orderId : allOrderIds){
            try {
                DbClassOrder dbclassOrderId = new DbClassOrder();
                List<OrderItem> orderItems = dbclassOrderId.getOrderItems(orderId);
            } catch (DatabaseException ex) {
                Logger.getLogger(OrderSubsystemFacade.class.getName()).log(Level.SEVERE, null, ex);
            } 
            }
            
            // get order Data  (step 8 sequence diagrama)
             
            List<Order> orders = new ArrayList<Order>();
            for(Integer orderId : allOrderIds){
            try {
                DbClassOrder dbclassOrderId = new DbClassOrder();
                OrderImpl or= dbclassOrderId.getOrderData(orderId);
                orders.add(or);
            } catch (DatabaseException ex) {
                Logger.getLogger(OrderSubsystemFacade.class.getName()).log(Level.SEVERE, null, ex);
            } 
            }
        return Collections.unmodifiableList(orders);
        
    }
	public OrderItem createOrderItem(Integer prodId,Integer orderId, String quantityReq, String totalPrice){
        return new OrderItemImpl(prodId,orderId,quantityReq,totalPrice);
    }
	
	public Order createOrder(Integer orderId, String orderDate, String totalPrice) {
		return new OrderImpl(orderId, orderDate, totalPrice);
	}
        
        //implement by ginna
    public void submitOrder(ShoppingCart shopCart) throws BackendException {
        //implement
        try{
        DbClassOrder dbclass = new DbClassOrder(this.cust.getCustomerProfile());
        dbclass.submitOrder(shopCart);
        }
        catch(DatabaseException e) {
            throw new BackendException(e);
        }
        
    	
    }
     
    ///////////// Methods internal to the Order Subsystem
    List<Integer> getAllOrderIds() throws DatabaseException {     
        DbClassOrder dbClass = new DbClassOrder();
        return dbClass.getAllOrderIds(cust.getCustomerProfile());
        
    }
    public List<OrderItem> getOrderItems(Integer orderId) throws BackendException {
        try {
            DbClassOrder dbClass = new DbClassOrder();
            return dbClass.getOrderItems(orderId);
        } catch(DatabaseException e) {
            throw new BackendException(e);
        }
    }
    
    OrderImpl getOrderData(Integer orderId) throws DatabaseException {
    	DbClassOrder dbClass = new DbClassOrder();
    	return dbClass.getOrderData(orderId);
    }
    
    
 

        
 
}
