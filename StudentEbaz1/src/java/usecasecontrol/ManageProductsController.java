
package usecasecontrol;

import business.exceptions.BackendException;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductFromGui;
import business.externalinterfaces.ProductSubsystem;
import business.productsubsystem.ProductSubsystemFacade;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import middleware.exceptions.DatabaseException;


public class ManageProductsController   {
    public int catId=0;
    private static final Logger LOG = 
    	Logger.getLogger(ManageProductsController.class.getName());
    
    public List<Product> getProductsList(String catalog) throws BackendException {
    	ProductSubsystem pss = new ProductSubsystemFacade();
    	try {
    		return pss.getProductList(catalog);
    	} catch(DatabaseException e) {
    		throw new BackendException(e);
    	}
    	
    }
    
    public List<String> getCatalogList() throws BackendException {
    	ProductSubsystem pss = new ProductSubsystemFacade();
    	try {
    		return pss.getCatalogNames();
    	} catch(DatabaseException e) {
    		throw new BackendException(e);
    	}
    }
    
    public void saveNewCatalogName(String name) throws BackendException {
        ProductSubsystem pss = new ProductSubsystemFacade();
        try {
            pss.saveNewCatalogName(name);
        } catch(DatabaseException e) {
            throw new BackendException(e.getMessage());
        }
    }
     public void updateCatalogName(String name) throws BackendException {
        ProductSubsystem pss = new ProductSubsystemFacade();
        try {
            pss.updateCatalogName(name, catId);
        } catch(DatabaseException e) {
            throw new BackendException(e.getMessage());
        }
    }
    
    public void saveNewProduct(String catalogSelected, String prodName, 
        String mfDate, String quantityAvail, String unitPrice) throws BackendException {
        ProductSubsystem pss = new ProductSubsystemFacade();
        ProductFromGui p = pss.createProduct(prodName, mfDate, quantityAvail, unitPrice);
        try {
            pss.saveNewProduct(p, catalogSelected);
        } catch(DatabaseException e) {
            throw new BackendException(e.getMessage());
        }
    }
    
    public void deleteCatalog() throws BackendException{
 
         ProductSubsystem pss = new ProductSubsystemFacade();
         pss.deleteCatalog(catId);
    }
     public void getCatalogIdFromType(String name){
 
         ProductSubsystem pss = new ProductSubsystemFacade();
        try {
            catId=pss.getCatalogIdFromType(name);
        } catch (DatabaseException ex) {
            Logger.getLogger(ManageProductsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void deleteProduct(int id) {
    	//implement
        ProductSubsystemFacade pss=new ProductSubsystemFacade();
        pss.deleteProduct(id);
    }

    public void updateProduct(Product p) {
        ProductSubsystemFacade pss=new ProductSubsystemFacade();
        pss.updateProduct(p);
    }
   
    
}
