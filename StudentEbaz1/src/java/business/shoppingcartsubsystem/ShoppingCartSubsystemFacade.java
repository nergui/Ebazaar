package business.shoppingcartsubsystem;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import middleware.exceptions.DatabaseException;
import business.customersubsystem.RulesPayment;
import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.Rules;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;
import java.util.ArrayList;
import java.util.logging.Level;

public class ShoppingCartSubsystemFacade implements ShoppingCartSubsystem {

    ShoppingCartImpl liveCart;
    ShoppingCartImpl savedCart;
    Integer shopCartId;
    CustomerProfile customerProfile;
    Logger log = Logger.getLogger(this.getClass().getPackage().getName());

    //interface methods
    public void setCustomerProfile(CustomerProfile customerProfile) {
        this.customerProfile = customerProfile;
    }

    //used in CustomerSubsystemFacade.initializeCustomer
    /*    Integer val = getShoppingCartId();
        if(val != null){
            shopCartId = val;
            log.info("cart id: "+shopCartId);
            List<CartItem> items = getCartItems(shopCartId);
            log.info("list of items: "+items);
            savedCart = new ShoppingCartImpl(items);
        }
        else {
        	savedCart = new ShoppingCartImpl();
        }

   /* Integer getShoppingCartId() throws DatabaseException {
        DbClassShoppingCart dbClass = new DbClassShoppingCart();
        return dbClass.getShoppingCartId(customerProfile);
    }
    
    List<CartItem> getCartItems(Integer shopCartId) throws DatabaseException {
        DbClassShoppingCart dbClass = new DbClassShoppingCart();
        return dbClass.getSavedCartItems(shopCartId);
    }
*/
    public ShoppingCartSubsystemFacade() {
    }

    public void addCartItem(String itemName, String quantity, String totalPrice, Integer pos) throws DatabaseException {
        //if a saved cart has been retrieved, it will be the live cart, unless
        //user has already added items to a new cart
        if (liveCart == null) {
            liveCart = new ShoppingCartImpl(new LinkedList<CartItem>());
        }
        CartItemImpl item = new CartItemImpl(itemName, quantity, totalPrice);
        if (pos == null) {
            liveCart.addItem(item);
        } else {
            liveCart.insertItem(pos, item);
        }
    }
    
    //for stubbing
    public CartItem createCartItem(String productName, 
                    String quantity,
                    String totalprice) throws DatabaseException {
        return new CartItemImpl(productName, quantity, totalprice);
    }

    public boolean deleteCartItem(int pos) {
        return liveCart.deleteCartItem(pos);
    }

    public void clearLiveCart() {
        liveCart.clearCart();
    }

    public boolean deleteCartItem(String itemName) {
        return liveCart.deleteCartItem(itemName);
    }

    public List<CartItem> getLiveCartItems() {
        if (liveCart == null || liveCart.getCartItems() == null) {
            return new LinkedList<CartItem>();
        } else {
            return liveCart.getCartItems();
        }

    }

    public void setShippingAddress(Address addr) {
        //liveCart should be non-null
        if(liveCart != null)
            liveCart.setShipAddress(addr);
    }

    public void setBillingAddress(Address addr) {
        if(liveCart != null)
            liveCart.setBillAddress(addr);
    }

    public void setPaymentInfo(CreditCard cc) {
        if(liveCart != null) 
            liveCart.setPaymentInfo(cc);

    }

    public ShoppingCart getLiveCart() {
        return liveCart;
    }
    
    public ShoppingCart getSavedCart() {
        return savedCart;
    }

    public void setLiveCart(ShoppingCart cart) {
        List<CartItem> cartItems = (cart == null) ? new ArrayList<CartItem>() :
                cart.getCartItems();
        liveCart = new ShoppingCartImpl(cartItems);
    }

    public void makeSavedCartLive() {
        liveCart = savedCart;
    }

    public void saveLiveCart() throws BackendException {
		try
                {
                    DbClassShoppingCart dbClass=new DbClassShoppingCart();
                    dbClass.saveCart(customerProfile, liveCart);
                } 
                catch(DatabaseException e)
                {
                    throw new BackendException(e);
                }
        
    

	}

    @Override
    public void runShoppingCartRules() throws RuleException, BusinessException {
        //implement
        if(liveCart == null) liveCart=new ShoppingCartImpl();
		Rules transferObject = new RulesShoppingCart(liveCart);
		transferObject.runRules();
       
    }

    @Override
    public void runFinalOrderRules() throws RuleException, BusinessException {
        Rules transferObject = new RulesFinalOrder(liveCart);
        transferObject.runRules();
    }

      public void retrieveSavedCart() throws DatabaseException {
        
        getShoppingCartId();
        if(shopCartId != null){
           
            log.info("cart id: "+shopCartId);
            List<CartItem> items = getCartItems(shopCartId);
            log.info("list of items: "+items);
            savedCart = new ShoppingCartImpl(items);
        }
        else {
        savedCart = new ShoppingCartImpl();
        }

    }
     private void getShoppingCartId() {
        
        DbClassShoppingCart dbClass=new DbClassShoppingCart();
       
        try {
            shopCartId = dbClass.getShoppingCartId(customerProfile);
        } catch (DatabaseException ex) {
            Logger.getLogger(ShoppingCartSubsystemFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
      private List<CartItem> getCartItems(Integer shopCartId) {
       DbClassShoppingCart dbclass=new DbClassShoppingCart();
     List<CartItem> items=null;
        
        try { 
            items=dbclass.getSavedCartItems(shopCartId);
        } catch (DatabaseException ex) {
            Logger.getLogger(ShoppingCartSubsystemFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return items;
    }
    }

