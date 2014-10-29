/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package presentation.data;

import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.exceptions.UnauthorizedException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import presentation.control.Authorization;
import presentation.control.Callback;
import usecasecontrol.CheckoutController;

/**
 *
 * @author FGel
 */
@Named(value = "checkoutPCB")
@SessionScoped
public class CheckoutPCB implements Serializable {

    /**
     * Creates a new instance of CheckoutPCB
     */
    public CheckoutPCB() {
    }
    
    private static final Logger LOG = Logger.getLogger(CheckoutPCB.class.getName());
    private String shippingMethod;
    private CustomerProfile custProf;
    private boolean newShippingAddress = false;
    private boolean shippingAddressChanged = false;
    private boolean billingAddSameAsShippingAdd = false;
    private boolean creditCardChanged = false;
    private Address shippingAddress;
    private Address billingAddress;
    private CreditCard card;
    private String termsAndConditions;
    //private List<ShoppingCartLineItem> itemsOrdered = new ArrayList<ShoppingCartLineItem>();
    private CheckoutController usecaseControl = new CheckoutController();
    @Inject
    private SessionData sessionContext;

    public SessionData getSessionContext() {
        return sessionContext;
    }
    private List<Address> addressList = new ArrayList<Address>();
    private HashMap<String, Boolean> checkedAddresses = new HashMap<String, Boolean>();
    //private CustomerSubsystem cust = null;

    public HashMap<String, Boolean> getCheckedAddresses() {
        return checkedAddresses;
    }

    public void setCheckedAddresses(HashMap<String, Boolean> checkedAddresses) {
        this.checkedAddresses = checkedAddresses;
    }

    private void initializeCheckedAddresses(List<Address> list) {
        for (Address add : addressList) {
            checkedAddresses.put(add.toString(), false);
        }
    }

    public String checkout(String pageCalledFrom, Requirement requirement) {
        //Check authorization first
        try {
            Authorization.checkAuthorization(requirement, sessionContext.custIsAdmin());
        } catch (UnauthorizedException e) {
            MessagesUtil.displayError(e.getMessage());
            return pageCalledFrom;
        }

        //Check shoppping cart not empty
        try {
            CustomerSubsystem cust = sessionContext.getCust();
            usecaseControl.runShoppingCartRules(cust.getShoppingCart());
            getShippingAddress();
            getBillingAddress();
            return "checkoutwindow";

        } catch (RuleException e) {
            LOG.warning("A RuleException was thrown: " + e.getMessage());
            MessagesUtil.displayError(e.getMessage());
            return pageCalledFrom;
        } catch (BusinessException e) {
            MessagesUtil.displayError(MessagesUtil.GENERAL_ERR_MSG);
            return pageCalledFrom;
        }

    }

    public List<Address> getAddressList() {
        CustomerSubsystem cust = sessionContext.getCust();
        try {
            addressList = usecaseControl.getSavedAddresses(cust);
            initializeCheckedAddresses(addressList);

        } catch (BackendException e) {
            //handle
        }
        return addressList;
    }

    public CreditCard getCard() {
        CustomerSubsystem cust = sessionContext.getCust();
        if (!creditCardChanged) {
            card = usecaseControl.getDefaultCreditCard(cust);
        }
        return card;
    }

    public void setCard(CreditCard card) {
        this.card = card;
    }

    public List<CartItem> getCartItems() {
        CustomerSubsystem cust = sessionContext.getCust();
        return cust.getShoppingCart().getLiveCartItems();
    }

    public String getTermsAndConditions() {
        termsAndConditions = "Any item purchased from this site adhere to the terms and conditions"
                + " depicted in this document. You will have to accept the terms and "
                + "conditions dipicted here in order to purches anything from this site";
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public Address getShippingAddress() {
        CustomerSubsystem cust = sessionContext.getCust();
        if (!newShippingAddress && !shippingAddressChanged) {
            shippingAddress = usecaseControl.getDefaultShippingAddress(cust);
        }
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;

    }

    /**
     * Returns value of toString on an Address -- such strings are the keys for
     * the checkedaddresses hashmap
     */
    public String getCheckedKey(Address addr) {
        return addr.toString();
    }

    public Address getBillingAddress() {
        CustomerSubsystem cust = sessionContext.getCust();
        if (!billingAddSameAsShippingAdd) {
            billingAddress = usecaseControl.getDefaultBillingAddress(cust);
        } else {
            billingAddress = shippingAddress;
        }
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public boolean isNewShippingAddress() {
        return newShippingAddress;
    }

    public void setNewShippingAddress(boolean newShippingAddress) {
        this.newShippingAddress = newShippingAddress;
    }

    public boolean isBillingAddSameAsShippingAdd() {
        return billingAddSameAsShippingAdd;
    }

    public void setBillingAddSameAsShippingAdd(boolean billingAddSameAsShippingAdd) {
        this.billingAddSameAsShippingAdd = billingAddSameAsShippingAdd;
    }

    public CustomerProfile getCustProf() {
        CustomerSubsystem cust = sessionContext.getCust();
        custProf = usecaseControl.getCustomerProfile(cust);
        return custProf;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }


    /**
     * Called when customer selects one of his saved addresses to use for the
     * current shopping cart during checkout
     */
    public String selectAddress() {
        for (Address addr : addressList) {
            String key = addr.toString();
            if (checkedAddresses.get(key)) {
                shippingAddress = addr;
                shippingAddressChanged = true;
                return "checkoutwindow";
            }
        }
        return "checkoutwindow";
    }

    public String proceedToPaymentWindow(String currentPage) {
        CustomerSubsystem cust = sessionContext.getCust();

        try {
            if (newShippingAddress) {
                shippingAddress =
                        usecaseControl.runAddressRules(cust, shippingAddress);
                if (billingAddSameAsShippingAdd) {
                    billingAddress = shippingAddress;
                }
            }
            usecaseControl.setAddressesInCart(cust, shippingAddress, billingAddress);
            usecaseControl.saveNewAddress(cust, shippingAddress);

            return "creditcardwindow";
        } catch (RuleException e) {
            MessagesUtil.displayError(e.getMessage());
            return currentPage;
        } catch (BackendException e) {
            LOG.warning("A BackendException has been thrown: " + e.getMessage());
            return "errorDb";
        } catch (BusinessException e) {
            LOG.warning("A BusinessException has been thrown: " + e.getMessage());
            return "errorDb";
        }

    }

    public String proceedToTermsConditions(String currentPage) {
        CustomerSubsystem cust = sessionContext.getCust();

        try {
            usecaseControl.runPaymentRules(cust, billingAddress, card);
            usecaseControl.setPaymentMethodInCart(cust, card);
            return "termsAndConditions";
        } catch (RuleException e) {
            MessagesUtil.displayError(e.getMessage());
            return currentPage;
        } catch (BusinessException e) {
            LOG.warning("A BusinessException was thrown: " + e.getMessage());
            return "errorDb";
        }


    }

    public String submitOrder(String currentPage) {
        CustomerSubsystem cust = sessionContext.getCust();
        try {
            usecaseControl.runFinalOrderRules(cust.getShoppingCart());
            usecaseControl.submitFinalOrder(cust);
            return "orderConfirmation";
        } catch (RuleException e) {
            MessagesUtil.displayError(e.getMessage());
        } catch (BackendException e) {
            return "errorDb";
        } catch (BusinessException e) {
            return "errorDb";
        }
        return "errorDb";
    }
    
    public CheckoutCallback getCheckoutCallback() {
        return new CheckoutCallback();
    }

    public class CheckoutCallback implements Callback {

        public final Requirement REQUIREMENT = Requirement.CHECKOUT;

        public String doUpdate() {

            return checkout("cartitem", REQUIREMENT);
        }
    }
}
