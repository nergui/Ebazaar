package business.productsubsystem;

import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.dataaccess.DataAccessUtil;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

public class DbClassCatalog implements DbClass {
	private static final Logger LOG = 
		Logger.getLogger(DbClassCatalog.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
	
	private String catalogName;
        private int catalogId;
	private String query;
    private String queryType;
    private final String SAVE = "Save";
    private final String DELETE = "Delete";
    private final String UPDATE = "Update";
    
        public void saveNewCatalog(String name) throws DatabaseException {
            this.catalogName = name;
            queryType = SAVE;
            dataAccessSS.saveWithinTransaction(this);  	
        }
        public void updateCatalogName(String name,int id) throws DatabaseException {
            this.catalogName = name;
            catalogId=id;
            queryType = UPDATE;
            dataAccessSS.saveWithinTransaction(this);  	
        }
     
     
	public void buildQuery() throws DatabaseException {
		if(queryType.equals(SAVE)) {
			buildSaveQuery();
		}else if(queryType.equals(DELETE)){
                	buildDeleteQuery();
                }else if(queryType.equals(UPDATE)){
                	buildUpdateQuery();
                }		
	}
	
	void buildSaveQuery() throws DatabaseException {
		query = "INSERT into CatalogType "+
		"(catalogid,catalogname) " +
		"VALUES(NULL,'"+
				  catalogName+"')"; 
	}
        void buildDeleteQuery() throws DatabaseException {
		query = "DELETE FROM productsdb.catalogtype WHERE catalogid='"+catalogId+"'"; 
	}
        void buildUpdateQuery() throws DatabaseException {
		query = "UPDATE productsdb.catalogtype SET catalogname='"+catalogName+"' WHERE catalogid='"+catalogId+"'"; 
	}

	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
	}

	public String getQuery() {
		return query;
	}

	public void populateEntity(ResultSet resultSet) throws DatabaseException {
		// do nothing
		
	}

        public  void deleteCatalog(int id) {
            this.catalogId = id;
    	    queryType = DELETE;
            try {  	
                dataAccessSS.deleteWithinTransaction(this);
            } catch (DatabaseException ex) {
                Logger.getLogger(DbClassCatalog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
	
}
