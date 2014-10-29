
package business.ordersubsystem;

import business.exceptions.BackendException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.OrderSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.util.OrderUtil;
import static business.util.StringParse.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;


class DbClassOrder implements DbClass {
	private static final Logger LOG = 
		Logger.getLogger(DbClassOrder.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
    private String query;
    private String queryType;
    private final String GET_ORDER_ITEMS = "GetOrderItems";
    private final String GET_ORDER_IDS = "GetOrderIds";
    private final String GET_ORDER_DATA = "GetOrderData";
    private final String SUBMIT_ORDER = "SubmitOrder";
    private final String SUBMIT_ORDER_ITEM = "SubmitOrderItem";	
    private CustomerProfile custProfile;
    private Integer orderId;
    private List<Order> allOrders;
    private List<Integer> orderIds;
    private List<OrderItem> orderItems;
    private OrderImpl orderData;
    private OrderItem orderItem;
    private Order order;  
    
    DbClassOrder(){}
    
    DbClassOrder(OrderImpl order){
        this.order = order;
    }
    
    DbClassOrder(OrderItem orderItem ){
        this.orderItem = orderItem;
    }
    
    DbClassOrder(CustomerProfile custProfile) {
    	this.custProfile = custProfile;
    }
    
    DbClassOrder(OrderImpl order, CustomerProfile custProfile){
        this(order);
        this.custProfile = custProfile;
    } 
    
    
    //modified by ginna
    List<Integer> getAllOrderIds(CustomerProfile custProfile) throws DatabaseException {
        queryType = GET_ORDER_IDS;
        this.custProfile=custProfile;
    	dataAccessSS.atomicRead(this);
        return Collections.unmodifiableList(orderIds);
        //return new ArrayList<Integer>();   
    }
     
    //modified by ginna
    OrderImpl getOrderData(Integer orderId) throws DatabaseException {
    	queryType = GET_ORDER_DATA;
        this.orderId=orderId;
    	dataAccessSS.atomicRead(this);
        return orderData;

        //return null;
    }
    
    //modified by ginna
    // Precondition: CustomerProfile has been set by the constructor
    void submitOrder(ShoppingCart shopCart) throws DatabaseException, BackendException {
        //strategy: 
        //1. get shopcart items and convert each to an order item using OrderUtil method
        //2. create a new order, and add the list of order items 
        //3. set address and payment info into the order
        //4. start a transaction
        //5. save order data
        //6. in a loop, save cartitem data one by one
        //7. commit
        List<CartItem> sc = shopCart.getCartItems();
        List<OrderItem> orderItems = new LinkedList<OrderItem>();
        
        Integer OrderNum = null;
        //OrderUtil oUtil = new OrderUtil();
        
        //convert cartitems to order item
        for(CartItem caritem : sc){        
            orderItems.add(OrderUtil.createOrderItemFromCartItem(caritem,OrderNum));
        }
        
        //creating a new order

        OrderImpl ord = new OrderImpl(null,OrderUtil.todaysDateStr(),OrderUtil.computeTotalPrice(orderItems));
        
        //get info from shopping cart
         Address BillingAddress = shopCart.getBillingAddress();
         Address ShippingAddress = shopCart.getShippingAddress();
         CreditCard PaymentInfo = shopCart.getPaymentInfo();
         
         //set info to the order
         ord.setBillAddress(BillingAddress);
         ord.setShipAddress(ShippingAddress);
         ord.setOrderItems(orderItems);
         ord.setCreditCard(PaymentInfo);
         this.order=ord;
         
        //submit order data

        dataAccessSS.createConnection(this);
        dataAccessSS.startTransaction();
        
        //save the order in the data base and obtains the order id
        try{
        OrderNum = submitOrderData();
    
         //submit order items
         for(OrderItem item : orderItems){
            item.setOrderid(OrderNum);
            submitOrderItem(item);
         }          
         dataAccessSS.commit();
         } catch (DatabaseException e) {
            dataAccessSS.rollback();
            LOG.warning("Rolling back...");
            throw (e);
        } finally {
            dataAccessSS.releaseConnection();
        }
        
        //instace of client
        
        
         //custProfile..refreshAfterSubmit();
         
        //clear car
       shopCart.clearCart();
    }
    
       //creating new order, return the order id
    private Integer submitOrderData() throws DatabaseException {
        queryType = SUBMIT_ORDER;
        return dataAccessSS.save();
    }
    
    //creating new order, return the order id
    private void submitOrderItem(OrderItem item) throws DatabaseException {
        this.orderItem = item;
        queryType = SUBMIT_ORDER_ITEM;
        dataAccessSS.save();
    }
   
    public List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException {
        queryType = GET_ORDER_ITEMS;
        this.orderId=orderId;
    	dataAccessSS.atomicRead(this);
        return Collections.unmodifiableList(orderItems);        
    }
    
    public void buildQuery() {
        if(queryType.equals(GET_ORDER_ITEMS)) {
            buildGetOrderItemsQuery();
        }
        else if(queryType.equals(GET_ORDER_IDS)) {
            buildGetOrderIdsQuery();
        }
        else if(queryType.equals(GET_ORDER_DATA)) {
        	buildGetOrderDataQuery();
        }
        else if(queryType.equals(SUBMIT_ORDER)) {
            buildSaveOrderQuery();
        }
        else if(queryType.equals(SUBMIT_ORDER_ITEM)) {
            buildSaveOrderItemQuery();
        }

    }
    
	private void buildSaveOrderQuery(){
        Address shipAddr = order.getShipAddress();
        Address billAddr = order.getBillAddress();
        CreditCard cc = order.getPaymentInfo();
        query = "INSERT into Ord "+
        "(orderid, custid, shipaddress1, shipcity, shipstate, shipzipcode, billaddress1, billcity, billstate,"+
           "billzipcode, nameoncard,  cardnum,cardtype, expdate, orderdate, totalpriceamount)" +
        "VALUES(NULL," + custProfile.getCustId() + ",'"+
                  shipAddr.getStreet1()+"','"+
                  shipAddr.getCity()+"','"+
                  shipAddr.getState()+"','"+
                  shipAddr.getZip()+"','"+
                  billAddr.getStreet1()+"','"+
                  billAddr.getCity()+"','"+
                  billAddr.getState()+"','"+
                  billAddr.getZip()+"','"+
                  cc.getNameOnCard()+"','"+
                  cc.getCardNum()+"','"+
                  cc.getCardType()+"','"+
                  cc.getExpirationDate()+"','"+
                  order.getOrderDate()+"',"+
                  Double.parseDouble(order.getTotalPrice())+")";       
    }
	
    private void buildSaveOrderItemQuery(){
        query = "INSERT into OrderItem "+
        "(orderitemid,productid, quantity,totalprice,orderid)" +
        "VALUES(NULL,"+
                  orderItem.getProductid()+","+   
                  Integer.parseInt(orderItem.getQuantity())+","+
                  Double.parseDouble(orderItem.getTotalPrice())+","+
                  orderItem.getOrderid()+")";
    }

    private void buildGetOrderDataQuery() {
        query = "SELECT orderdate, totalpriceamount FROM Ord WHERE orderid = " + orderId;     
    }
    
    private void buildGetOrderIdsQuery() {
        query = "SELECT orderid FROM Ord WHERE custid = "+custProfile.getCustId();     
    }
    
    
    private void buildGetOrderItemsQuery() {
        query = "SELECT * FROM OrderItem WHERE orderid = "+orderId; 
    }
    
    private void populateOrderItems(ResultSet rs) throws DatabaseException {
        orderItems = new LinkedList<OrderItem>();
        OrderItem item = null;
        try {
            while(rs.next()) {
                item = new OrderItemImpl(rs.getInt("orderitemid"),
                                rs.getInt("productid"),
                                rs.getInt("orderid"),
                                rs.getString("quantity"),
                                rs.getString("totalprice"));
                orderItems.add(item);
            }
        }
        catch(SQLException e){
            throw new DatabaseException(e);
        }
    }
    
    private void populateOrderIds(ResultSet resultSet) throws DatabaseException {
        orderIds = new LinkedList<Integer>();
        try {
            while(resultSet.next()){
                orderIds.add(resultSet.getInt("orderid"));
            }
        }
        catch(SQLException e){
            throw new DatabaseException(e);
        }
    }
    
    private void populateOrderData(ResultSet resultSet) throws DatabaseException {  	
        try {
            if(resultSet.next()){
            	orderData=new OrderImpl(orderId, resultSet.getString("orderdate"),
                		           makeString(resultSet.getDouble("totalpriceamount")));
            }
        }
        catch(SQLException e){
            throw new DatabaseException(e);
        }
    }    
 
    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        if(queryType.equals(GET_ORDER_ITEMS)){
            populateOrderItems(resultSet);
        }
        else if(queryType.equals(GET_ORDER_IDS)){
            populateOrderIds(resultSet);
        }
        else if(queryType.equals(GET_ORDER_DATA)){
        	populateOrderData(resultSet);
        }       
    }
    
    
    public String getDbUrl() {
    	DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
    }
    
    public String getQuery() {
        return query;
    }
     
    public void setOrderId(Integer orderId){
        this.orderId = orderId;       
    }   
}
