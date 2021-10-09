package dbexp.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.progressmonitor.LightWeightLogger;
import dbexp.framework.progressmonitor.Logger;


/**
 * This class is used to create, test, open, close connections
 * NOTE: All connections are set to Auto Commit so you will
 * have to to NOT auto commit if you want to commit or rollback DML statements explicitly. 
 * DDL statements may be run in their own transaction automatically
 * depending on the database.
 * <p>
 * The methods of this class are useful for setting up or tearing
 * down experiments.
 * <p>
 * WARNING: The methods of this should *NOT* be called inside 
 * threaded performance evaluation code (i.e. during the "MEASUREMENT" phase of
 * thread execution) as methods of this class could use synchronization and/or 
 * shared/static objects and also log to i/o.
 * 
 * @author Shirley Goldrei
 *
 */
public class ConnectionManager {
	private static final Logger LOG = new LightWeightLogger(ConnectionManager.class.getName());
	
	private final ConnectionConfiguration mConfiguration;
	private Connection mConn = null;
	
	private boolean mDriverLoaded = false;
	
	public ConnectionManager( ConnectionConfiguration aConfiguration ) {
		mConfiguration = aConfiguration;
	}
	
	private void loadDriver() throws ConnectionException {
		try {   
	           /* load the JDBC driver */
	           Class.forName(mConfiguration.getDriverName());
	           mDriverLoaded = true;
	       } catch (ClassNotFoundException e)  {  
	    	   /* error handling when no JDBC class is found */
	           LOG.error(e);
	           throw new ConnectionException(e);
	       }
	}
	
    /**
     * Establishes a connection to a database.
     * The connection parameters are read from the ConnectionConfiguration instance.
     * This method could be slow since it lazily loads the JDBC driver and creating
     * connection objects in notoriously slow. Don't call this method within 
     * your performance evaluation code. If you want to include creating a connection
     * consider reimplementing this method somehow.
     * @returns  true   on success and then the instance variable <code>mConn</code>
     *                  holds an open connection to the database.
     *           false  otherwise
     */ 

    public boolean connectToDatabase () {
    	// lazily load the driver    	
    	synchronized (this) {
			try {
				if (!mDriverLoaded) {
					loadDriver();
				}
			} catch (ConnectionException e) {
				LOG.error("Can't create connection to database because failed to load driver");
			}
		}
    	
		try 
	       {   
	           /* connect to the database */
	           mConn = DriverManager.getConnection(
	        		   	mConfiguration.getConnectionURL(),
	        		   	mConfiguration.getUserName(),
	        		   	mConfiguration.getPassword());
	           return true;
	       } catch (SQLException sql_ex) {  
	           /* error handling */
	           LOG.error(sql_ex);
	
	           return false;
	       }
    }

    /**
     * Just test the database connection.
     */

    public void testConnection () {
       if ( connectToDatabase() )
       {
          LOG.info("You successfully connected to Database Server.");
          try {
             mConn.close(); // close the connection again after usage! 
          } catch (SQLException sql_ex) {  /* error handling */
             LOG.error(sql_ex);
          }
       } else
          LOG.error("Oops - something went wrong in trying to connect to the database server.");
    }    

    public void closeConnection() {
        //close connection
        try {
            if (mConn != null) {
                LOG.debug("Closing Connection...\n");
            	mConn.close();
            	mConn = null;
            }

        } catch (SQLException e) {
        	LOG.error(e);
        }
    }
    
    public Connection getConnection() {
    	return mConn;
    }
    
    public ConnectionConfiguration getConfiguration() {
    	return mConfiguration;
    }
    
    public void setIsolationLevel( IsoLevel anIsolationLevel ) throws SQLException, UnimplementedException {
    	//System.out.println("*** Set " + anIsolationLevel.name());
    	//System.out.println("*** " + SQLHelper.getInstance(mConfiguration.getDBMSType()).isolationLevel(anIsolationLevel));
    	executeUpdate( SQLHelper.getInstance(mConfiguration.getDBMSType()).isolationLevel(anIsolationLevel) );
    	//System.out.println( "*** Isolation leverl = " + getIsolationName() );
    }
    
    public String getCurretnIsolationName() {
    	
		ResultSet resultSet = getQueryResultSet("SELECT @@tx_isolation");
		try {			
			if (resultSet.next())
				return resultSet.getString(1);
			
		} catch (SQLException e) {
			LOG.error(e);
		}
    	return "";
    }
    
    public int getQueryErrorCode(String sqlText) {
    	if (mConn == null) {
    		LOG.debug("Database disconnected.");
    		return -1;
    	}
    	try {
    		//System.out.println(getIsolationName());
    		mConn.setAutoCommit(false);
    		Statement statement = mConn.createStatement();
    		ResultSet rs = statement.executeQuery(sqlText);
    		while (rs.next()) {}
    		statement.close();
			mConn.commit();
			
			return 0;
		} catch (SQLException e) {
			//System.out.println("***QUERY ERROR " + e.getErrorCode());
			//System.out.print("*");
			return e.getErrorCode();
		}
    }
    
    public int getUpdateErrorCode(String sqlText) {
    	if (mConn == null) {
    		LOG.debug("Database disconnected.");
    		return -1;
    	}
    	try {
    		//System.out.println(getIsolationName());
    		mConn.setAutoCommit(false);
    		Statement statement = mConn.createStatement();
			statement.executeUpdate(sqlText);
			statement.close();
			mConn.commit();
			return 0;
		} catch (SQLException e) {
			//System.out.println("###UPDATE ERROR " + e.getErrorCode());
			//System.out.print("#");
			return e.getErrorCode();
		}
    }

    public int getBatchUpdateErrorCode(String[] sqlBatch) {
    	if (mConn == null) {
    		LOG.debug("Database disconnected.");
    		return -1;
    	}
    	
    	try {
    		boolean autoCommit = mConn.getAutoCommit();
    		mConn.setAutoCommit(false);
    		Statement statement = mConn.createStatement();
    		for (int i = 0; i < sqlBatch.length; i++) {
    			if (sqlBatch[i].trim().length() > 0)
    				statement.addBatch(sqlBatch[i]);
    		}
    		statement.executeBatch();
    		mConn.commit();
    		statement.close();
    		mConn.setAutoCommit(autoCommit);
			return 0;
		} catch (SQLException e) {
			LOG.info(e.getLocalizedMessage());
			if (mConn != null) {
				try {
					mConn.rollback();
				} catch (SQLException e1) {
					LOG.info(e1.getLocalizedMessage());
					return e.getErrorCode();
				}
			}
			return e.getErrorCode();
		}
    }
    
    public ResultSet getQueryResultSet(String sqlText) {
    	if (mConn == null) {
    		LOG.debug("Database disconnected.");
    		return null;
    	}
    	try {
    		//mConn.setAutoCommit(false);
    		Statement statement = mConn.createStatement();
    		ResultSet result = statement.executeQuery(sqlText);
			//mConn.commit();
			return result;
		} catch (SQLException e) {
			//LOG.info("ERROR CODE: " + e.getErrorCode());
			return null;
		}
    }
    
    public boolean executeUpdate(String sqlText) {
    	return getUpdateErrorCode(sqlText) == 0;
    }

    public boolean executeBatch(String[] sqlBatch) {
    	return getBatchUpdateErrorCode(sqlBatch) == 0;
    }
    
}
