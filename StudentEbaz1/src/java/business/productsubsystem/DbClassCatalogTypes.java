package business.productsubsystem;

import business.externalinterfaces.Catalog;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

/**
 * @author pcorazza
 * <p>
 * Class Description: 
 */
public class DbClassCatalogTypes implements DbClass {

	private static final Logger LOG = 
		Logger.getLogger(DbClassCatalogTypes.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
    private String query;
    private String queryType;
    private String name;
    final String GET_TYPES = "GetTypes";
    final String GET_Id = "GetId";
    private CatalogTypesImpl types;
    private List<Catalog> catalogList;
    private final String LOAD_PROD_TABLE = "LoadCatTable";
    private final String READ_PROD_LIST = "ReadCatList";
    
    public CatalogTypesImpl getCatalogTypes() throws DatabaseException {
        queryType = GET_TYPES;
        dataAccessSS.atomicRead(this);	
        return types;        
    }
    public CatalogTypesImpl getCatalogIdFromName(String kName) throws DatabaseException {
        queryType = GET_Id;
        name=kName;
        dataAccessSS.atomicRead(this);	
        return types;        
    }
    
    public void buildQuery() {
        if(queryType.equals(GET_TYPES)){
            buildGetTypesQuery();}
        else if(queryType.equals(LOAD_PROD_TABLE)){
            buildGetTypesQuery();
        }
    }

    void buildGetTypesQuery() {
        query = "SELECT * FROM CatalogType";       
    }
     void buildGetTypesQueryIdFromName(String name) {
        query = "SELECT * FROM CatalogType WHERE catalogname='"+name+"'";       
    }
    
    /**
     * This is activated when getting all catalog types.
     */
    	public List<Catalog> getCatalogList()
			throws DatabaseException {
		if (catalogList == null) {
			return refreshProductList();
		}
		return Collections.unmodifiableList(catalogList);
	}
    	public List<Catalog> refreshProductList()
			throws DatabaseException {	
		queryType = LOAD_PROD_TABLE;
		dataAccessSS.atomicRead(this);
		return catalogList;
	}
    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        types = new CatalogTypesImpl();
        if(queryType.equals(LOAD_PROD_TABLE))
             populateCatList(resultSet);
        try {
            while(resultSet.next()){
                types.addCatalog(resultSet.getInt("catalogid"),
                        		resultSet.getString("catalogname"));
            }
        }catch(SQLException e){
            throw new DatabaseException(e);
        }
        
    }
   
    private void populateCatList(ResultSet rs) throws DatabaseException {
		catalogList = new LinkedList<Catalog>();
		try {
			Catalog  cat = null;
			Integer catId = null;
			String catName = null;
			
			while (rs.next()) {
				catId = rs.getInt("catalogid");
				catName = rs.getString("catalogname");				
				cat = new CatalogImpl(String.valueOf(catId), catName);
				catalogList.add(cat);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
   
    public String getDbUrl() {
    	DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
    }

    public String getQuery() {

        return query;
    }

   

}
