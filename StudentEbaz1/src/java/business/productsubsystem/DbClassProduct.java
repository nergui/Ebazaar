package business.productsubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import business.util.TwoKeyHashMap;
import business.*;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductFromGui;
import static business.util.StringParse.*;
import java.util.logging.Level;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

class DbClassProduct implements DbClass {
	private static final Logger LOG = Logger.getLogger(DbClassProduct.class
			.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();

	/**
	 * The productTable matches product ID with Product object. It is static so
	 * that requests for "read product" based on product ID can be handled
	 * without extra db hits
	 */
	private static TwoKeyHashMap<Integer, String, Product> productTable;
	private String queryType;
	private String query;
	private Product product;
	private ProductFromGui prodFromGui;
	private String description;
	private List<Product> productList;
	Integer catalogId;
	Integer productId;
        String prodName;

	private final String LOAD_PROD_TABLE = "LoadProdTable";
	private final String READ_PRODUCT = "ReadProduct";
	private final String READ_PROD_LIST = "ReadProdList";
	private final String SAVE_NEW_PROD = "SaveNewProd";
        private final String DELETE_PROD = "DELETE";
        private final String UPDATE_PROD = "UPDATE"; 
        private final String READ_PRODUCT_Name="ReadProductName";

	public void buildQuery() {
		if (queryType.equals(LOAD_PROD_TABLE)) {
			buildProdTableQuery();
		} else if (queryType.equals(READ_PRODUCT)) {
			buildReadProductQuery();
		} else if (queryType.equals(READ_PROD_LIST)) {
			buildProdListQuery();
		} else if (queryType.equals(SAVE_NEW_PROD)) {
			buildSaveNewQuery();
		}else if (queryType.equals(DELETE_PROD)) {
			buildDeleteNewQuery();
		}else if (queryType.equals(UPDATE_PROD)) {
			buildUpdateQuery();
		}
        else if(queryType.equals(READ_PRODUCT_Name)){
                    buildReadProductNameQuery();
                }

	}
        private void buildUpdateQuery() {
         
            
        query = "UPDATE productsdb.product SET productname='"+prodFromGui.getProductName()+"', totalquantity='"+prodFromGui.getQuantityAvail()+"',priceperunit='"+prodFromGui.getUnitPrice()+"' WHERE productid="+productId;
        }
        private void buildDeleteNewQuery() {
		query = "DELETE FROM productsdb.product WHERE productid="+productId;
	}
	private void buildProdTableQuery() {
		query = "SELECT * FROM product";
	}

	private void buildProdListQuery() {
		query = "SELECT * FROM Product WHERE catalogid = " + catalogId;
	}

	private void buildReadProductQuery() {
		query = "SELECT * FROM Product WHERE productid = " + productId;
	}
        private void buildReadProductNameQuery() {
		query = "SELECT * FROM Product WHERE productname = \"" + prodName + "\"";
	}

        private void buildSaveNewQuery() {
		query = "INSERT into productsdb.product "
				+ "(productid,productname,totalquantity,priceperunit,mfgdate,catalogid,description) "
				+ "VALUES(NULL,'" + prodFromGui.getProductName() + "',"
				+ prodFromGui.getQuantityAvail() + ","
				+ prodFromGui.getUnitPrice() + ",'" + prodFromGui.getMfgDate()
				+ "'," + catalogId + ",'" + description + "')";
	}

	public TwoKeyHashMap<Integer, String, Product> readProductTable()
			throws DatabaseException {
		if (productTable != null) {
			return productTable.clone();
		}
		return refreshProductTable();
	}

	/**
	 * Force a database call
	 */
	public TwoKeyHashMap<Integer, String, Product> refreshProductTable()
			throws DatabaseException {
		queryType = LOAD_PROD_TABLE;
		dataAccessSS.atomicRead(this);
		
		// Return a clone since productTable must not be corrupted
		return productTable.clone();
	}

	public List<Product> readProductList(Integer catalogId)
			throws DatabaseException {
		if (productList == null) {
			return refreshProductList(catalogId);
		}
		return Collections.unmodifiableList(productList);
	}

	public List<Product> refreshProductList(Integer catalogId)
			throws DatabaseException {
		this.catalogId = catalogId;
		queryType = READ_PROD_LIST;
		dataAccessSS.atomicRead(this);
		return productList;
	}

	public Product readProduct(Integer productId)
			throws DatabaseException {
		if (productTable != null && productTable.isAFirstKey(productId)) {
			return productTable.getValWithFirstKey(productId);
		}
		queryType = READ_PRODUCT;
            this.productId=productId;
            dataAccessSS.atomicRead(this);
		return product;
	}
        public Product readProduct(String prodName)
			throws DatabaseException {
//		if (productTable != null && productTable.isAFirstKey(productId)) {
//			return productTable.getValWithFirstKey(productId);
//		}
		queryType = READ_PRODUCT_Name;
		this.prodName = prodName;
		dataAccessSS.atomicRead(this);
            return product;
	}

	/**
	 * Database columns: productid, productname, totalquantity, priceperunit,
	 * mfgdate, catalogid, description
	 */
	public void saveNewProduct(ProductFromGui product, Integer catalogid,
			String description) throws DatabaseException {
		this.prodFromGui = product;
		this.catalogId = catalogid;
		this.description = description;
		queryType = this.SAVE_NEW_PROD;
		dataAccessSS.saveWithinTransaction(this);
	}
       public  void updateProduct(ProductFromGui product) {
            this.prodFromGui = product;
            productId=prodFromGui.getProductId();
            queryType=this.UPDATE_PROD;
            try {
                dataAccessSS.saveWithinTransaction(this);
            } catch (DatabaseException ex) {
                Logger.getLogger(DbClassProduct.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        public void deleteProduct(int id) throws DatabaseException {
                productId=id;
		queryType = this.DELETE_PROD ;
		dataAccessSS.deleteWithinTransaction(this);
	}

	public void populateEntity(ResultSet resultSet) throws DatabaseException {
		if (queryType.equals(LOAD_PROD_TABLE)) {
			populateProdTable(resultSet);
		} else if (queryType.equals(READ_PRODUCT)) {
			populateProduct(resultSet);
		} else if (queryType.equals(READ_PROD_LIST)) {
			populateProdList(resultSet);
		} else if (queryType.equals(READ_PRODUCT_Name)){
                        populateProduct(resultSet);
		}
	}

	private void populateProdList(ResultSet rs) throws DatabaseException {
		productList = new LinkedList<Product>();
		try {
			Product product = null;
			Integer prodId = null;
			String productName = null;
			String quantityAvail = null;
			String unitPrice = null;
			String mfgDate = null;
			Integer catalogId = null;
			String description = null;
			while (rs.next()) {
				prodId = rs.getInt("productid");
				productName = rs.getString("productname");
				quantityAvail = makeString(rs.getInt("totalquantity"));
				unitPrice = makeString(rs.getDouble("priceperunit"));
				mfgDate = rs.getString("mfgdate");
				catalogId = rs.getInt("catalogid");
				description = rs.getString("description");
				product = new ProductImpl(prodId, productName, quantityAvail,
						unitPrice, mfgDate, catalogId, description);
				productList.add(product);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	/**
	 * Internal method to ensure that product table is up to date.
	 */
	private void populateProdTable(ResultSet rs) throws DatabaseException {
		productTable = new TwoKeyHashMap<Integer, String, Product>();
		try {
			Product product = null;
			Integer prodId = null;
			String productName = null;
			String quantityAvail = null;
			String unitPrice = null;
			String mfgDate = null;
			Integer catalogId = null;
			String description = null;
			while (rs.next()) {
				prodId = rs.getInt("productid");
				productName = rs.getString("productname");
				quantityAvail = makeString(rs.getInt("totalquantity"));
				unitPrice = makeString(rs.getDouble("priceperunit"));
				mfgDate = rs.getString("mfgdate");
				catalogId = rs.getInt("catalogid");
				description = rs.getString("description");
				product = new ProductImpl(prodId, productName, quantityAvail,
						unitPrice, mfgDate, catalogId, description);
				productTable.put(prodId, productName, product);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private void populateProduct(ResultSet rs) throws DatabaseException {
		try {
			/*Product product = null;
			Integer prodId = null;
			String productName = null;
			String quantityAvail = null;
			String unitPrice = null;
			String mfgDate = null;
			Integer catalogId = null;
			String description = null;
			while (rs.next()) {
				prodId = rs.getInt("productid");
				productName = rs.getString("productname");
				quantityAvail = makeString(rs.getInt("totalquantity"));
				unitPrice = makeString(rs.getDouble("priceperunit"));
				mfgDate = rs.getString("mfgdate");
				catalogId = rs.getInt("catalogid");
				description = rs.getString("description");
				product = new ProductImpl(prodId, productName, quantityAvail,
						unitPrice, mfgDate, catalogId, description);
                                */
                    if (rs.next()) {
				product = new ProductImpl(rs.getInt("productid"),
						rs.getString("productname"),
						makeString(rs.getInt("totalquantity")),
						makeString(rs.getDouble("priceperunit")),
						rs.getString("mfgdate"), rs.getInt("catalogid"),
						rs.getString("description"));
				
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
