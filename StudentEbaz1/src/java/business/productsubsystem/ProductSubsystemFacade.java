/*
 * Created on Mar 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package business.productsubsystem;

import java.util.List;

import business.DbClassQuantity;
import business.Quantity;
import business.exceptions.BackendException;
import business.util.*;	
import business.externalinterfaces.Catalog;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductFromGui;
import business.externalinterfaces.ProductSubsystem;
import java.util.logging.Level;
import java.util.logging.Logger;
import business.util.*;
import java.util.List;
import javax.faces.model.SelectItem;
import middleware.exceptions.DatabaseException;
import usecasecontrol.BrowseSelectController;

public class ProductSubsystemFacade implements ProductSubsystem {
	final String DEFAULT_PROD_DESCRIPTION = "New Product";
	CatalogTypesImpl types;
	
        @Override
    public TwoKeyHashMap<Integer,String,Product> getProductTable() throws DatabaseException {
        DbClassProduct dbClass = new DbClassProduct();
        return dbClass.readProductTable();
        
    }
	public TwoKeyHashMap<Integer,String,Product> refreshProductTable() throws DatabaseException {
		DbClassProduct dbClass = new DbClassProduct();
        return dbClass.refreshProductTable();		
	}

    public List<String> refreshCatalogNames() throws DatabaseException {
        DbClassCatalogTypes dbclass = new DbClassCatalogTypes();
        types = dbclass.getCatalogTypes();
        return types.getCatalogNames();
    }
    
    public List<String> getCatalogNames() throws DatabaseException {
        
        //don't hit database if not needed
        if(types == null){
            DbClassCatalogTypes dbclass = new DbClassCatalogTypes();
            types = dbclass.getCatalogTypes();
        }
        return types.getCatalogNames();
    }
    public List<Product> getProductList(String catType) throws DatabaseException {
        DbClassProduct dbclass = new DbClassProduct();
        Integer catId = getCatalogIdFromType(catType);
        List<Product> list = dbclass.readProductList(catId);
        //this is a read-only list
        System.out.println("returning list");
        return list;
    }
	public List<Product> refreshProductList(String catType) throws DatabaseException {
		return getProductList(catType);
	}
	public Product getProduct(String prodName) throws DatabaseException {
		//implement - returning stub data
             DbClassProduct dbclass = new DbClassProduct();
              System.out.println("to be printed");
        //Integer prodId = getProductIdFromName(prodName);
         System.out.println("returning list");
       return  dbclass.readProduct(prodName);
        //this is a read-only list
        }
           
	public Product getProductFromId(Integer prodId) throws BackendException {
            //implement
            DbClassProduct dbClass=new DbClassProduct();
            
            Product p = null;
            try {
                p = dbClass.readProduct(prodId);
            } catch (DatabaseException ex) {
                Logger.getLogger(ProductSubsystemFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
            return p;
	}
	public Integer getProductIdFromName(String prodName) throws DatabaseException {
		//implement
             
		DbClassProduct dbClass = new DbClassProduct();
                
        return dbClass.readProduct(prodName).getProductId();
		
	}
	
	public void saveNewCatalogName(String name) throws DatabaseException {
        
            DbClassCatalog dbclass=new DbClassCatalog();
            dbclass.saveNewCatalog(name);
        }
        public void updateCatalogName(String name,int id) throws DatabaseException {
        
            DbClassCatalog dbclass=new DbClassCatalog();
            dbclass.updateCatalogName(name,id);
        }
        public void deleteCatalog(int id) {
        
            DbClassCatalog dbclass=new DbClassCatalog();
            dbclass.deleteCatalog(id);
        }
	public Integer getCatalogIdFromType(String catType) throws DatabaseException {

            //DbClassCatalogTypes dbclass=new DbClassCatalogTypes();
            //types=dbclass.getCatalogIdFromName(catType);
            //return types.getCatalogId(catType);
			  BrowseSelectController bsController = new BrowseSelectController();		
        try {
            List<Catalog> cats = bsController.getCatalog(catType);

            for (Catalog cat : cats) {
                if(catType.equalsIgnoreCase(cat.getName()))
                    return Integer.parseInt(cat.getId());              
            }

        } catch (DatabaseException e) {
            //handle exception
        }
            return 0;
	}
	public ProductFromGui createProduct(String name, String date, String numAvail, String unitPrice) {
		return new ProductImpl(name,date,numAvail,unitPrice);
		
	}
	/** the input product has only the values given from the
	 *  gui -- first step is to locate other values needed, and then
	 *  do the save
	 *  database columns:
	 *  productid, productname, totalquantity, priceperunit,
	 *  mfgdate, catalogid, description
	 */
	public void saveNewProduct(ProductFromGui product, String catalogType) throws DatabaseException {
		//get catalogid
		Integer catalogid = getCatalogIdFromType(catalogType); 
				//invent description
		String description = DEFAULT_PROD_DESCRIPTION;
		DbClassProduct dbclass = new DbClassProduct();
		dbclass.saveNewProduct(product, catalogid,description);
		
	}
	/* reads quantity avail and stores in the Quantity argument */
	public void readQuantityAvailable(String prodName, Quantity quantity) throws DatabaseException {
		//stub
		//quantity.setQuantityAvailable("0");
		//implement
                DbClassQuantity dbclass = new DbClassQuantity();
		dbclass.setQuantity(quantity);
		dbclass.readQuantityAvail(prodName);
				
	}

        public void deleteProduct(int id) {
            DbClassProduct dbClass=new DbClassProduct();
            try {
                dbClass.deleteProduct(id);
            } catch (DatabaseException ex) {
                Logger.getLogger(ProductSubsystemFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

    public void updateProduct(Product p) {
        DbClassProduct dbClass=new DbClassProduct();
        dbClass.updateProduct((ProductFromGui) p);
    }

	public List<Catalog> getCatalog(String catType) throws DatabaseException {
         DbClassCatalogTypes dbclass = new DbClassCatalogTypes();
          List<Catalog> cats = dbclass.getCatalogList();
          return cats;
    }

}
