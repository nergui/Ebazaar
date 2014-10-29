package usecasecontrol;

import business.DbClassLogin;
import business.Login;
import business.customersubsystem.CustomerSubsystemFacade;
import business.exceptions.BackendException;
import business.exceptions.UserException;
import business.externalinterfaces.*;

public class LoginControl {

    public int authenticate(Login login) throws UserException, BackendException {

        DbClassLogin dbClass = new DbClassLogin(login);
        if (!dbClass.authenticate()) {
            throw new UserException("Authentication failed for ID: " + login.getCustId());
        }
        return dbClass.getAuthorizationLevel();

    }

    public CustomerSubsystem prepareCustomerObject(Integer custId, int authorizationLevel) throws BackendException {
        CustomerSubsystem customer = new CustomerSubsystemFacade();
        customer.initializeCustomer(custId, authorizationLevel);
        return customer;
    }
    /*
     private SessionContext context;
     private LoginWindow loginWindow;
     private Controller controller;
    
     Component currWindow;
     Component parentWindow;
    
     //If parent and current windows are same, we don't make one vanish
     //while the other is displayed.
     private boolean parentIsOuterFrame;
    
     public LoginControl(Component currWindow, Component parentWindow){
     this.currWindow = currWindow;
     this.parentWindow = parentWindow;
     parentIsOuterFrame = (parentWindow.getClass() == EbazaarMainFrame.class);
     }
     public LoginControl(Component currWindow, Component parentWindow, Controller controller){
     this(currWindow,parentWindow);
     this.controller=controller;
     }    
    
     public void startLogin() {
     context = SessionContext.getInstance();
     loginWindow = new LoginWindow(this);
        
     EbazaarMainFrame.getInstance().getDesktop().add(loginWindow);
     //loginWindow.setVisible(true);
     //loginWindow.show();
     if(!parentIsOuterFrame) parentWindow.setVisible(false);
     loginWindow.setVisible(true);
     }
     private void loadCustomer(Integer custId) throws DatabaseException{
     CustomerSubsystem customer = new CustomerSubsystemFacade();
     customer.initializeCustomer(custId);
     SessionContext context = SessionContext.getInstance();
     context.add(CustomerConstants.LOGGED_IN, Boolean.TRUE);
     context.add(CustomerConstants.CUSTOMER, customer);

        
     }
     private void authenticate(Integer id, String pwd) {
     try {
     //authenticate
     Login login = new Login(id,pwd);
     DbClassLogin dbClass = new DbClassLogin(login);
     boolean authenticated = dbClass.authenticate();
        
     //if authenticated, load customer subsystem
     if(authenticated){
     loadCustomer(id);
     JOptionPane.showMessageDialog(loginWindow,                                                    
     "Login successful",
     "Success", 
     JOptionPane.INFORMATION_MESSAGE);                
        	    
     }
     else {
     throw new UserException("Either id or password is incorrect.");
 
     }
     }
     catch(EBazaarException e){
     JOptionPane.showMessageDialog(loginWindow,                                                    
     "Error: "+e.getMessage(),
     "Error", 
     JOptionPane.ERROR_MESSAGE);
                
     loginWindow.setVisible(true);
     }
     }
     //////// event handling code
    
     public SubmitListener getSubmitListener(LoginWindow w) {
     return new SubmitListener();
     }
     public CancelListener getCancelListener(LoginWindow w) {
     return new CancelListener();
     }
    
     class SubmitListener implements ActionListener {
     public void actionPerformed(ActionEvent evt) {
     loginWindow.setVisible(false);
     Integer id = Integer.parseInt(loginWindow.getCustId());
     String pwd = loginWindow.getPassword();
     authenticate(id,pwd);
     loginWindow.dispose();
     if(controller != null){
     Boolean loggedIn = (Boolean)SessionContext.getInstance().get(CustomerConstants.LOGGED_IN);
     if(loggedIn==Boolean.TRUE) controller.doUpdate();
     else parentWindow.setVisible(true);
     }
     else {
     Boolean loggedIn = (Boolean)SessionContext.getInstance().get(CustomerConstants.LOGGED_IN);
     if(loggedIn==Boolean.TRUE) currWindow.setVisible(true);
     else parentWindow.setVisible(true);
       	    	
     }   	    
     }
     }
     class CancelListener implements ActionListener {
     public void actionPerformed(ActionEvent evt) {
        	
     if(parentWindow != null) {
     parentWindow.setVisible(true);
     }
     loginWindow.dispose();

     }
     }
    
     */
}
