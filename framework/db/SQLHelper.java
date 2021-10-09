package dbexp.framework.db;

import java.util.HashMap;
import java.util.Map;

public class SQLHelper {
	private  DBMSType mDBMSType;
	private  Map<IsoLevel,String> mSQLCommands;
	private static final Map<IsoLevel, String> sqlserver = new HashMap<IsoLevel,String>(); 
	private static final Map<IsoLevel, String> sqlserver_mv = new HashMap<IsoLevel,String>();
	private static final Map<IsoLevel, String> postgres = new HashMap<IsoLevel,String>();
	private static final Map<IsoLevel, String> oracle = new HashMap<IsoLevel,String>();
	
	private static final Map<IsoLevel, String> mysql = new HashMap<IsoLevel,String>();
	private static final Map<IsoLevel, String> mysqlInnodbSSI = new HashMap<IsoLevel,String>();

	
	private SQLHelper(){};
	
	public static SQLHelper getInstance(DBMSType aDBMS) throws UnimplementedException {
		SQLHelper helper = new SQLHelper();
		helper.mDBMSType = aDBMS;
		switch (aDBMS) {
		case SQLServer5:
			helper.mSQLCommands = sqlserver;
			break;
		case SQLServer5_MV:
			helper.mSQLCommands = sqlserver_mv;
			break;
		case Postgres:
			helper.mSQLCommands = postgres;
			break;
		case Oracle10:
			helper.mSQLCommands = oracle;
			break;
		
		case MySQL:
			helper.mSQLCommands = mysql;
			break;

		case MySQL_Innodb_SSI:
			helper.mSQLCommands = mysqlInnodbSSI;
			break;
			
		default:
			throw new UnimplementedException("DBMS " + aDBMS + " has not been implemented");
		}
		return helper;
	}
	
	public String isolationLevel(IsoLevel il) throws UnimplementedException {
		String sql = mSQLCommands.get(il);
		if (sql == null) {
			throw new UnimplementedException("IsoLevel " + il + " implementation not available for DBMS " + mDBMSType );
		} else {
			return sql;
		}
	}
	static {
		//SQLServer5
		sqlserver.put(IsoLevel.RC, "SET TRANSACTION ISOLATION LEVEL READ COMMITTED;");
		sqlserver.put(IsoLevel.TwoPL, "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;");
		
		sqlserver_mv.put(IsoLevel.RC_MV, "SET TRANSACTION ISOLATION LEVEL READ COMMITTED;");
		sqlserver_mv.put(IsoLevel.SI, "SET TRANSACTION ISOLATION LEVEL SNAPSHOT;");
		sqlserver_mv.put(IsoLevel.TwoPL_MV, "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
	
		//Postgres
		postgres.put(IsoLevel.RC_MV, "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ COMMITTED;");
		postgres.put(IsoLevel.SI, "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE;");
		
		//Oracle
		oracle.put(IsoLevel.RC_MV, "alter session set isolation_level=READ COMMITTED");
		oracle.put(IsoLevel.SI, "alter session set isolation_level=SERIALIZABLE");

		/* MySQL
		 * Extract from MySQL 5.0 Reference Manual
		 * 
		 * SET [GLOBAL | SESSION] TRANSACTION ISOLATION LEVEL { READ UNCOMMITTED  | READ COMMITTED | REPEATABLE READ | SERIALIZABLE }
		 * {SERIALIZABLE SI}
		 * This statement sets the transaction isolation level globally, for the current session, or for the next transaction: 
		 * With the GLOBAL keyword, the statement sets the default transaction level globally for all subsequent sessions. Existing sessions are unaffected. 
		 * With the SESSION keyword, the statement sets the default transaction level for all subsequent transactions performed within the current session. 
		 * Without any SESSION or GLOBAL keyword, the statement sets the isolation level for the next (not started) transaction performed within the current session. 
		 */
		mysql.put(IsoLevel.RC, "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED");
		mysql.put(IsoLevel.TwoPL, "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE");
		mysql.put(IsoLevel.SI, "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ");
		
		/*
		mysqlInnodbSSI.put(IsoLevel.RC, "SET @@tx_isolation = 1");
		mysqlInnodbSSI.put(IsoLevel.RR, "SET @@tx_isolation = 2");
		mysqlInnodbSSI.put(IsoLevel.TwoPL, "SET @@tx_isolation = 3");
		mysqlInnodbSSI.put(IsoLevel.SI, "SET @@tx_isolation = 4");
		mysqlInnodbSSI.put(IsoLevel.SSI, "SET @@tx_isolation = 5");
		*/
		/*
		mysqlInnodbSSI.put(IsoLevel.RC, "SET @@tx_isolation = \"READ-COMMITTED\"");
		mysqlInnodbSSI.put(IsoLevel.RR, "SET @@tx_isolation = \"REPEATABLE-READ\"");
		mysqlInnodbSSI.put(IsoLevel.TwoPL, "SET @@tx_isolation = \"SERIALIZABLE\"");
		mysqlInnodbSSI.put(IsoLevel.SI, "SET @@tx_isolation = \"SNAPSHOT\"");
		mysqlInnodbSSI.put(IsoLevel.SSI, "SET @@tx_isolation = \"SERIALIZABLE-SNAPSHOT\"");
		*/
		mysqlInnodbSSI.put(IsoLevel.RU, "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
		mysqlInnodbSSI.put(IsoLevel.RC, "SET TRANSACTION ISOLATION LEVEL READ COMMITTED");
		mysqlInnodbSSI.put(IsoLevel.RR, "SET TRANSACTION ISOLATION LEVEL REPEATABLE READ");
		mysqlInnodbSSI.put(IsoLevel.TwoPL, "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
		mysqlInnodbSSI.put(IsoLevel.SI, "SET TRANSACTION ISOLATION LEVEL SNAPSHOT");
		mysqlInnodbSSI.put(IsoLevel.SSI, "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE SNAPSHOT");
	}
}
