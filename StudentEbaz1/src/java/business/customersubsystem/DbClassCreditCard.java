
package business.customersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import business.externalinterfaces.CustomerProfile;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;


class DbClassCreditCard implements DbClass {
	private static final Logger LOG = 
		Logger.getLogger(DbClassCreditCard.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
    private final String READ = "Read";
    private CustomerProfile custProfile;
    String query;
    private String queryType;
    private CreditCardImpl defaultPaymentInfo;
    
    CreditCardImpl getDefaultPaymentInfo(){
        return defaultPaymentInfo;
    }
    public void readDefaultPaymentInfo(CustomerProfile custProfile) throws DatabaseException {
        this.custProfile = custProfile;
        queryType=READ;
        dataAccessSS.atomicRead(this);      	
    }
 
    public void buildQuery() {
        //implement
        if(queryType.equals(READ)){
            buildReadDefaultPaymentInfoQuery();
        }
    }
    
     void buildReadDefaultPaymentInfoQuery() {
        query = "SELECT nameoncard, expdate, cardtype, cardnum " +
        "FROM Customer " +
        "WHERE custid = " + custProfile.getCustId();        
    }
    
    
    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        //implement
        try {
            if(resultSet.next()){
                defaultPaymentInfo = new CreditCardImpl(resultSet.getString("nameoncard"),
                                                 resultSet.getString("expdate"),
                                                 resultSet.getString("cardnum"),
                                                 resultSet.getString("cardtype"));
            }          
        }
        catch(SQLException e) {
            throw new DatabaseException(e);
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
