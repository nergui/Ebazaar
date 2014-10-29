/*
 * This is a Presentation Control Bean for the browse and select use case.  It
 * is intended to hold JSF action methods for the Browse and 
 * Select use case.
 * 
 */
package presentation.data;

import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.exceptions.UnauthorizedException;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductSubsystem;
import business.externalinterfaces.RulesConfigProperties;
import business.externalinterfaces.ShoppingCartSubsystem;
import business.productsubsystem.ProductImpl;
import business.productsubsystem.ProductSubsystemFacade;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;
import com.sun.xml.bind.util.ProxyListImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import middleware.exceptions.DatabaseException;
import presentation.control.Authorization;
import presentation.control.Callback;
import usecasecontrol.BrowseSelectController;

@Named("bsPCB")
@SessionScoped
public class BrowseSelectPCB implements Serializable {

    BrowseSelectPCB() {
    }
    private String newPageName = null;//for navigation 
    private String catalog;
    private String productName;
    private String quantityRequested;
    private String total;
    private int numberOfItems;
    @Inject
    private SessionData sessionContext;

    public SessionData getSessionContext() {
        return sessionContext;
    }

    public int getNumberOfItems() {
        numberOfItems = cartItems.size();
        return numberOfItems;
    }
    
    // = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    // ProductSubsystem stubProdSS = new MPTestProductSubsystemFacade();
    // private String greeting;
    // private String totalShoppingCost;
    private Product product;
    private Product productNamer;

    public Product getProductNamer() {
        return productNamer;
    }
    private List<CartItem> cartItems;
    private boolean editableItem;
    //  private String itemUnitPrice;
    private BrowseSelectController bsController = new BrowseSelectController();
    //Items selected by user for deletion
    private HashMap<String, Boolean> checked = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> checkedForEdit = new HashMap<String, Boolean>();

    public HashMap<String, Boolean> getChecked() {
        return checked;
    }

    public void setChecked(HashMap<String, Boolean> checked) {
        this.checked = checked;
    }

    public boolean isEditableItem() {
        return editableItem;
    }

    public void setEditableItem(boolean editableItem) {
        this.editableItem = editableItem;
    }
    
    public HashMap<String, Boolean> getCheckedForEdit() {
        return checkedForEdit;
    }

    public void setCheckedForEdit(HashMap<String, Boolean> checkedForEdit) {
        this.checkedForEdit = checkedForEdit;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(String q) {
        this.quantityRequested = q;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public SelectItem[] getCatalogs() {
        SelectItem[] catalogs = null;
        try {
            catalogs = new SelectItem[bsController.getCatalogNames().size()];
            List<String> cats = bsController.getCatalogNames();
            int count = 0;
            for (String cat : cats) {
                catalogs[count++] = new SelectItem(cat);
            }

        } catch (DatabaseException e) {
            //handle exception
        }
        return catalogs;
    }

    public SelectItem[] getProducts() throws DatabaseException {
        //read real data 
        // SelectItem[] products = new SelectItem[2];
        //products[0] = new SelectItem("Pants");
        // products[1] = new SelectItem("Shirts");
        SelectItem[] products2 = null;
        try {
            String name = catalog;
            products2 = new SelectItem[bsController.getProducts(catalog).size()];
            List<Product> cats = bsController.getProducts(catalog);
            int count = 0;

            for (Product cat : cats) {
                products2[count] = new SelectItem(cat.getProductName());
                //  products2[count] = new SelectItem(cat.getQuantityAvail());
                //  products2[count] = new SelectItem(cat.getProductId());
                count++;
            }

        } catch (DatabaseException e) {
            //handle exception
        }
        return products2;
    }

    public String addToCart() {

        return "quantity";
    }

    public String addQuantity() {

        int quant = Integer.parseInt(quantityRequested);
        try {
            ShoppingCartSubsystem ss = obtainCurrentShoppingCartSubsystem();
            //ShoppingCartSubsystem ss = obtainCurrentShoppingCartSubsystem();
            //put the item inside a shopping cart
            bsController.addCartItem(ss, productName, quant);
            sessionContext.setExternalShopCartSS(ss);//for not logged in
            sessionContext.setShopCartSS(ss);
        } catch (DatabaseException ex) {
            //handle exception
        }

        return "cartitem";
    }

    public void retrieveSavedCart(Requirement r) {
        //First authorize
        try {
            Authorization.checkAuthorization(r, sessionContext.custIsAdmin());
        } catch (UnauthorizedException e) {
            MessagesUtil.displayError(e.getMessage());
            return;
        }
         try {
            //sessionContext.getShopCartSS().setCustomerProfile();
            bsController.saveCart(sessionContext.getCust());
            //sessionContext.getShopCartSS().clearLiveCart();
        } catch (BackendException e) {
            MessagesUtil.displayError(MessagesUtil.DATABASE_ERROR);
        }
        //implement - set value into cartItems variable
    }

    public void saveCart(Requirement r) {
        //Authorize first
        try {
            Authorization.checkAuthorization(r, sessionContext.custIsAdmin());
            bsController.saveCart(sessionContext.getCust());
        } catch (UnauthorizedException e) {
            MessagesUtil.displayError(e.getMessage());
            return;
        }
      catch(BackendException e){
          
      }  
        

    }

    private ShoppingCartSubsystem obtainCurrentShoppingCartSubsystem() {
        //Implement: get the Shopping Cart Subsystem from cached CustomerSubsystem
       if(sessionContext.getIsLoggedIn()){
            return bsController.obtainCurrentShoppingCartSubsystem(sessionContext.getCust(), 
                sessionContext.getShopCartSS());
        }
        else{
            return bsController.obtainCurrentShoppingCartSubsystem(sessionContext.getCust(), 
                sessionContext.getExternalShopCartSS());
        }
    }
    public List<CartItem> getCartItems() {
       cartItems = bsController.getCartItems(obtainCurrentShoppingCartSubsystem());
        //stub
//      List<CartItem> l = new ArrayList<CartItem>();
//        ShoppingCartSubsystemFacade f = new ShoppingCartSubsystemFacade();
//        try {
//          // l.add(f.createCartItem("Stub Book",
//                   // "2",
//                    //"30.00"));
//            l.add(f.createCartItem(bsController.getProduct(productName).getProductName(), quantityRequested, bsController.getProduct(productName).getUnitPrice()));
//        } catch (DatabaseException e) {
//            System.out.println("WARNING: unable to populate cart with stub data");
//            //don't care about this -- just a stub

//        } finally {
            return cartItems;
        }
        
    

    // @param cartItems the cartItems to set
    public void setCartItems(List<CartItem> Items) {
        this.cartItems = Items;
    }

    public Product getProduct() throws DatabaseException{
        //implement
        //Product p = new ProductImpl("stub", "11/11/11", "10", "10.00");
        //return p;
      Product product1=bsController.getProduct(productName);
    
    
    return product1;
    }

    
    public CartItem[] changeListtoArray() {

        CartItem[] itemArray = new CartItem[cartItems.size()];

        for (int i = 0; i < itemArray.length; i++) {
            CartItem item = cartItems.get(i);
            itemArray[i] = item;
        }
        return itemArray;
    }

    public void deleteSelectedItems() {
        
          for(CartItem cartItem: cartItems){
              if(checked.get(cartItem.getProductName())){
                 cartItems.remove(cartItem);
                }
          }
          checked.clear();
    }

    //support function
    private boolean noItemsSelectedForDelete() {
        if (cartItems == null || cartItems.isEmpty()) {
            return true;
        }
        int count = 0;
        for (CartItem item : cartItems) {
            Boolean checkedValue = checked.get(item.getProductName());
            if (checkedValue != null && checkedValue.equals(Boolean.TRUE)) {
                return false;
            }
        }
        return true;
    }

    public void makeItemEditable(CartItem itemToEdit) {
        for (CartItem items : cartItems) {
            if (items.getProductName().equals(itemToEdit.getProductName())) {
                // this.editableItem=true;
                checkedForEdit.remove(itemToEdit.getProductName());
                checkedForEdit.put(itemToEdit.getProductName(), true);
            }
        }
    }

    public void saveEditedItem(CartItem itemTobesaved, String quantity) {
        checkedForEdit.clear();
        for (CartItem items : cartItems) {
            //set the new quantity
            if (items.getProductName().equals(itemTobesaved.getProductName())) {
                items.setQuantity(quantity);
            }
            //make the items not editable
            checkedForEdit.put(items.getProductName(), false);
        }
        //iterate through the editablecartItem hashMap and make them uneditable
        checkedForEdit.clear();

    }

    //RULES
    public void validateQuantity(FacesContext context, UIComponent toValidate, Object val) {
        String desired = (String) val;
        try {
            bsController.runRulesOnQuantity(productName, desired);
        } catch (RuleException e) {
            ((UIInput) toValidate).setValid(false);
            String msg = e.getMessage();
            FacesMessage retval = new FacesMessage(msg);
            context.addMessage(toValidate.getClientId(context), retval);
        } catch (BusinessException e) {
            ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
            handler.performNavigation("errorDb");
        } catch (DatabaseException e) {
            ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
            handler.performNavigation("errorDb");
        }
    }
    
    public SaveCartCallback getSaveCartCallback() {
        return new SaveCartCallback();
    }

    public class SaveCartCallback implements Callback {

        public final Requirement REQUIREMENT = Requirement.SAVE_CART;

        public String doUpdate() {
            saveCart(REQUIREMENT);
            MessagesUtil.displaySuccess("Cart successfully saved.");
            return null;
        }
    }
    
    public RetrieveCartCallback getRetrieveCartCallback() {
        return new RetrieveCartCallback();
    }

    public class RetrieveCartCallback implements Callback {

        public final Requirement REQUIREMENT = Requirement.RETRIEVE_SAVED_CART;

        public String doUpdate() {
            retrieveSavedCart(REQUIREMENT);

            //No special page to return
            return null;
        }
    }
}
/**
 * @return the cartItemReqCapture
 */
