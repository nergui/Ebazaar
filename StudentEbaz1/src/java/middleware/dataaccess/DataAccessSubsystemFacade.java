
package middleware.dataaccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.Cleanup;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DataAccessTest;
import middleware.externalinterfaces.DbClass;

/**
 * @author pcorazza
 * @since Nov 10, 2004
 * Class Description:
 * Change Log: DbAction, rather than the facade, manages the connection
 * 
 */
public class DataAccessSubsystemFacade implements DataAccessSubsystem, DataAccessTest {
    
	private static final Logger LOG = Logger.getLogger(DataAccessSubsystemFacade.class.getPackage().getName());
	DbAction action;
	//Connection con;
	
	/**
	 * Gets a connection from the SimpleConnectionPool and caches it.
	 * Also caches the DbAction instance that is used. Note that all
	 * methods in DataAccess require createConnection as the first step.
	 * In saveInTransaction, createConnection is called explicitly.
	 * The save and delete methods have as a precondition that createConnection
	 * has already been called.
	 */
	public void createConnection(DbClass dbClass) throws DatabaseException {
		if(dbClass != null) {
			action = new DbAction(dbClass);
			System.out.println("\n NEW DBACTION \n");
			//con = action.pool.getConnection(dbClass.getDbUrl());
		}
	}
	
	/**
	 * Returns connection to pool and sets autoCommit to true.
	 */
	public void releaseConnection() throws DatabaseException {  
		//action.pool.returnToPool(con, dbClass.getDbUrl());
		action.returnToPool();
	}
	
	/**
	 * Note: autocommit is set back to true when connection is returned to pool
	 * Precondition: A Connection has been obtained via createConnection
	 */
	public void startTransaction() throws DatabaseException {
		action.startTransaction();
	}
	
	/**
	 * Precondition: A Connection has been obtained via createConnection
	 */
	public void commit() throws DatabaseException {
		action.commit();
	}
	
	/**
	 * Precondition: A Connection has been obtained via createConnection
	 */
	public void rollback() throws DatabaseException {
		action.rollback();
	}
	
	///// Raw read and save methods -- typically used as part of a bigger transaction //////
	
	/**
	 * Precondition: A Connection has been obtained via createConnection. 
	 * User must manually releaseConnection after read has completed. 
	 * Can use atomicRead to handle createConnection and releaseConnection
	 * if no other data access code is bundled with the read.
	 */
    public void read() throws DatabaseException {
        action.performRead();
    }
	
    /**
	 * Precondition: A Connection has been obtained via createConnection. User
	 * of this code must manually releaseConnection after operation has completed.
	 * For updates, returns number of rows affected; for inserts, returns generated
	 * key, if there is one.
	 */
    public Integer save() throws DatabaseException  {     
        return action.performUpdate();
    }

    /**
     * Precondition: A Connection has been obtained via createConnection. User
	 * of this code must manually releaseConnection after operation has completed.
	 * Returns number of rows deleted
     */
    public Integer delete() throws DatabaseException  {
        return action.performUpdate();
    }
    
    
    ////// Convenience methods for data access operations that are already atomic ////////
    
	/**
	 * This convenience method carries out a typical insert/update within a transaction. To wrap
	 * multiple or complex sql operations in a transaction, use startTransaction instead. Note
	 * that createConnection is called as part of the method body (so a separate call to
	 * createConnection is not required in this case). Likewise, releaseConnection is handled
	 * automatically.
	 */
	public Integer saveWithinTransaction(DbClass dbClass) 
			throws DatabaseException {
		createConnection(dbClass);
		startTransaction();
        try {
        	int result = save();
        	commit();
        	return result;
        } catch(DatabaseException e) {
        	LOG.warning("Attempting to rollback...");
        	rollback();
        	throw (e);
        }  finally {
        	releaseConnection();
        }
	}
	
	/**
	 * This convenience method carries out a typical delete within a transaction. To wrap
	 * multiple or complex sql operations in a transaction, use startTransaction instead. Note
	 * that createConnection is called as part of the method body (so a separate call to
	 * createConnection is not required in this case). Likewise, releaseConnection is handled
	 * automatically.
	 */
	public Integer deleteWithinTransaction(DbClass dbClass) 
			throws DatabaseException {
		createConnection(dbClass);
		startTransaction();
		int numRows = 0;
        try {
        	numRows = delete();
        	commit();  
        	return numRows;
        } catch(DatabaseException e) {
        	LOG.warning("Attempting to rollback...");
        	rollback();
        	throw (e);
        }  finally {
        	releaseConnection();
        }
	}
	
	/**
	 * This convenience method performs a single read operation. It handles
	 * the createConnection and releaseConnection steps.
	 */
	public void atomicRead(DbClass dbClass) 
			throws DatabaseException {
		createConnection(dbClass);
		read();
		releaseConnection();
	}
		
	public void closeAllConnections(Cleanup c){
        SimpleConnectionPool pool = SimpleConnectionPool.getInstance(c);
        if(pool != null) pool.closeConnections();		
	}

	     
    //Testing interface
    public ResultSet[] multipleInstanceQueries(String[] queries, String[] dburls) throws DatabaseException {
    	if(queries == null || dburls == null) return null;
    	if(queries.length != dburls.length) return null;
    	int numConnections = queries.length;
    	ResultSet[] results = new ResultSet[numConnections];
    	SimpleConnectionPool pool = SimpleConnectionPool.getInstance(numConnections);
        ArrayList<Connection> cons = new ArrayList<Connection>();
        for(int i = 0; i < numConnections; ++i) {
        	cons.add(pool.getConnection(dburls[i]));
        }
        for(int i = 0; i < numConnections; ++i) {
        	results[i] = SimpleConnectionPool.doQuery(cons.get(i), queries[i]);
        }
        return results;   	
    	
    }

}
