package gov.usda.fsa.fcao.flp.flpids.common.dao.dao;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Enhanced interface that supports both legacy PreparedStatementSetter approach
 * and modern named parameter approach for SQL injection protection.
 */
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
	
	/**
	 * NEW METHOD: Builds SQL statement using named parameters instead of positional parameters.
	 * This method should create SQL with named parameter placeholders (e.g., :paramName)
	 * instead of positional placeholders (?).
	 * 
	 * @param baseSQLStatement SQL Statement without Where clause.
	 * @return SQL Statement with Where clause using named parameters
	 */
	public String buildNamedParameterSQLStatement(String baseSQLStatement);
	
	/**
	 * NEW METHOD: Provides the parameter values for named parameter binding.
	 * This replaces the PreparedStatementSetter.setValues() approach with
	 * a safer named parameter approach.
	 * 
	 * @return SqlParameterSource containing named parameters and their values
	 */
	public SqlParameterSource getNamedParameters();

}