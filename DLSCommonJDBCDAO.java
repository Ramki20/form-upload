package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.base;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.BusinessObjectBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.types.DeleteCodeType;
import gov.usda.fsa.fcao.flp.flpids.common.dao.dao.IDataAccessObject;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSDataNotFoundException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSPersistenceFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSPersistenceOptimisticLockStopException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
/**
 * @author chris.caruthers
 * @date Last Updated on April 19/2018
 */
public abstract class DLSCommonJDBCDAO<BO extends BusinessObjectBase, Key extends Serializable>
		extends DLSSimpleJdbcDaoSupport<BO, Key> implements
		IDataAccessObject<BO, Key> {

	protected static final String INSERT = "SQLInsert";
	protected static final String UPDATE = "SQLUpdate";
	protected static final String BATCH_INSERT = "SQLBatchInsert";
	protected static final String BATCH_UPDATE = "SQLBatchUpdate";
	protected static final String DELETE = "SQLDelete";
	public static final int BATCH_UPDATE_SIZE = 500;

	private boolean lightWeightInd;

	
/*
 * default implementation
 */
	@Override
	protected void setId(BO bo, Key id){
		if(id instanceof Integer){
			bo.setId((Integer)id);
		}
		else if(id instanceof Long){
			bo.setId((Integer)id);
		}
	}	
	/*
	 * Constructor
	 */
	protected DLSCommonJDBCDAO() {
		setLightWeightInd(Boolean.FALSE);
	}

	/**
	 * @return state of the Light Weight Indicator attribute.<br>
	 * <br>
	 */
	public boolean isLightWeightInd() {
		return lightWeightInd;
	}

	/**
	 * Sets the state of the Light Weight Indicator.<br>
	 * <br>
	 * 
	 * @see gov.usda.fsa.fcao.flp.flpids.common.dao.dao.IDataAccessObject#setLightWeightInd(boolean)
	 */
	public void setLightWeightInd(boolean lightWeightInd) {
		this.lightWeightInd = lightWeightInd;
	}

	

	/**
	 * Inserts reference to the passed Agency Token into each BO object in the
	 * passed list.<br>
	 * <br>
	 * 
	 * @param boList
	 * @param token
	 */
	protected void setAgencyToken(List<BO> boList,
			gov.usda.fsa.common.base.AgencyToken token) {
		for (BO object : boList) {
			if (object != null) {
				object.setAgencyToken(token);
			}
		}
	}

	

	
	
	public  List<BO> retrieveUsingDLO(AgencyToken token, BO refereceBO,  String sqlString)
			throws DLSPersistenceFatalException {
		return retrieve(token, refereceBO, sqlString);
	}
	
	public  List<BO> retrieve(AgencyToken token, BO referenceBO)
			throws DLSPersistenceFatalException {
		return retrieve(token, referenceBO, this.getSql(RETRIEVE));
	}
	
	public  List<BO> retrieve(AgencyToken token, BO referenceBO, String sqlString)
			throws DLSPersistenceFatalException {
		List<BO> boList = new ArrayList<BO>();
		try {			
			boList = retrieve(token, referenceBO, sqlString, getRetrieveParameterMap(referenceBO),
					getRowMapper());
			validate(boList);
			setAgencyToken(boList, token);
			if (isLightWeightInd()) {
				setLightWeightInd(Boolean.FALSE);
			} else {
				populateChildren(token,boList);
			}
			return boList;
		} catch (DLSPersistenceFatalException ex) {
			logger.error("Error retrieving Business Object");
			throw ex;
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder();
			sb
					.append("DLSCommonJDBCDAO.retrieve(): Exception caught, convert and throw: [");
			sb.append(ex);
			sb.append("]");
			logger.error(sb.toString());
			throw new DLSPersistenceFatalException(sb.toString(), ex);
		} finally {
			if(logger.isDebugEnabled()){
				logger
					.debug("OUT: DLSCommonJDBCDAO.retrieve(). poFacadeListSize = ["
							+ boList.size() + "]");
			}
		}
	}
	
	public BO retrieveByKey(AgencyToken token, BO referenceObj)
			throws DLSPersistenceFatalException, DLSDataNotFoundException {
		BO businessObject = null;
		businessObject = super.retrieveByKey(token,referenceObj, this.getSql(RETRIEVE_BYKEY), 
				getRowMapper());

		if (businessObject == null) {
			StringBuilder sb = new StringBuilder();
			sb
					.append("DLSCommonJDBCDAO.retrieveByKey(): No data found for key: [");
			sb.append(this.getPrimaryKey(referenceObj));
			sb.append("]");
			logger.error(sb.toString());
			throw new DLSDataNotFoundException(sb.toString());
		}
		try {			
			businessObject.setAgencyToken(token);
			if (isLightWeightInd()) {
				setLightWeightInd(false);
			} else {
				List<BO> boList = new ArrayList<BO>();
				boList.add(businessObject);
				/*
				 * Calls the validate method on the concrete DAO class.
				 */
				validate(boList);
				populateChildren(token,boList);
			}
			return businessObject;
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder();
			sb
					.append("DLSCommonJDBCDAO.retrieveByKey(): Exception caught, convert and throw: [");
			sb.append(ex);
			sb.append("]");
			logger.error(sb.toString());
			throw new DLSPersistenceFatalException(sb.toString(), ex);
		}
	}

	

	public BO insert(AgencyToken token, BO source, String sqlKey,
			Boolean aArithabortFlag) throws DLSPersistenceFatalException {
		setArithabortFlag(aArithabortFlag);
		return insert(token, source, sqlKey);
	}


	
	public BO insert(AgencyToken token, BO source, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		setArithabortFlag(aArithabortFlag);
		return insert(token, source);
	}


	public BO insert(AgencyToken token, BO source)
			throws DLSPersistenceFatalException {
		return insert(token, source, INSERT);
	}
	
	public BO insert(AgencyToken token, BO source, String sqlKey)
			throws DLSPersistenceFatalException {
		setDefaultFieldsBeforeInsertion(token, source);
		BO savedBusinessObject = source;
		this.create(getSql(sqlKey), savedBusinessObject);
		savedBusinessObject.setDirty(Boolean.FALSE);
		return savedBusinessObject;
	}

	/**
	 * Inserts multiple records as a batch..
	 * 
	 * @param List
	 *            <BOs>
	 * 
	 * @throws DLSPersistenceFatalException
	 * @return BO list with Ids.
	 */
	public List<BO> batchInsert(List<BO> sourceList)
			throws DLSPersistenceFatalException {
		 List<BO> resultList = new ArrayList<BO>();
		 for (BO bo : sourceList) {
			 BO result = insert(bo.getAgencyToken(), bo);
			 resultList.add(result);
		 }
		 return resultList;
	}


	public void update(AgencyToken token, BO source, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		setArithabortFlag(aArithabortFlag);
		update(token, source);
	}
	
	

	
	public void update(AgencyToken token, BO source)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		update(token, source, UPDATE);
	}

	public void update(AgencyToken token, BO source, String sqlKey)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		setDefaultFieldsBeforeUpdate(token, source);
		update(getSql(sqlKey), source);
		source.setDirty(Boolean.FALSE);
	}

	@Override
	public void remove(AgencyToken token, BO source) throws DLSPersistenceFatalException,
		DLSPersistenceOptimisticLockStopException {
		if(token != null){
			source.setLastChangeUserName(token.getUserIdentifier());
		}
		remove(source, DELETE);
	}
	
	public void remove(AgencyToken token, BO source, String sqlKey)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		if (token != null) {
			source.setLastChangeUserName(token.getUserIdentifier());
		}
		remove(source, sqlKey);
	}
	

	@Override
	public void remove(BO source) throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		remove(source, DELETE);
	}

	

	public void remove(BO source, String sqlKey)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		delete(getSql(sqlKey), source);
	}


	public void deleteLogically(AgencyToken token, BO source,
			Boolean arithArbortFlag) throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		setArithabortFlag(arithArbortFlag);
		setDataStatusCodeValue(source,DeleteCodeType.Delete.getDeleteCode());	
		if(token != null){
			update(token,  source, UPDATE);
		}else{
			update(getSql(UPDATE),  source);
		}
	}


	
	public void copyNonNullData(BO from, BO to) {
		if(from != null && to != null){
			to.copy(from);
		}
	}


	protected void setDefaultFieldsBeforeInsertion(AgencyToken token, BO source)
			throws DLSPersistenceFatalException {
		String msg = "";
		BusinessObjectBase businessObject = source;
		Date now = Calendar.getInstance().getTime();
		businessObject.setLastChangeDate(now);
		businessObject.setCreationDate(now);
		
		if (token != null && token.getUserIdentifier() != null &&
				token.getUserIdentifier().trim().length() > 0) {
			businessObject.setCreationUserName(token.getUserIdentifier());
			businessObject.setLastChangeUserName(token.getUserIdentifier());
		}else{
			businessObject.setCreationUserName("Unknown from JDBC Insert");
			businessObject.setLastChangeUserName("Unknown from JDBC Insert");
		}
		/*
		 * if the underlying table supports dataStatusCode field (for logical
		 * deletion), set the field to 'A' (active) for new object String msg =
		 * "";
		 */
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = { String.class };
			Method dataStatusCodeSetter = businessObject.getClass().getMethod(
					"setDataStatusCode", paramTypes);

			if (dataStatusCodeSetter != null) {
				if(logger.isDebugEnabled()){
					logger.debug("DLSCommonJDBCDAo:setDefaultFieldsBeforeInsertion() is setting dataStatusCode to 'A'");
				}
				String dataActiveMark = DeleteCodeType.Active.getDeleteCode();
				Object[] paramInput = { dataActiveMark };
				dataStatusCodeSetter.invoke(businessObject, paramInput);
			}
		} catch (NoSuchMethodException noFuncExc) {
			msg = "DLSCommonJDBCDAO.setDefaultFieldsBeforeInsertion(): No dataStatusCode logic, no need to set dataStatusCode.";
			logger.info(msg);
		} catch (Exception exc) {
			msg = "DLSCommonJDBCDAO.setDefaultFieldsBeforeInsertion(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
		businessObject.setDirty(Boolean.TRUE);
	}


/**
 * do not setup last_change_date as it is used to check for optimistic locking. 
 * The SQL query should update it from CURRENT_TIMESTAMP
 */
	protected void setDefaultFieldsBeforeUpdate(AgencyToken token, BO source)
			throws DLSPersistenceFatalException {
		BusinessObjectBase businessObject = source;
		if (token != null && token.getUserIdentifier() != null &&
				token.getUserIdentifier().trim().length() > 0) {
			businessObject.setLastChangeUserName(token.getUserIdentifier());
		}else{
			businessObject.setLastChangeUserName("Unknown from JDBC Update");
		}
	}

	protected void setArithabortFlag(Boolean aArithabortFlag) {
		if (aArithabortFlag) {
			try {
				java.sql.Connection lConnection = this.getConnection();
				java.sql.Statement lStmt = lConnection.createStatement();
				lStmt.execute("SET ARITHABORT ON");
			} catch (SQLException e) {
				logger
						.error(
								"Failed to execute 'SET ARITHABORT ON' on insert(true)",
								e);
			}
		}
	}

	/*
	 * If these behaviors are required, override method in concrete DAO class.
	 */
	protected void populateChildren(AgencyToken token, List<BO> boList)
			throws DLSPersistenceFatalException, DLSDataNotFoundException {
	}

	protected void validate(List<BO> list) throws DLSDataNotFoundException,
			DLSPersistenceFatalException {
	}

	
	
	public void insertBatch(AgencyToken token, 
			List<BO> savedBOs, String sqlKey)
			throws DLSPersistenceFatalException {
		insertBatch(token, savedBOs, sqlKey,false);	
	}



	
	public void updateBatch(AgencyToken token, 
			List<BO> savedBOs,String sqlKey)
			throws DLSPersistenceFatalException {
		updateBatch(token, savedBOs,sqlKey,Boolean.TRUE);
	}
	
	public void insertBatch(AgencyToken token, 
			List<BO> savedBOs, String sqlKey, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {

		setArithabortFlag(aArithabortFlag);
		List<SqlParameterSource> parameters = new ArrayList<SqlParameterSource>();
		for (BO bo : savedBOs) {;
			setDefaultFieldsBeforeInsertion(token,bo);
			parameters.add(new BeanPropertySqlParameterSource(bo));
		}
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
//		SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(this
//				.getDataSource());
		int returnedValues[] = jdbcTemplate.batchUpdate(getSql(sqlKey),
				parameters.toArray(new SqlParameterSource[0]));

		Boolean validInsert = Boolean.TRUE;
		for (int i = 0; i < returnedValues.length; i++) {
			int returnValue = returnedValues[i];
			if (returnValue == 0) {
				validInsert = Boolean.FALSE;
				break;
			}
		}

		if (!validInsert) {
			throw new DLSPersistenceFatalException(
					"SQL Batch Insert statement did not execute");
		}
	}
	
	public int updateBatch(AgencyToken token, 
			List<BO> savedBOs,String sqlKey, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		if(savedBOs == null || savedBOs.isEmpty()) {
			return (0);
		}
		setArithabortFlag(aArithabortFlag);
		List<SqlParameterSource> parameters = new ArrayList<SqlParameterSource>();
		for (BO bo : savedBOs) {
			setDefaultFieldsBeforeUpdate(token,bo);
			parameters.add(new BeanPropertySqlParameterSource(bo));
		}
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
		int returnedValues[] = jdbcTemplate.batchUpdate(getSql(sqlKey),
				parameters.toArray(new SqlParameterSource[0]));
		int updatedCount = 0;
		for (int i = 0; i < returnedValues.length; i++) {
			int returnValue = returnedValues[i];
			if (returnValue > 0) {
				updatedCount++;
			}
		}
		return updatedCount;
	}

	@Override
	protected List<String> getDataStatusCodeParameterValues(BO referenceBO){
		if(referenceBO != null){
			String statusCodeValue = getDataStatusCodeValue(referenceBO);
			if(DeleteCodeType.Delete.getDeleteCode().equalsIgnoreCase(statusCodeValue)){
				return DELETE_DELETE_CODES;
			}
			if(DeleteCodeType.Inactive.getDeleteCode().equalsIgnoreCase(statusCodeValue)){
				return INACTIVE_DELETE_CODES;
			}
		}
		return super.getDataStatusCodeParameterValues(referenceBO);
	}
	
	
	protected void setDataStatusCodeValue(BO businessObject, String value)
			throws DLSPersistenceFatalException {
		String dataDeleteMark = value;
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = { String.class };
			Method dataStatusCodeSetter = businessObject.getClass().getMethod(
					"setDataStatusCode", paramTypes);

			if (dataStatusCodeSetter != null) {
				if(StringUtil.isEmptyString(dataDeleteMark)){
					dataDeleteMark = DeleteCodeType.Delete.getDeleteCode();
				}
				if(logger.isDebugEnabled()){
					logger.debug("setting dataStatusCode to " + dataDeleteMark);
				}
				Object[] paramInput = { dataDeleteMark };
				dataStatusCodeSetter.invoke(businessObject, paramInput);
			}
		} catch (NoSuchMethodException noFuncExc) {
			String message = "DLSCommonJDBCDAO.setDataStatusCodeValue(): object does not support logic deletion ["
					+ businessObject.getClass().getName() + "]";
			logger.info(message);
			//throw new DLSPersistenceFatalException(msg, noFuncExc);
		} catch (Exception exc) {
			String message = "DLSCommonJDBCDAO.setDataStatusCodeValue(): check if class has accessible setter for dataStatusCode ["
					+ businessObject.getClass().getName() + "]";
			logger.error(message);
			throw new DLSPersistenceFatalException(message, exc);
		}
	}

	private String getDataStatusCodeValue(BO businessObject) {
		String datsStatusCodeValue = "";
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = {  };
			Method dataStatusCodeSetter = businessObject.getClass().getMethod(
					"getDataStatusCode", paramTypes);

			if (dataStatusCodeSetter != null) {
				logger.debug("getting dataStatusCode from BO'");
				datsStatusCodeValue = (String)dataStatusCodeSetter.invoke(businessObject);
			}
		} catch (NoSuchMethodException noFuncExc) {
			if(logger.isDebugEnabled()) {
				String message = "DLSCommonJDBCDAO.getDataStatusCodeValue(): object does not  have getDatStatusCode() method ["
						+ businessObject.getClass().getName() + "]";
				logger.debug(message);
			}
			//throw new DLSPersistenceFatalException(msg, noFuncExc);
		} catch (Exception exc) {
			String message = "DLSCommonJDBCDAO.getDataStatusCodeValue(): check if class has accessible getter for dataStatusCode ["
					+ businessObject.getClass().getName() + "]";
			logger.error(message);
		}
		return datsStatusCodeValue;
	}
	
///////////****BELOW METHOD ARE DEPRECATED****///////////////////////////	
	
	@Deprecated
	private BO businessObject;

	@Deprecated
	private List<BO> businessObjects;

	@Deprecated
	public void setBusinessObjects(List<BO> businessObjects) {
		this.businessObjects = businessObjects;
	}

	@Deprecated
	public List<BO> getBusinessObjects() {
		return businessObjects;
	}

	@Deprecated
	protected BO getBusinessObject() {
		return this.businessObject;
	}

	@Deprecated
	public void setBusinessObject(BO bo) {
		this.businessObject = bo;
	}
	@Deprecated
	@Override
	public void setId(Key id){
		
	}
	
	@Deprecated
	public List<BO> retrieveUsingDLO() throws DLSPersistenceFatalException {
		return retrieveUsingDLO(getAgencyToken(),
				getBusinessObject(), 
				this.getSql(RETRIEVE_USING_DLO));
	}

	@Deprecated
	public List<BO> retrieveUsingDLO(AgencyToken token)
			throws DLSPersistenceFatalException {
		return retrieveUsingDLO(token, getBusinessObject(), 
				this.getSql(RETRIEVE_USING_DLO));
	}
	
	@Deprecated
	public List<BO> retrieve() throws DLSPersistenceFatalException {
		return retrieve(getAgencyToken());
	}

	@Deprecated
	public List<BO> retrieve(AgencyToken token)
			throws DLSPersistenceFatalException {
		return retrieve(token, getBusinessObject(),this.getSql(RETRIEVE));
	}

	@Deprecated
	protected List<BO> retrieve(String sqlString)
			throws DLSPersistenceFatalException {
		return retrieve(getAgencyToken(),
				getBusinessObject(),sqlString);
	}

	@Deprecated
	protected List<BO> retrieve(AgencyToken token, String sqlString)
			throws DLSPersistenceFatalException {
		return retrieve(token, getBusinessObject(), sqlString);
	}

	@Deprecated
	public BO retrieveByKey() throws DLSPersistenceFatalException,
		DLSDataNotFoundException {
		return retrieveByKey(getAgencyToken(), getBusinessObject());
	}

	/**
	 * 
	 * @param sqlKey
	 * @param aArithabortFlag
	 * @return Updated BO
	 * @throws DLSPersistenceFatalException
	 */
	@Deprecated
	public BO insert(String sqlKey, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		return insert(getAgencyToken(),
				getBusinessObject(), sqlKey);
	}
	@Deprecated
	public BO insert(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		return insert(getAgencyToken(),
				getBusinessObject(), aArithabortFlag);
	}
	@Deprecated
	public BO insert() throws DLSPersistenceFatalException {
		return insert(getAgencyToken(),
				getBusinessObject(), INSERT);
	}


	@Deprecated
	public BO insert(String sqlKey) throws DLSPersistenceFatalException {
		return insert(getAgencyToken(),
				getBusinessObject(), sqlKey);
	}
	

	@Deprecated
	public void update(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		update(getAgencyToken(), getBusinessObject());
	}


	@Deprecated
	public void update(String sqlKey) throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		update(getAgencyToken(), getBusinessObject(),
				sqlKey);
	}
	

	@Deprecated
	public void update() throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		update(getAgencyToken(), getBusinessObject(),
				UPDATE);
	}


	@Deprecated
	protected void setDefaultFieldsBeforeInsertion()
			throws DLSPersistenceFatalException {
		setDefaultFieldsBeforeInsertion(getAgencyToken(),
				getBusinessObject());
	}
	@Deprecated
	public void remove() throws DLSPersistenceFatalException,
	DLSPersistenceOptimisticLockStopException {
		remove(getBusinessObject());
	}
	@Deprecated
	public void remove(String sqlKey) throws DLSPersistenceFatalException,
		DLSPersistenceOptimisticLockStopException {
	delete(getSql(sqlKey), getBusinessObject());
	}
	@Deprecated
	public void deleteLogically(Boolean aArithArbortFlag)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		deleteLogically(getAgencyToken(),
				getBusinessObject(), aArithArbortFlag);
	}
	
	@Deprecated
	public void batchUpdate() throws DLSPersistenceFatalException {

		updateBatch(BATCH_UPDATE, Boolean.TRUE);
	}

	@Deprecated
	public void batchUpdate(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {

		updateBatch(BATCH_UPDATE, aArithabortFlag);
	}
	@Deprecated
	public void updateBatch(String sqlKey, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		updateBatch(null, getBusinessObjects(),sqlKey,aArithabortFlag);
	}
	
	@Deprecated
	public void batchInsert() throws DLSPersistenceFatalException {
		insertBatch(BATCH_INSERT, Boolean.TRUE);
	}

	@Deprecated
	public void batchInsert(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		insertBatch(BATCH_INSERT, aArithabortFlag);
	}
	@Deprecated
	public void insertBatch(String sqlKey, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		insertBatch(null, getBusinessObjects(), sqlKey,aArithabortFlag);
	}
	
	private AgencyToken getAgencyToken(){
		return (getBusinessObject() == null? null: getBusinessObject().getAgencyToken());
	}
	
	@Deprecated
	protected void setDefaultFieldsBeforeUpdate()
			throws DLSPersistenceFatalException {
		setDefaultFieldsBeforeUpdate(getAgencyToken(),
				getBusinessObject());
	}

	@Deprecated
	protected void populateChildren(List<BO> boList)
			throws DLSPersistenceFatalException, DLSDataNotFoundException {
		if(!boList.isEmpty()){
			populateChildren(boList.get(0).getAgencyToken(), boList);
		}
	}
}
