package business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import business.exceptions.BackendException;

import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

public class DbClassLogin implements DbClass {
	private static final Logger LOG = 
		Logger.getLogger(DbClassLogin.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
    private Integer custId;
    int authorizationLevel;
    private String password;
    @SuppressWarnings("unused")
    private Login login;
    private String query;
    private DataAccessSubsystem dataAccess;
    private boolean authenticated;
    
    public DbClassLogin(Login login){
        this.login=login;
        this.custId = login.getCustId();
        this.password = login.getPassword();
    }
    public boolean authenticate() throws BackendException {
    	try {
    		dataAccessSS.atomicRead(this);
    	} catch(DatabaseException e ) {
    		throw new BackendException(e);
    	}
    	
        return authenticated;        
    }
    
    public int getAuthorizationLevel() {
        return authorizationLevel;
    }
    
    /** Tries to locate the custId/password pair in Customer table */
    public void buildQuery() {
        query = "SELECT * FROM Customer WHERE custid = " + custId+ " AND password = '"+password+"'";
    }

    public void populateEntity(ResultSet resultSet) throws DatabaseException  {
        try {
            if(resultSet.next()) {
            	authorizationLevel = resultSet.getInt("admin");           	
                authenticated = true;
            	
            }
        } catch(SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
      
    }
 
    public String getDbUrl() {
    	DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
    }

    public String getQuery() {
        return query;
    }
}
