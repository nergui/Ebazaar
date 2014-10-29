package integrationtests;

import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;
import middleware.exceptions.DatabaseException;
import alltests.AllTests;
import business.exceptions.BackendException;
import business.externalinterfaces.ProductSubsystem;
import business.productsubsystem.ProductSubsystemFacade;
import dbsetup.DbQueries;

public class BrowseAndSelectTest extends TestCase {
	
	static String name = "Browse and Select Test";
	static Logger log = Logger.getLogger(BrowseAndSelectTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	
	public void testCatalogListStep() throws DatabaseException {
		// Add row in CatalogType table for testing
		String[] vals = DbQueries.insertCatalogRow();
		String expectedName = vals[2];
		
		// Perform test
        ProductSubsystem pss = new ProductSubsystemFacade();
		List<String> catNames=null;
        
               try {
			catNames = pss.getCatalogNames();
                
		}
		catch(DatabaseException ex){
			fail("DatabaseException: " + ex.getMessage());
		}
		finally {
			assertTrue(catNames != null);
			boolean nameFound = false;
			if(catNames != null){
				for (String next : catNames) {
					if(next.equals(expectedName)) {
						nameFound=true;
						System.out.println(next);
						break;
					}
				}
			}
			assertTrue(nameFound);
			// Clean up table
			DbQueries.deleteCatalogSql(Integer.parseInt(vals[1]));
			
		}
	}

	

}