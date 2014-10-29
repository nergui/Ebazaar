package business.shoppingcartsubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static business.util.StringParse.*;

import business.*;
import business.customersubsystem.CustomerSubsystemFacade;
import business.exceptions.BackendException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.ShoppingCart;
import business.util.OrderUtil;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

public class DbClassShoppingCart implements DbClass {

    private static final Logger LOG = Logger.getLogger(DbClassShoppingCart.class
            .getPackage().getName());
    private DataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();
    DataAccessSubsystem dataAccess;
    ShoppingCartImpl cartImpl;
    ShoppingCart cart;
    CartItem cartItem;
    List<CartItem> cartItemsList;
    CustomerProfile custProfile;
    Integer cartId;
    String query;
    final String GET_ID = "GetId";
    final String GET_SAVED_ITEMS = "GetSavedItems";
    final String GET_TOP_LEVEL_SAVED_CART = "GetTopLevelSavedCart";
    final String SAVE_CART = "SaveCart";
    final String SAVE_CART_ITEM = "SaveCartItem";
    final String DELETE_CART = "DeleteCart";
    final String DELETE_ALL_CART_ITEMS = "DeleteAllCartItems";
    String queryType;

    public void buildQuery() {
        if (queryType.equals(GET_ID)) {
            buildGetIdQuery();
        } else if (queryType.equals(GET_SAVED_ITEMS)) {
            buildGetSavedItemsQuery();
        } else if (queryType.equals(GET_TOP_LEVEL_SAVED_CART)) {
            this.buildGetTopLevelCartQuery();
        } else if (queryType.equals(SAVE_CART)) {
            buildSaveCartQuery();
        } else if (queryType.equals(SAVE_CART_ITEM)) {
            buildSaveCartItemQuery();
        } else if (queryType.equals(DELETE_CART)) {
            buildDeleteCartQuery();
        } else if (queryType.equals(DELETE_ALL_CART_ITEMS)) {
            buildDeleteAllCartItemsQuery();
        }
    }

    private void buildGetIdQuery() {
        query = "SELECT shopcartid "
                + "FROM ShopCartTbl "
                + "WHERE custid = " + custProfile.getCustId();

    }

    private void buildDeleteCartQuery() {
        query = "DELETE FROM shopcarttbl WHERE shopcartid = " + cartId.intValue();
    }

    private void buildDeleteAllCartItemsQuery() {
        query = "DELETE FROM shopcartitem WHERE shopcartid = " + cartId.intValue();
    }

    //precondition: cart and custprofile has been stored as instance variable
    private void buildSaveCartQuery() {
        //implement
        query = "INSERT INTO Shopcarttbl (shopcartid, custid,shipaddress1, "
                + "shipaddress2, shipcity, shipstate, shipzipcode, billaddress1, "
                + "billaddress2, billcity, billstate, billzipcode, nameoncard, "
                + "expdate,cardtype, cardnum, totalpriceamount, totalshipmentcost, "
                + "totaltaxamount, totalamountcharged) VALUES (NULL, "+ custProfile.getCustId() + ", '" + cart.getShippingAddress().getStreet1() + "', '"
                + cart.getShippingAddress().getStreet2() + "', '" + cart.getShippingAddress().getCity() + "', '" + cart.getShippingAddress().getState() + "', '"
                + cart.getShippingAddress().getZip() + "', '" + cart.getBillingAddress().getStreet1() + "', '"
                + cart.getBillingAddress().getStreet2() + "', '" + cart.getBillingAddress().getCity() + "', '" + cart.getBillingAddress().getState() + "', '"
                + cart.getBillingAddress().getZip() + "', '" + cart.getPaymentInfo().getNameOnCard() + "', '" + cart.getPaymentInfo().getExpirationDate() + "', '"
                + cart.getPaymentInfo().getCardType() + "', '" + cart.getPaymentInfo().getCardNum() + "', '" + (new Double(cart.getTotalPrice())).toString() + "',"
                + "'0.00', '0.00', '" + (new Double(cart.getTotalPrice())).toString() + "')";
    }

    private void buildSaveCartItemQuery() {
        query = "INSERT INTO shopcartitem (cartitemid, shopcartid, productid, quantity, totalprice, shipmentcost, taxamount) "
                + "VALUES (NULL, " + cartItem.getCartid() + ", " + cartItem.getProductid() + ", '" + cartItem.getQuantity() + "', '"
                + cartItem.getTotalprice() + "', '0','0')";

    }

    private void buildGetSavedItemsQuery() {
        query = "SELECT * FROM accountsdb.shopcartitem WHERE shopcartid = " + cartId;

    }

    private void buildGetTopLevelCartQuery() {
        query = "SELECT * FROM shopcarttbl WHERE shopcartid = " + cartId;

    }

    //Support method for saveCart -- part of another transaction
    private void deleteCart(Integer cartId) throws DatabaseException {
        this.cartId = cartId;
        queryType = DELETE_CART;
        dataAccessSS.delete();

    }

    //Support method for saveCart -- part of another transaction
    private void deleteAllCartItems(Integer cartId) throws DatabaseException {
        this.cartId = cartId;
        queryType = DELETE_ALL_CART_ITEMS;
        dataAccessSS.delete();
    }

    ShoppingCartImpl retrieveSavedCart(CustomerProfile custProfile) throws DatabaseException {
        this.custProfile = custProfile;
        dataAccessSS.createConnection(this);
        dataAccessSS.startTransaction();
        try {
            Integer cid = getShoppingCartId(custProfile);

            ShoppingCartImpl cart = getTopLevelSavedCart(cid);

            //Last, get cart items associated with this cart id, and insert into cart
            List<CartItem> items = getSavedCartItems(cid);
            cart.setCartItems(items);

            dataAccessSS.commit();
            return cart;

        } catch (DatabaseException e) {
            dataAccessSS.rollback();
            LOG.warning("Rolling back...");
            throw (e);
        } finally {
            dataAccessSS.releaseConnection();
        }
    }
    //support method for retrieveSavedCart

  Integer getShoppingCartId1(CustomerProfile custProfile) throws DatabaseException {
        this.custProfile = custProfile;
        queryType = GET_ID;
        dataAccessSS.atomicRead(this);
        return cartId;
    }
    Integer getShoppingCartId(CustomerProfile custProfile) throws DatabaseException {
        this.custProfile = custProfile;
        queryType = GET_ID;
        dataAccessSS.atomicRead(this);
        return cartId;
    }

    //support method for retrieveSavedCart
   List<CartItem> getSavedCartItems(Integer cartId) throws DatabaseException {
        this.cartId = cartId;
        queryType = GET_SAVED_ITEMS;
        dataAccessSS.atomicRead(this);
        return cartItemsList;

    }

    //support method for retrieveSavedCart
    private ShoppingCartImpl getTopLevelSavedCart(Integer cartId) throws DatabaseException {
        this.cartId = cartId;
        queryType = GET_TOP_LEVEL_SAVED_CART;
        dataAccessSS.read();
        return cartImpl;
    }

     private int saveCartTopLevel() throws DatabaseException {

        queryType = SAVE_CART;
        //testing
        buildQuery();
        LOG.info(query);
        //end testing
        return dataAccessSS.save();
    }
    
    private void saveCartItem(CartItem item) throws DatabaseException {
        this.cartItem = item;
        queryType = SAVE_CART_ITEM;
        //testing
        buildQuery();
        LOG.info(query);
        dataAccessSS.save();
    }
     
    public void saveCart(CustomerProfile custProfile, ShoppingCart cart) throws DatabaseException {
        Integer cartId = null;
        this.cart = cart;
        this.custProfile = custProfile;
        List<CartItem> cartItems = cart.getCartItems();

        dataAccessSS.createConnection(this);
        dataAccessSS.startTransaction();
        try {

            Integer oldCartId = getShoppingCartId(custProfile);

            //delete old cart
            if (oldCartId != null) {
                deleteCart(oldCartId);
                deleteAllCartItems(oldCartId);
            }

            cartId = saveCartTopLevel();

            for (CartItem item : cartItems) {
                item.setCartId(cartId);
                saveCartItem(item);
            }
            dataAccessSS.commit();

        } catch (DatabaseException e) {
            dataAccessSS.rollback();
            LOG.warning("Rolling back...");
            throw (e);
        } finally {
            dataAccessSS.releaseConnection();
        }
    }

    

    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        if (queryType.equals(GET_ID)) {
            populateShopCartId(resultSet);
        } else if (queryType.equals(GET_SAVED_ITEMS)) {
            populateCartItemsList(resultSet);
        } else if (queryType.equals(GET_TOP_LEVEL_SAVED_CART)) {       
                populateTopLevelCart(resultSet);
        }

    }

    private void populateShopCartId(ResultSet rs) {
        try {
            if (rs.next()) {
                cartId = rs.getInt("shopcartid");
            }
        } catch (SQLException e) {
            //do nothing
        }
    }

    private void populateTopLevelCart(ResultSet rs) throws DatabaseException {
        cartImpl = new ShoppingCartImpl();
        Address shippingAddress = null;
        Address billingAddress = null;
        CreditCard creditCard = null;
        try {
            if (rs.next()) {
                //load shipping address
                String shipStreet = rs.getString("shipaddress1");
                String shipCity = rs.getString("shipcity");
                String shipState = rs.getString("shipstate");
                String shipZip = rs.getString("shipzipcode");
                shippingAddress = 
                    CustomerSubsystemFacade.createAddress(shipStreet, shipCity, shipState, shipZip);

                //load billing address
                String billStreet = rs.getString("shipaddress1");
                String billCity = rs.getString("shipcity");
                String billState = rs.getString("shipstate");
                String billpZip = rs.getString("shipzipcode");
                billingAddress = 
                    CustomerSubsystemFacade.createAddress(billStreet, billCity, billState, billpZip);

                //load credit card: createCreditCard(String name, String num, String type, expDate)
                String name = rs.getString("nameoncard");
                String num = rs.getString("cardnum");
                String type = rs.getString("cardtype");
                String exp = rs.getString("expdate");
                creditCard = 
                    CustomerSubsystemFacade.createCreditCard(name, num, type, exp);

                //load cart
                cartImpl.setCartId((new Integer(rs.getInt("shopcartid")).toString()));
                cartImpl.setShipAddress(shippingAddress);
                cartImpl.setBillAddress(billingAddress);
                cartImpl.setPaymentInfo(creditCard);


            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

    }

    private void populateCartItemsList(ResultSet rs) throws DatabaseException {
        //CartItem cartItem = null;
        cartItemsList = new LinkedList<CartItem>();
        try {
            while (rs.next()) {
                cartItem = new CartItemImpl(rs.getInt("shopcartid"),
                        rs.getInt("productid"),
                        rs.getInt("cartitemid"),
                        makeString(rs.getInt("quantity")),
                        makeString(rs.getDouble("totalprice")),
                        true);

                cartItemsList.add(cartItem);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } catch(BackendException ex) {
            throw new DatabaseException(ex);
        }
    }

    public String getDbUrl() {
        DbConfigProperties props = new DbConfigProperties();
        return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
    }

    public String getQuery() {
        return query;
    }

    public static void main(String[] args) {
        /*
        DbClassShoppingCart dbclass = new DbClassShoppingCart();
        CustomerSubsystemFacade c = new CustomerSubsystemFacade();
        CustomerProfile profile = c.createCustomerProfile(1);

        dbclass.buildSaveCartQuery();
        String saveQuery = dbclass.getQuery();
        dbclass.buildSaveCartItemQuery();
        String saveCartItemQuery = dbclass.getQuery();
        System.out.println("Save cart query: \n");
        System.out.println(saveQuery);
        System.out.println("\nSave cart item query: \n");
        System.out.println(saveCartItemQuery);*/
    }
}
