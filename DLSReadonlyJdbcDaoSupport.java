package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.util.Assert;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.BusinessObjectBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.types.DeleteCodeType;
import gov.usda.fsa.fcao.flp.flpids.common.dao.dao.IDLSPreparedStatementSetter;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSDataNotFoundException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSPersistenceFatalException;

/**
 * @author chris.caruthers
 * @date Last Updated on April 19/2018
 */
public abstract class DLSReadonlyJdbcDaoSupport<BO extends BusinessObjectBase, Key extends Serializable>
		extends NamedParameterJdbcDaoSupport {

	protected static final Logger DEFAULT_LOGGER = LogManager
			.getLogger(DLSReadonlyJdbcDaoSupport.class);

	/*
	 * Constants used for retrieving the three required SQL Statements from
	 * within the Spring SQL configuration file for the concrete DAO.
	 */
	protected static final String RETRIEVE_BYKEY = "SqlSelectByKey";
	protected static final String RETRIEVE = "SQLSelectByFilter";
	protected static final String RETRIEVE_USING_DLO = "SQLSelectByFilterUsingDLO";

	protected static final String DATA_STATUS_CODES_PARAMETER = "dataStatusCodes";	
	
	/*
	 * This value is injected.
	 */
	protected Map<String, String> sqlQueries;

	@SuppressWarnings("unchecked")
	protected Key getPrimaryKey(BO referenceBO) {
		return (Key) referenceBO.getId();
	}

	protected List<BO> retrieve(BO referenceBO,
			String sqlStatement, Map<String, Object> parameters,
			RowMapper<BO> rowMapper) throws DLSPersistenceFatalException {

		List<BO> domainObjects = new ArrayList<BO>();

		try {
			MapSqlParameterSource paramSource = new MapSqlParameterSource();
			paramSource.addValues(parameters);

			/*
			 * Inject parameters that control records to return based on data
			 * status code.
			 */
			setDataStatusCodeParameters(referenceBO, paramSource);

			domainObjects = this.getNamedParameterJdbcTemplate().query(
					sqlStatement, paramSource, rowMapper);

		} catch (DataAccessException ex) {
			StringBuilder sb = new StringBuilder();
			sb.append("DLSSimpleJdbcDaoSupport.retrieve(): DataAccessException caught, convert and throw: [");
			sb.append(ex);
			sb.append("]");
			logger.error(sb.toString());
			throw new DLSPersistenceFatalException(sb.toString(), ex);
		}
		return domainObjects;
	}
	
	protected List<BO> retrieve(AgencyToken token, BO referenceBO,
			String sqlStatement, Map<String, Object> parameters,
			RowMapper<BO> rowMapper) throws DLSPersistenceFatalException {
		referenceBO.setAgencyToken(token);
		return retrieve(referenceBO, sqlStatement,parameters,rowMapper);
	}

	protected List<BO> retrieve(String baseSQLQuery,
			IDLSPreparedStatementSetter setter, RowMapper<BO> rowMapper)
			throws DLSPersistenceFatalException {

		List<BO> domainObjects = new ArrayList<BO>();

		try {
			domainObjects = getJdbcTemplate().query(
					setter.buildSQLStatement(baseSQLQuery), setter, rowMapper);
		} catch (DataAccessException ex) {
			StringBuilder sb = new StringBuilder();
			sb.append("DLSSimpleJdbcDaoSupport.retrieve(): DataAccessException caught, convert and throw: [");
			sb.append(ex);
			sb.append("]");
			logger.error(sb.toString());
			throw new DLSPersistenceFatalException(sb.toString(), ex);
		}

		return domainObjects;
	}

	protected List<BO> retrieve(String retrieveSql, List<Object> ids,
			RowMapper<BO> rowMapper) throws DLSPersistenceFatalException {
		List<BO> domainObjects = new ArrayList<BO>();
		String msg = "";
		try {
			Object[] args = new Object[ids.size()];
			for (int index = 0; index < ids.size(); index++) {
				args[index] = ids.get(index);
			}
			domainObjects = getJdbcTemplate().query(retrieveSql, args,
					rowMapper);
		} catch (DataAccessException e) {
			msg = "DLSSimpleJdbcDaoSupport.retrieve(): DataAccessException caught, convert and throw: ["
					+ e + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, e);
		}
		return domainObjects;
	}

	protected BO retrieveByKey(AgencyToken token, BO referenceBO,
			String sqlStatement, RowMapper<BO> rowMapper)
			throws DLSPersistenceFatalException {
		BO domainObject = null;
		Key primaryKey = getPrimaryKey(referenceBO);
		try {
			MapSqlParameterSource paramSource = new MapSqlParameterSource();
			paramSource.addValue("id", primaryKey);

			/*
			 * Inject parameters that control records to return based on data
			 * status code.
			 */
			setDataStatusCodeParameters(referenceBO, paramSource);

			domainObject = (BO) this.getNamedParameterJdbcTemplate()
					.queryForObject(sqlStatement, paramSource, rowMapper);

		} catch (EmptyResultDataAccessException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Record not found for " + primaryKey);
			}

		} catch (DataAccessException ex) {
			StringBuilder sb = new StringBuilder();
			sb.append("DLSSimpleJdbcDaoSupport.retrieveByKey(): DataAccessException caught, convert and throw: [");
			sb.append(ex);
			sb.append("]");
			logger.error(sb.toString());
			throw new DLSPersistenceFatalException(sb.toString(), ex);
		}
		return domainObject;
	}

	protected BO retrieveByKey(BO referenceBO, String sqlStatement,
			RowMapper<BO> rowMapper) throws DLSPersistenceFatalException {
		return retrieveByKey(referenceBO.getAgencyToken(), referenceBO,
				sqlStatement, rowMapper);
	}

	protected List<Object> getRetrieveIdList() {
		List<Object> dummyIds = new ArrayList<Object>();
		return dummyIds;
	}

	/**
	 * Return SQL Statement using the passed key value.<br>
	 * <br>
	 * 
	 * @param key
	 *            identifies an SQL Statement within the Spring SQL
	 *            configuration file.
	 * @return SQL Statement string.
	 */
	protected String getSql(String key) {
		Assert.notNull(key, key + " is null.");
		String sql = sqlQueries.get(key);
		Assert.notNull(sql, "Query for " + key
				+ " is null/not defined. Check config");
		if (DEFAULT_LOGGER.isDebugEnabled()) {
			DEFAULT_LOGGER.debug("Returning sql for " + key + ", sql = " + sql);
		}
		return sql;
	}

	/*
	 * Method required for creating 'customerDao' bean.
	 */
	@Required
	public void setSqlQueries(Map<String, String> sqlQueryMap) {
		sqlQueries = sqlQueryMap;
	}

	/*
	 * These methods should be overridden in the concrete DAO class.
	 */
	protected abstract RowMapper<BO> getRowMapper();

	protected Map<String, Object> getRetrieveParameterMap(BO referenceBO) {
		return new HashMap<String, Object>();
	}

	/**
	 * Active Delete Code Type List
	 */
	public static List<String> ACTIVE_DELETE_CODES;

	static {
		ACTIVE_DELETE_CODES = new ArrayList<String>();
		ACTIVE_DELETE_CODES.add(DeleteCodeType.Active.getDeleteCode());
	}

	/**
	 * Inactive Delete Code Type List
	 */
	public static List<String> INACTIVE_DELETE_CODES;

	static {
		INACTIVE_DELETE_CODES = new ArrayList<String>();
		INACTIVE_DELETE_CODES.add(DeleteCodeType.Inactive.getDeleteCode());
	}

	/**
	 * Inactive Delete Code Type List
	 */
	public static List<String> NON_DELETE_DELETE_CODES;

	static {
		NON_DELETE_DELETE_CODES = new ArrayList<String>();
		NON_DELETE_DELETE_CODES.add(DeleteCodeType.Active.getDeleteCode());
		NON_DELETE_DELETE_CODES.add(DeleteCodeType.Inactive.getDeleteCode());
	}

	/**
	 * Inactive Delete Code Type List
	 */
	public static List<String> DELETE_DELETE_CODES;

	static {
		DELETE_DELETE_CODES = new ArrayList<String>();
		DELETE_DELETE_CODES.add(DeleteCodeType.Delete.getDeleteCode());
	}

	protected void setDataStatusCodeParameters(BO referenceBO,
			MapSqlParameterSource paramSource) {
		/*
		 * Inject parameters that control records to return based on data status
		 * code.
		 */
		paramSource.addValue(DATA_STATUS_CODES_PARAMETER,
				getDataStatusCodeParameterValues(referenceBO));
	}

	protected List<String> getDataStatusCodeParameterValues(BO referenceBO) {
		return ACTIVE_DELETE_CODES;
	}

///////////****BELOW METHOD ARE DEPRECATED****///////////////////////////
	@SuppressWarnings("unchecked")
	@Deprecated
	protected Key getPrimaryKey() {
		return (Key) getBusinessObject().getId();
	}

	@Deprecated
	protected Map<String, Object> getRetrieveParameterMap() {
		return getRetrieveParameterMap(getBusinessObject());
	}

	
	@Deprecated
	protected List<BO> retrieve(String sqlStatement,
			Map<String, Object> parameters, RowMapper<BO> rowMapper)
			throws DLSPersistenceFatalException {
		return retrieve(getBusinessObject().getAgencyToken(),
				getBusinessObject(), sqlStatement, parameters, rowMapper);
	}

	@Deprecated
	public void setBusinessObject(BO bo){
		
	}
	
	@Deprecated
	protected  BO getBusinessObject(){
		throw new IllegalAccessError("getBusinessObject() is deprecated...");
	}

	
	@Deprecated
	public BO retrieveByKey() throws DLSPersistenceFatalException,
			DLSDataNotFoundException{
		throw new IllegalAccessError("retrieveByKey(s) is deprecated...");
	}
	@Deprecated
	public java.util.List<BO> retrieve() throws DLSPersistenceFatalException{
		throw new IllegalAccessError("retrieve() is deprecated...");
	}
	@Deprecated
	public java.util.List<BO> retrieve(AgencyToken token)
			throws DLSPersistenceFatalException{
		throw new IllegalAccessError("retrieve(token) is deprecated...");
	}
	@Deprecated
	public void setId(Key id){
		throw new IllegalAccessError("setId(id) is deprecated...");
	}
}
