package gov.usda.fsa.fcao.flp.flpids.common.dao.dao;

import org.springframework.jdbc.core.PreparedStatementSetter;

public interface IDLSPreparedStatementSetter extends PreparedStatementSetter {
	
	/**
	 * Appends custom Where clause to the base SQL Statement passed in 
	 * as an argument.  The implementing class will need to create the 
	 * Where clause based on which values have been provided.<br><br>
	 * 
	 * @param baseSQLStatement SQL Statement without Where clause.
	 * @return SQL Statement with Where clause
	 */
	public String buildSQLStatement(String baseSQLStatement);

}
