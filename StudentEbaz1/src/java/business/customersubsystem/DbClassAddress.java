
package business.customersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbConfigKey;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import business.externalinterfaces.Address;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.DbClassAddressForTest;

class DbClassAddress implements DbClass, DbClassAddressForTest {
	private static final Logger LOG = Logger.getLogger(DbClassAddress.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();
    private CustomerProfile custProfile;
    private Address address;
    private List<Address> addressList;
    private AddressImpl defaultShipAddress;
    private AddressImpl defaultBillAddress;
    private String queryType;
    private String query;
    
    private final String SAVE = "Save";
    private final String SAVE_DEFAULT_SHIP = "SaveDefaultShip";
    private final String SAVE_DEFAULT_BILL = "SaveDefaultBill";
    private final String READ = "Read";
    private final String READ_DEFAULT_SHIP = "ReadDefaultShip";
    private final String READ_DEFAULT_BILL = "ReadDefaultBill";
	//column names
    private final String STREET="street";
    private final String CITY = "city";
    private final String STATE = "state";
    private final String ZIP = "zip";
	
    //data access subsystem
    
    
    public void saveAddress(CustomerProfile custProfile) throws DatabaseException {
        this.custProfile = custProfile;
        queryType = SAVE;
        dataAccessSS.saveWithinTransaction(this);
    }
    
    public void buildQuery() throws DatabaseException {
        if(queryType.equals(SAVE)){
            buildSaveNewAddrQuery();
        }
        else if(queryType.equals(SAVE_DEFAULT_SHIP)){
            buildSaveDefaultShipQuery();
        }
        else if(queryType.equals(SAVE_DEFAULT_BILL)){
           // buildSaveDefaultBillQuery();
        }
        else if(queryType.equals(READ)){
            buildReadAllAddressesQuery();
        }
        else if(queryType.equals(READ_DEFAULT_SHIP)){
            buildReadDefaultShipQuery();
        }
        else if(queryType.equals(READ_DEFAULT_BILL)){
            buildReadDefaultBillQuery();
        }
    }
    
    Address getAddress() {
        return address;
    }
    
    public List<Address> getAddressList() {
        return addressList;
    }
    
    AddressImpl getDefaultShipAddress(){
        return this.defaultShipAddress;
    }
    
    AddressImpl getDefaultBillAddress() {
        return this.defaultBillAddress;
    }
	
    void readDefaultShipAddress(CustomerProfile custProfile) throws DatabaseException {
    	this.custProfile = custProfile;
        queryType=READ_DEFAULT_SHIP;
        dataAccessSS.atomicRead(this);       
    }
    
    void readDefaultBillAddress(CustomerProfile custProfile) throws DatabaseException {
    	this.custProfile = custProfile;
        queryType=READ_DEFAULT_BILL;
        dataAccessSS.atomicRead(this);       
    }
      
    public void readAllAddresses(CustomerProfile custProfile) throws DatabaseException {
    	this.custProfile = custProfile;
        queryType = READ;
        dataAccessSS.atomicRead(this);            
    }
    
        
    void setAddress(Address addr){
        address = addr;
    }
    void buildReadCustNameQuery() {
        query = "SELECT fname, lname " +
        "FROM Customer " +
        "WHERE custid = " + custProfile.getCustId();        
    }
	
    void buildSaveNewAddrQuery() throws DatabaseException {
        query = "INSERT into altshipaddress " +
        		"(addressid,custid,street,city,state,zip) " +
        		"VALUES(NULL," +
        				  custProfile.getCustId() + ",'" +
        				  address.getStreet1()+"','" +
        				  address.getCity() + "','" +
        				  address.getState() + "','" +
        				  address.getZip() + "')";
    }
    void buildSaveDefaultShipQuery() throws DatabaseException {
        query = "INSERT into customer " +
        		"(addressid,custid,street,city,state,zip) " +
        		"VALUES(NULL," +
        				  custProfile.getCustId() + ",'" +
        				  address.getStreet1()+"','" +
        				  address.getCity() + "','" +
        				  address.getState() + "','" +
        				  address.getZip() + "')";
    }
    
    void buildReadAllAddressesQuery() {
        query = "SELECT * from altshipaddress where custid="+custProfile.getCustId();
    }
    
    void buildReadDefaultShipQuery() {
        query = "SELECT shipaddress1, shipaddress2, shipcity, shipstate, shipzipcode "+
        "FROM Customer "+
        "WHERE custid = " + custProfile.getCustId();
    }
    
    void buildReadDefaultBillQuery() {
        query = "SELECT billaddress1, billaddress2, billcity, billstate, billzipcode "+
        "FROM Customer "+
        "WHERE custid = " + custProfile.getCustId();
    }
    public String getDbUrl() {
    	DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
        
    }
    public String getQuery() {
        return query;
        
    }
    void populateAddressList(ResultSet rs) throws DatabaseException {
        addressList = new LinkedList<Address>();
        if(rs != null){
            try {
                while(rs.next()) {
                    address = new AddressImpl();
                    String str = rs.getString(STREET);
                    address.setStreet1(str);
                    address.setCity(rs.getString(CITY));
                    address.setState(rs.getString(STATE));
                    address.setZip(rs.getString(ZIP));
                    addressList.add(address);
                }                
            }
            catch(SQLException e){
                throw new DatabaseException(e);
            }         
        }       
    }
    
    void populateDefaultShipAddress(ResultSet rs) throws DatabaseException {
        try {
            if(rs.next()){
                defaultShipAddress = new AddressImpl(rs.getString("shipaddress1"),
                                                 rs.getString("shipaddress2"),
                                                 rs.getString("shipcity"),
                                                 rs.getString("shipstate"),
                                                 rs.getString("shipzipcode"));
            }          
        }
        catch(SQLException e) {
            throw new DatabaseException(e);
        }
        
    }
    
    void populateDefaultBillAddress(ResultSet rs) throws DatabaseException {
        try {
            if(rs.next()){
                defaultBillAddress = new AddressImpl(rs.getString("billaddress1"),
                                                 rs.getString("billaddress2"),
                                                 rs.getString("billcity"),
                                                 rs.getString("billstate"),
                                                 rs.getString("billzipcode"));
            }          
        }
        catch(SQLException e) {
            throw new DatabaseException(e);
        }
        
    }
	
	
    /* used only when we read from the database
     */
    public void populateEntity(ResultSet rs) throws DatabaseException {
        if(queryType.equals(READ)){
            populateAddressList(rs);
        }		        		
        else if(queryType.equals(READ_DEFAULT_SHIP)){
            populateDefaultShipAddress(rs);
        }
        else if(queryType.equals(READ_DEFAULT_BILL)){
            populateDefaultBillAddress(rs);
        }
        
    }
    public static void main(String[] args){
        DbClassAddress dba = new DbClassAddress();
        try {
            dba.readAllAddresses(dba.custProfile);
            System.out.println(dba.getAddressList());
        }
        catch(DatabaseException e){
            e.printStackTrace();
        }
    }
 
}
