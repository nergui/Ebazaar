
package presentation.data;

import business.Login;
import business.exceptions.BackendException;
import business.exceptions.UserException;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import presentation.control.Callback;
import presentation.control.CallbackManager;
import usecasecontrol.LoginControl;

/**
 *
 * @author FGel
 */
@Named
//@ManagedBean
@SessionScoped
public class LoginSession implements Serializable {

    
    private static final Logger LOG = Logger.getLogger(LoginSession.class.getName());
    private String username;
    private String password;
    Login login;
    private String item;
    private String greeting = "Hello %s! The cart from your last session has %d %s";
    private boolean isLoggedIn = false;
    private String requestedUrl;
   
    //The requirement is the goal user is trying to reach ("retrieveSavedCart") 
    //-- recorded here in case login must take place before goal is reached
    private String requirement;
    
    @Inject
    BrowseSelectPCB bsPCB;
    
    @Inject
    CheckoutPCB checkoutPCB;
    
    @Inject
    CallbackManager cbm;
    
    private LoginControl loginControl = new LoginControl();

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String login() {
        LOG.info("Start login");
        login = new Login(Integer.parseInt(username), password);
        try {
            int authorizationLevel = loginControl.authenticate(login);
            setIsLoggedIn(true);
            loadCustomer(login, authorizationLevel);

            if (requestedUrl != null) {
                cbm.init();
                Callback callback = cbm.getCallback(requirement);

                //callback may return a new page to go to -- if so,
                //use that version; otherwise, use requestedUrl
                String alternatePage = null;
                if (callback != null) {
                    alternatePage = callback.doUpdate();
                }
                
                return navigateToPageAfterLogin(alternatePage);

            } else {

                return "index";
            }

        } catch (UserException e) {
            MessagesUtil.displayError(e.getMessage());
            return "login";
        } catch (BackendException e) {
            return "errorDb";
        }
    }
    
    public String checkLogin() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params =
                fc.getExternalContext().getRequestParameterMap();
        requestedUrl = params.get("url");
        requirement = params.get("requirement");
        if (isLoggedIn) {
            //Callback callback = (new CallbackManager()).getCallback(requirement);
            cbm.init();
            Callback callback = cbm.getCallback(requirement);
            String alternatePage = null;
            if (callback != null) {
               alternatePage = callback.doUpdate();
            }
            return navigateToPageAfterLogin(alternatePage);
        } else {
            return "login";
        }
    } 
    
    public String getRequestedUrl() {
        return requestedUrl;
    }

    public void setRequestedUrl(String requestedUrl) {
        this.requestedUrl = requestedUrl;
    }
    //private UserDB userdb;
    private LoginControl usecaseControl = new LoginControl();

    // private ShoppingCartSystemDB cartDb;
    //  private  LoginBeanReq req;
    public LoginSession() {
        
    }
    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
    //after the retriveCart item is implmented counting the cart items should go to the borwseAndSelect bean

    public String getGreeting() {
        SessionData sessionContext = bsPCB.getSessionContext();
        CustomerSubsystem cust = sessionContext.getCust();
        if (cust != null) {

            ShoppingCart savedCart = cust.getShoppingCart().getSavedCart();
            int numItems = (savedCart == null) ? 0 : savedCart.getCartItems().size();
            String custname = cust.getCustomerProfile().getFirstName();
            //item variable is either 'item' or 'items' depending on num items in cart
            item = " items.";
            if (numItems == 1) {
                item = " item.";
            }
            greeting = String.format(greeting, custname, numItems, item);
            // greeting = String.format(greeting, custname); 
        }
        return greeting;
    }

    String navigateToPageAfterLogin(String alternatePage) {
 
        String temp = (alternatePage == null) ? requestedUrl : alternatePage;
        requestedUrl = null;
        return temp;
    }

    public void checkLogin(ComponentSystemEvent event) {

        if (!isLoggedIn) {
            FacesContext context = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
            handler.performNavigation("login");

        }
    }

    public void loadCustomer(Login login, int authorizationLevel) throws BackendException {
        //prepareCustomerObject(login.getCustId()) creat customerSubsystem set 
        //its property retrive saved cart and assign it savecart
        SessionData sessionContext = bsPCB.getSessionContext();
        CustomerSubsystem cust = usecaseControl.prepareCustomerObject(login.getCustId(), authorizationLevel);
        sessionContext.setCust(cust);
        sessionContext.setIsLoggedIn(true);
        ShoppingCartSubsystem externalCart = sessionContext.getExternalShopCartSS();
        if (externalCart != null) {
            cust.getShoppingCart().setLiveCart(externalCart.getLiveCart());
            sessionContext.clearExternalShopCart();
        }

    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        isLoggedIn = false;
        return "index";
    }

    
    
    
}
