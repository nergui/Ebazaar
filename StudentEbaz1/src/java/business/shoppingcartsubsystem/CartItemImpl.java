
package business.shoppingcartsubsystem;

import business.exceptions.BackendException;
import java.util.logging.Logger;

import middleware.exceptions.DatabaseException;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductSubsystem;
import business.productsubsystem.ProductSubsystemFacade;
import java.util.logging.Level;


public class CartItemImpl implements CartItem {
    Integer cartid;
    Integer productid;
    Integer lineitemid;
    String quantity;
    String totalprice;
    String itemUnitPrice;

   
	String productName;
	Logger log = Logger.getLogger(this.getClass().getPackage().getName());
//	this is true if this cart item is data that has come from
    //database
    boolean alreadySaved;
  
    /** This version of constructor used when reading data from screen */
    public CartItemImpl(String productName, 
                    String quantity,
                    String totalprice) throws DatabaseException{
        this.productName = productName;
        this.quantity = quantity;
        this.totalprice =totalprice;
        alreadySaved = false;
        ProductSubsystem prodSS= new ProductSubsystemFacade();
        productid = prodSS.getProductIdFromName(productName);
    }

    
    
    /** This version of constructor used when reading from database */
    public CartItemImpl(Integer cartid, 
                    Integer productid, 
                    Integer lineitemid, 
                    String quantity, 
                    String totalprice,
                    boolean alreadySaved) throws BackendException {
        this.cartid = cartid;
        this.productid= productid;
        this.lineitemid = lineitemid;
        this.quantity = quantity;
        this.totalprice =totalprice;
        this.alreadySaved = alreadySaved;
        ProductSubsystem prodSS= new ProductSubsystemFacade();
        productName = prodSS.getProductFromId(productid).getProductName();
    }

    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append("cartid = <"+cartid+">,");
        buf.append("productid = <"+productid+">,");
        buf.append("lineitemid = <"+lineitemid+">,");
        buf.append("quantity = <"+quantity+">,");
        buf.append("totalprice = <"+totalprice+">");
        buf.append("alreadySaved = <"+alreadySaved+">");
        return buf.toString();
    }
	public boolean isAlreadySaved() {
		return alreadySaved;
	}
	public Integer getCartid() {
		return cartid;
	}
	public Integer getLineitemid() {
		return lineitemid;
	}
	public Integer getProductid() {
		return productid;
	}
	public String getProductName() {
		return productName;
	}
	public String getQuantity() {
		return quantity;
	}
        //NEW added b/c we need to set quantity
        public void setQuantity(String quantity) {
		this.quantity=quantity;
	}
	public String getTotalprice() {
		return totalprice;
	}
        public void setCartId(int id) {
		this.cartid = id;
	}
      //new added jan 16
         public String getItemUnitPrice() {
             ProductSubsystem prodSS= new ProductSubsystemFacade();
                    try{
                        Product prod=prodSS.getProduct(productName);
                        itemUnitPrice=prod.getUnitPrice();
                    }catch (DatabaseException ex) {
                        Logger.getLogger(CartItem.class.getName()).log(Level.SEVERE, null, ex.getMessage());
                    }
		return itemUnitPrice;
	}
        //New recalculate total price when quantity is changed 
          public String computeTotalPrice() {
            double up = Double.parseDouble(itemUnitPrice);
            int q = Integer.parseInt(quantity);
            double tp = up*q;
            totalprice=(new Double(tp)).toString();
            return totalprice;
        }
        
}
