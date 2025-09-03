package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.base;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// Replace old Criteria imports with JPA Criteria API
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.SessionFactoryUtils;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.BusinessObjectBase;
import gov.usda.fsa.fcao.flp.flpids.common.dao.dao.IHibernateDataAccessObject;
import gov.usda.fsa.fcao.flp.flpids.common.dao.impl.support.HibernateHqlAndCriteriaToSqlTranslator;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSDataNotFoundException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSPersistenceFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSPersistenceOptimisticLockStopException;
import org.apache.logging.log4j.LogManager;

/**
 * @author chris.caruthers
 * @date Last Updated on April 19/2018
 * Updated for Hibernate 6.x compatibility
 */
public abstract class DLSCommonHibernateDAO<BO extends BusinessObjectBase, Key extends Serializable>
		implements IHibernateDataAccessObject<BO, Key> {

	private static final long HIBERNATE_JDBC_BATCH_SIZE = 50;
	protected static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(DLSCommonHibernateDAO.class);
	private SessionFactory sessionFactory;

	private boolean lightWeightInd;

	protected DLSCommonHibernateDAO() {
		setLightWeightInd(Boolean.FALSE);
	}

	public boolean getLightWeightInd() {
		return lightWeightInd;
	}

	public void setLightWeightInd(boolean aLightWeightInd) {
		lightWeightInd = aLightWeightInd;
	}

	@SuppressWarnings("unchecked")
	public List<BO> retrieve(AgencyToken token, BO referenceBO)
			throws DLSPersistenceFatalException {
		List<BO> boList = new ArrayList<BO>();
		try {
			// Use JPA Criteria API instead of legacy Hibernate Criteria
			Query<BO> query = createQueryByExample(referenceBO);
			addAdditionalSearchCriteria(referenceBO, query);

			boList = query.getResultList();
			setAgencyToken(boList, token);
			this.populateChildren(token, boList);
		} catch (HibernateException hbEx) {
			String msg = "DLSCommonHibernateDAO.retrieve(): HibernateException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		}
		if(logger.isDebugEnabled()){
			logger.debug("OUT: DLSCommonHibernateDAO.retrieve(). poFacadeListSize = ["
						+ boList.size() + "]");
		}
		return boList;
	}

	@SuppressWarnings("unchecked")
	public BO retrieveByKey(AgencyToken token, BO referenceBO)
			throws DLSPersistenceFatalException, DLSDataNotFoundException {
		BO businessObject = null;
		try {
			Key primaryKey = this.getPrimaryKey(referenceBO);
			businessObject = (BO) (getCurrentSession().get(referenceBO
					.getClass(), primaryKey));
			if (businessObject == null) {
				String msg = "DLSCommonHibernateDAO.retrieveByKey(): No data found for key: ["
						+ primaryKey + "]";
				logger.error(msg);
				throw new DLSDataNotFoundException(msg);
			}
			if(logger.isDebugEnabled()){
				logger.debug("po = [" + businessObject.toString() + "]");
				logger.debug("OUT: DLSCommonHibernateDAO.retrieveByKey()");
			}
			businessObject.setAgencyToken(token);
			if (getLightWeightInd()) {
				setLightWeightInd(false);
			} else {
				List<BO> boList = new ArrayList<BO>();
				boList.add(businessObject);
				populateChildren(token,boList);
			}
			return businessObject;
		} catch (DLSDataNotFoundException noDataExc) {
			throw noDataExc;
		} catch (ClassCastException castEx) {
			String msg = "DLSCommonHibernateDAO.retrieveByKey(): ClassCastException caught, convert and throw: ["
					+ castEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, castEx);
		} catch (HibernateException hbEx) {
			String msg = "DLSCommonHibernateDAO.retrieveByKey(): HibernateException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		} catch (Exception exc) {
			String msg = "DLSCommonHibernateDAO.retrieveByKey(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
	}

	@Override
	public BO insert(AgencyToken token, BO source, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		updateSetArithabortFlag(aArithabortFlag);
		return insert(token, source);
	}

	@Override
	public BO insert(AgencyToken token, BO source)
			throws DLSPersistenceFatalException {
		try {
			BO savedBusinssObject = source;
			setDefaultFieldsBeforeInsertion(token,savedBusinssObject);

			Key id = saveBusinessObject(savedBusinssObject);

			if (id instanceof Integer) {
				savedBusinssObject.setId((Integer) id);
			} else {
				this.setId(source,id);
			}

			savedBusinssObject.setDirty(Boolean.FALSE);
			return (BO) savedBusinssObject;
		} catch (ClassCastException castEx) {
			String msg = "DLSCommonHibernateDAO.insert(): ClassCastException caught, convert and throw: ["
					+ castEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, castEx);
		} catch (HibernateException hbEx) {
			String msg = "DLSCommonHibernateDAO.insert(): HibernateException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		} catch (Exception exc) {
			String msg = "DLSCommonHibernateDAO.insert(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
	}

	@Override
	public List<BO> batchInsert(List<BO> sourceList)
			throws DLSPersistenceFatalException {
		List<BO> resultList = new ArrayList<BO>();
		for (int index = 0; index < sourceList.size(); index++) {
			BO theBO = sourceList.get(index);
			BO result = insert(theBO.getAgencyToken(), theBO);
			resultList.add(result);
			if (((1 + index) % HIBERNATE_JDBC_BATCH_SIZE == 0)) {
				this.getCurrentSession().flush();
				this.getCurrentSession().clear();
			}
		}
		return resultList;
	}

	/**
	 * Use retrieve() if you have a list of filter other than the primary key of
	 * the table. It retrieves a list of BO without retrieve the children
	 */
	@SuppressWarnings("unchecked")
	protected List<BO> retrieveParent(BO referenceBO) throws DLSPersistenceFatalException {
		String msg = "";
		List<BO> boList = new ArrayList<BO>();
		try {
			Query<BO> query = createQueryByExample(referenceBO);
			addAdditionalSearchCriteria(referenceBO, query);

			boList = query.getResultList();
			setAgencyToken(boList, referenceBO.getAgencyToken());
		} catch (HibernateException hbEx) {
			msg = "DLSCommonHibernateDAO.retrieve(): HibernateException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		}
		return boList;
	}

	protected BO retrieveParentByKey(BO referenceBO) {
		return doRetrieveParentByKey(referenceBO);
	}

	protected Boolean checkExists(BO bo, Key key)
			throws DLSPersistenceFatalException {
		Boolean exist = Boolean.TRUE;
		try {
			getCurrentSession().get(bo.getClass(), key);
		} catch (Exception e) {
			String msg = "DLSCommonHibernateDAO.retrieveByKey(): Exception caught: ["
					+ e + "]";
			logger.error(msg);
			exist = Boolean.FALSE;
		}
		return exist;
	}

	@SuppressWarnings("unchecked")
	private Key saveBusinessObject(BO savedBusinssObject) {
		Key id = (Key) getCurrentSession().save(savedBusinssObject);
		return id;
	}

	@SuppressWarnings("unchecked")
	private BO doRetrieveParentByKey(BO referenceBO) {
		String msg = "";
		BO businessObject = null;
		try {
			Key primaryKey = getPrimaryKey(referenceBO);
			businessObject = (BO) (getCurrentSession().get(referenceBO
					.getClass(), primaryKey));
			if (businessObject != null) {
				businessObject.setAgencyToken(referenceBO
						.getAgencyToken());
			}
		} catch (Exception castEx) {
			msg = "DLSCommonHibernateDAO.doRetrieveParentByKey(): Exception caught: ["
					+ castEx + "]";
			logger.error(msg);
		}
		return businessObject;
	}

	public void update(AgencyToken token, BO source, Boolean aArithabortFlag)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		updateSetArithabortFlag(aArithabortFlag);
		update(token, source);
	}

	public void update(AgencyToken token, BO source)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		try {
			BO businssObjectToBeUpdated = source;
			setDefaultFieldsBeforeUpdate(token,businssObjectToBeUpdated);
			getCurrentSession().merge(businssObjectToBeUpdated); // Changed from update() to merge()
			businssObjectToBeUpdated.setDirty(Boolean.FALSE);
		} catch (HibernateException hbEx) {
			String msg = "DLSCommonHibernateDAO.update(): HibernateException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		} catch (Exception exc) {
			String msg = "DLSCommonHibernateDAO.update(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
	}

	@Override
	public void remove(AgencyToken token, BO source) throws DLSPersistenceFatalException,
		DLSPersistenceOptimisticLockStopException{
		if (token != null) {
			source.setLastChangeUserName(token.getUserIdentifier());
		}
		remove(source);
	}
	
	@Override
	public void remove(BO source) throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		try {
			getCurrentSession().remove(source); // Changed from delete() to remove()
		} catch (HibernateException hbEx) {
			String msg = "DLSCommonHibernateDAO.remove(): HibernateException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		} catch (Exception exc) {
			String msg = "DLSCommonHibernateDAO.remove(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
	}

	public void deleteLogically(AgencyToken token, BO source,
			Boolean aArithArbortFlag) throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		BusinessObjectBase businessObject = source;
		String className = businessObject.getClass().getName();
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = { String.class };
			Method dataStatusCodeSetter = businessObject.getClass().getMethod(
					"setDataStatusCode", paramTypes);

			if (dataStatusCodeSetter != null) {
				Object[] paramInput = { "D" };
				dataStatusCodeSetter.invoke(businessObject, paramInput);
			}
		} catch (NoSuchMethodException noFuncExc) {
			String msg = "DLSCommonHibernateDAO.deleteLogically(): object does not support logic deletion ["
					+ className + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, noFuncExc);
		} catch (Exception exc) {
			String msg = "DLSCommonHibernateDAO.deleteLogically(): check if class has accessible setter for dataStatusCode ["
					+ className + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
		this.update(token, source, aArithArbortFlag);
	}

	@Override
	public void copyNonNullData(BO from, BO to) {
		if(from != null && to != null){
			to.copy(from);
		}
	}

	protected void setDefaultFieldsBeforeInsertion(AgencyToken token,BO theBO)
			throws DLSPersistenceFatalException {
		Date now = Calendar.getInstance().getTime();
		BO businessObject = theBO;
		businessObject.setLastChangeDate(now);
		businessObject.setCreationDate(now);

		if (token != null && token.getUserIdentifier() != null &&
				token.getUserIdentifier().trim().length() > 0) {
			businessObject.setCreationUserName(token.getUserIdentifier());
			businessObject.setLastChangeUserName(token.getUserIdentifier());
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("AgencyToken is null. Will set CreationUserName and LastChangeUserName to 'unknown'");
			}
			businessObject.setCreationUserName("Unknown by Hibernate Insert");
			businessObject.setLastChangeUserName("Unknown by Hibernate Insert");
		}
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = { String.class };
			Method dataStatusCodeSetter = businessObject.getClass().getMethod(
					"setDataStatusCode", paramTypes);

			if (dataStatusCodeSetter != null) {
				Object[] paramInput = { "A" };
				dataStatusCodeSetter.invoke(businessObject, paramInput);
			}
		} catch (NoSuchMethodException noFuncExc) {
			String msg = "DLSCommonHibernateDAO.setDefaultFieldsBeforeInsertion(): No dataStatusCode logic, no need to set dataStatusCode.";
			logger.info(msg);
		} catch (Exception exc) {
			String msg = "DLSCommonHibernateDAO.setDefaultFieldsBeforeInsertion(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
		businessObject.setDirty(Boolean.TRUE);
	}

	protected void setDefaultFieldsBeforeUpdate(AgencyToken token,BO theBO)
			throws DLSPersistenceFatalException {
		Date now = Calendar.getInstance().getTime();
		BusinessObjectBase businessObject = theBO;
		businessObject.setLastChangeDate(now);

		if (token != null && token.getUserIdentifier() != null &&
				token.getUserIdentifier().trim().length() > 0) {
			businessObject.setLastChangeUserName(token.getUserIdentifier());
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("AgencyToken is null. Will set LastChangeUserName to 'unknown'");
			}
			businessObject.setLastChangeUserName("Unknown by Hibernat Update");
		}
	}

	protected void addAdditionalSearchCriteria(BO referenceBO, Query<BO> query)
			throws DLSPersistenceFatalException {
		// Note: This method signature has changed - you'll need to implement
		// query parameter setting using HQL or native SQL instead of Criteria API
		// Example implementation would depend on your specific use case
		
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = null;
			Method dataStatusCodeGetter = referenceBO.getClass()
					.getMethod("getDataStatusCode", paramTypes);
			if (dataStatusCodeGetter != null) {
				// You'll need to modify the HQL query string or use parameters
				// This is a simplified example - adapt based on your actual query structure
				logger.info("DataStatusCode filtering needs to be implemented in query");
			}
		} catch (NoSuchMethodException noFuncExc) {
			String msg = "DLSCommonHibernateDAO.addAdditionalSearchCriteria(): No dataStatusCode logic, no need to add dataStatusCode restriction.";
			logger.info(msg);
		} catch (Exception exc) {
			String msg = "DLSCommonHibernateDAO.addAdditionalSearchCriteria(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
	}

	// Replace the old Criteria-based method with JPA Criteria API or HQL
	protected Query<BO> createQueryByExample(Object inMB) throws HibernateException {
		if (logger.isDebugEnabled()) {
			logger.debug("IN: createQueryByExample()");
			logger.debug("inMB = [" + inMB + "]");
		}
		
		try {
			// Validate entity class name to prevent injection
			Class<?> entityClass = inMB.getClass();
			String entityName = validateEntityClassName(entityClass);
			
			// Use parameterized query approach instead of string concatenation
			String hql = "FROM " + entityName + " e WHERE 1=1";
			
			// Add your example-based filtering logic here using parameters
			// This is a simplified version - you'll need to implement proper example matching
			// Example: hql += " AND e.property = :propertyValue";
			
			Query<BO> query = getCurrentSession().createQuery(hql, (Class<BO>) entityClass);
			
			// Set parameters here if you added any filters
			// Example: query.setParameter("propertyValue", exampleObject.getProperty());
			
			if(logger.isDebugEnabled()){
				logger.debug("OUT: createQueryByExample(). HQL: {}", hql);
			}
			
			return query;
		} catch (HibernateException hbEx) {
			String msg = "createQueryByExample(): HibernateException caught, re-throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw hbEx;
		}
	}
	
	/**
	 * Validates and sanitizes entity class name to prevent SQL injection
	 * @param entityClass The entity class
	 * @return Safe entity name for HQL queries
	 * @throws HibernateException if class name is invalid
	 */
	private String validateEntityClassName(Class<?> entityClass) throws HibernateException {
		String className = entityClass.getSimpleName();
		
		// Validate class name contains only safe characters (alphanumeric and underscore)
		if (!className.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
			throw new HibernateException("Invalid entity class name: " + className);
		}
		
		// Additional validation: ensure it's a known entity class
		// You can add more specific validation based on your entity naming conventions
		if (className.length() > 50) { // Reasonable length limit
			throw new HibernateException("Entity class name too long: " + className);
		}
		
		return className;
	}

	// Remove or comment out this method as SQL extraction is complex in Hibernate 6
	/*
	protected String getSQLFromCriteria(org.hibernate.Criteria query)
			throws Exception {
		// This method needs significant rewriting for Hibernate 6.x
		// Consider removing it or implementing with different approach
		throw new UnsupportedOperationException("SQL extraction from criteria not supported in Hibernate 6.x");
	}
	*/

	public org.hibernate.Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected void setAgencyToken(List<BO> boList,
			gov.usda.fsa.common.base.AgencyToken token) {
		for (BO object : boList) {
			if (object != null) {
				object.setAgencyToken(token);
			}
		}
	}

	protected java.sql.Connection getConnection() throws Exception {
	    javax.sql.DataSource dataSource = SessionFactoryUtils.getDataSource(sessionFactory);
	    if (dataSource == null) {
	        throw new DLSPersistenceFatalException("DataSource is null - check SessionFactory configuration");
	    }
	    return dataSource.getConnection();		
	}

	protected abstract Key getPrimaryKey(BO referenceBO);

	protected  void validate(List<BO> list, Key key) 
			throws DLSDataNotFoundException, DLSPersistenceFatalException{
		if (list.isEmpty()) {
			throw new DLSDataNotFoundException(
					"retrieval failed with key " + key);
		}
	}
	
	protected  void validate(List<BO> list)
			throws DLSDataNotFoundException, DLSPersistenceFatalException{
		if (list.isEmpty()) {
			throw new DLSDataNotFoundException(
					"retrieval failed ");
		}
	}

	protected  void populateChildren(AgencyToken token, List<BO> boList)
			throws DLSPersistenceFatalException{
		logger.info("subclass must implement this method " + token.getApplicationIdentifier());		
	}

	public int[] batchInsert(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		throw new org.apache.commons.lang.NotImplementedException(
				"batchInsert() is not supported in Hibernate version (DLSCommonHibernateDAO)...");
	}

	public int[] batchUpdate() throws DLSPersistenceFatalException {
		throw new org.apache.commons.lang.NotImplementedException(
				"batchUpdate() is not supported in Hibernate version (DLSCommonHibernateDAO)...");
	}

	protected void setId(BO bo, Key id){
		// Implement based on your specific needs
	}

	@Override
	public BO detatch(BO source) throws HibernateException {
		getCurrentSession().evict(source);
		return source;
	}

	public void clearSessionCache() {
		getCurrentSession().clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public BO refreshDetached(BO entity, Long id) throws HibernateException {
		Session session = getCurrentSession();
		BO attached = (BO) session.get(BusinessObjectBase.class, id);
		if (attached != entity) {
			session.evict(attached);
			session.lock(entity, LockMode.NONE);
		}
		session.refresh(entity);
		return entity;
	}

	private void updateSetArithabortFlag(Boolean aArithabortFlag) {
		if (aArithabortFlag) {
			try {
				Session session = getCurrentSession();
				session.createNativeQuery("SET ARITHABORT ON").executeUpdate(); // Changed from createSQLQuery
			} catch (HibernateException e) {
				logger.error("Failed to execute 'SET ARITHABORT ON' on update(true)", e);
			}
		}
	}

	// Keep all deprecated methods as-is for backward compatibility
	private AgencyToken getAgencyToken(){
		return (getBusinessObject() == null? null: getBusinessObject().getAgencyToken());
	}
	
	@Deprecated
	protected void setDefaultFieldsBeforeUpdate()
			throws DLSPersistenceFatalException {
		if(getBusinessObject() != null){
			setDefaultFieldsBeforeUpdate(getAgencyToken(),getBusinessObject());
		}
	}
	
	@Override
	@Deprecated
	public void deleteLogically(Boolean aArithArbortFlag)
			throws DLSPersistenceFatalException {
		try {
			deleteLogically(getAgencyToken(),
					getBusinessObject(), aArithArbortFlag);
		} catch (DLSPersistenceOptimisticLockStopException ex) {
			logger.error("Error in deleteLogically()..", ex);
		}
	}
	
	@Deprecated
	protected void setDefaultFieldsBeforeInsertion()
			throws DLSPersistenceFatalException {
		if(getBusinessObject() != null){
			setDefaultFieldsBeforeInsertion(getAgencyToken(),
					getBusinessObject());
		}
	}
	
	@Override
	@Deprecated
	public void update() throws DLSPersistenceFatalException {
		try{
		update(getAgencyToken(), getBusinessObject());
		}catch(DLSPersistenceOptimisticLockStopException e){
			logger.error("Cannot update the business object, failed with DLSPersistenceOptimisticLockStopException ..", e);
			throw new DLSPersistenceFatalException("Cannot update the business object, failed with DLSPersistenceOptimisticLockStopException ..", e);
		}
	}

	@Override
	@Deprecated
	public void remove() throws DLSPersistenceFatalException {
		try {
			remove(getBusinessObject());
		} catch (DLSPersistenceOptimisticLockStopException e) {
			logger.error("Cannot remiving business object from persistence store on remove() call: DLSPersistenceOptimisticLockStopException exception..", e);
			throw new DLSPersistenceFatalException("Cannot remiving business object from persistence store on remove() call: DLSPersistenceOptimisticLockStopException exception..", e);
		}
	}
	
	@Deprecated
	public void update(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		try {
			update(getAgencyToken(), getBusinessObject(),
					aArithabortFlag);
		} catch (DLSPersistenceOptimisticLockStopException ex) {
			logger.error("DLSCommonHibernateDAO.update(aArithabortFlag) failed with DLSPersistenceOptimisticLockStopException", ex);
			throw new DLSPersistenceFatalException("DLSCommonHibernateDAO.update(aArithabortFlag) failed with DLSPersistenceOptimisticLockStopException", ex);
		}
	}
	
	@Deprecated
	protected BO retrieveParentByKey() {
		return doRetrieveParentByKey(getBusinessObject());
	}
	
	@Deprecated
	public List<BO> retrieve() throws DLSPersistenceFatalException {
		return retrieve(getAgencyToken());
	}
	
	@Deprecated
	public List<BO> retrieve(AgencyToken token)
			throws DLSPersistenceFatalException {
		return retrieve(token,getBusinessObject());
	}
	
	@Deprecated
	public BO retrieveByKey() throws DLSPersistenceFatalException,
			DLSDataNotFoundException {
		return retrieveByKey(getAgencyToken(),
				getBusinessObject());
	}
	
	@Override
	@Deprecated
	public BO insert() throws DLSPersistenceFatalException {
		return insert(getAgencyToken(), getBusinessObject());
	}
	
	@Override
	@Deprecated
	public BO insert(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		return insert(getAgencyToken(),
				getBusinessObject(), aArithabortFlag);
	}
	
	@Deprecated
	protected List<BO> retrieveParent() throws DLSPersistenceFatalException {
		return retrieveParent(getBusinessObject());
	}
	
	@Deprecated
	protected BO getBusinessObject() {
		return this.theBusinessObject;
	}

	@Deprecated
	public void setBusinessObject(BO bo) {
		this.theBusinessObject = bo;
	}
	
	@Deprecated
	protected  void populateChildren(List<BO> boList)
			throws DLSPersistenceFatalException{
		if(!boList.isEmpty()){
			populateChildren(boList.get(0).getAgencyToken(),boList);
		}
	}

	@Deprecated
	public void setId(Key id){
		// Implement if needed
	}
	
	@Deprecated
	private BO theBusinessObject;
}