package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.base;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.HibernateException;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.persister.entity.OuterJoinLoadable;
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
			Criteria aCriteria = createCriteriaByExample(referenceBO);
			addAdditionalSearchCriteria(referenceBO,aCriteria);

			boList = aCriteria.list();
			setAgencyToken(boList, token);
			this.populateChildren(token,boList);
		} catch (HibernateException hbEx) {
			String msg = "DLSCommonHibernateDAO.retrieve(): HibernateException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		}
		if(logger.isDebugEnabled()){
			logger
				.debug("OUT: DLSCommonHibernateDAO.retrieve(). poFacadeListSize = ["
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
		} // end try
		catch (DLSDataNotFoundException noDataExc) {
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
	 * Use retrieve()if you have a list of filter other than the primary key of
	 * the table. It retrieves a list of BO without retrieve the children
	 */
	@SuppressWarnings("unchecked")
	protected List<BO> retrieveParent(BO referenceBO) throws DLSPersistenceFatalException {
		String msg = "";
		List<BO> boList = new ArrayList<BO>();
		try {
			Criteria aCriteria = createCriteriaByExample(referenceBO);
			addAdditionalSearchCriteria(referenceBO,aCriteria);

			boList = aCriteria.list();
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
		} // end try
		catch (Exception e) {
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
			getCurrentSession().update(businssObjectToBeUpdated);
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
			getCurrentSession().delete(source);
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
				logger
					.debug("AgencyToken is null. Will set CreationUserName and LastChangeUserName to 'unknown'");
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
				logger
					.debug("AgencyToken is null. Will set LastChangeUserName to 'unknown'");
			}
			businessObject.setLastChangeUserName("Unknown by Hibernat Update");
		}
	}



	protected void addAdditionalSearchCriteria(BO referenceBO,Criteria inCriteria)
			throws DLSPersistenceFatalException {
		if (getPrimaryKey(referenceBO) != null
				&& getPrimaryKey(referenceBO).getClass() == Integer.class) {
			inCriteria.add(Restrictions.eq("id", getPrimaryKey(referenceBO)));
			inCriteria.addOrder(Order.asc("id"));
		}

		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = null;
			Method dataStatusCodeGetter = referenceBO.getClass()
					.getMethod("getDataStatusCode", paramTypes);
			if (dataStatusCodeGetter != null) {
				// inCriteria.add(Restrictions.ne("dataStatusCode", "D"));
				inCriteria.add(Restrictions.eq("dataStatusCode", "A"));
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

	protected Criteria createCriteriaByExample(Object inMB)
			throws HibernateException {
		if (logger.isDebugEnabled()) {
			logger.debug("IN: createCriteriaByExample()");
			logger.debug("inMB = [" + inMB + "]");
		}
		Criteria aCriteria = null;
		try {
			aCriteria = getCurrentSession().createCriteria(inMB.getClass())
					.add(Example.create(inMB));
			if(logger.isDebugEnabled()){
				logger.debug("\n IN: createCriteriaByExample() Crtieria"
					+ inMB.toString());
			}
		} catch (HibernateException hbEx) {
			String msg = "createCriteriaByExample(): HibernateException caught, re-throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw hbEx; // re-throw
		}
		if (logger.isDebugEnabled()) {
			HibernateHqlAndCriteriaToSqlTranslator translator = new HibernateHqlAndCriteriaToSqlTranslator();
			translator.setSessionFactory(this.sessionFactory);
			String sqlString = translator.toSql(aCriteria);
			logger.debug("OUT: createCriteriaByExample(). SQL String: "
					+ sqlString);
		}
		return aCriteria;
	}

	protected String getSQLFromCriteria(org.hibernate.Criteria query)
			throws Exception {
		CriteriaImpl c = (CriteriaImpl) query;
		SessionImpl s = (SessionImpl) c.getSession();
		SessionFactoryImplementor factory = (SessionFactoryImplementor) s.getSessionFactory();
		LoadQueryInfluencers loadQueryInfluencers = new LoadQueryInfluencers(factory);
		String[] implementors = factory.getImplementors(c.getEntityOrClassName());
		org.hibernate.loader.criteria.CriteriaLoader loader = new org.hibernate.loader.criteria.CriteriaLoader(
				(OuterJoinLoadable) factory.getEntityPersister(implementors[0]),
				factory, c, implementors[0], loadQueryInfluencers);
		java.lang.reflect.Field sqlField = OuterJoinLoader.class
				.getDeclaredField("sql");
		sqlField.setAccessible(true);
		return (String) sqlField.get(loader);
	}

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
		return SessionFactoryUtils.getDataSource(sessionFactory)
				.getConnection();
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

	
	/**
	 * This batch insert is specifically for Spring JDBC framework. Not to be
	 * used in Hibernate calls. Hence not implemented. SG -11/06/2013
	 */
	public int[] batchInsert(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		throw new org.apache.commons.lang.NotImplementedException(
				"batchInsert() is not supported in Hibernate version (DLSCommonHibernateDAO)...");
	}

	/**
	 * This batch update is specifically for Spring JDBC framework. Not to be
	 * used in Hibernate calls. Hence not implemented. SG -11/06/2013
	 */
	public int[] batchUpdate() throws DLSPersistenceFatalException {
		throw new org.apache.commons.lang.NotImplementedException(
				"batchUpdate() is not supported in Hibernate version (DLSCommonHibernateDAO)...");
	}

	
	protected void setId(BO bo, Key id){
		
	}
	/**
	 * the associated entity will be detached also if mapped with
	 * cascade="evict"
	 */
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
		BO attached = (BO) session.load(BusinessObjectBase.class, id);
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
				session.createSQLQuery(
						"SET ARITHABORT ON").executeUpdate();
				//query.executeUpdate();

			} catch (HibernateException e) {
				logger
						.error(
								"Failed to execute 'SET ARITHABORT ON' on update(true)",
								e);
			}
		}
	}
	
///////////****BELOW METHOD ARE DEPRECATED****///////////////////////////
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
			logger
					.error(
							"Cannot remiving business object from persistence store on remove() call: DLSPersistenceOptimisticLockStopException exception..",
							e);
			throw new DLSPersistenceFatalException(
					"Cannot remiving business object from persistence store on remove() call: DLSPersistenceOptimisticLockStopException exception..",
					e);
		}
	}
	@Deprecated
	public void update(Boolean aArithabortFlag)
			throws DLSPersistenceFatalException {
		try {
			update(getAgencyToken(), getBusinessObject(),
					aArithabortFlag);
		} catch (DLSPersistenceOptimisticLockStopException ex) {
			logger
					.error(
							"DLSCommonHibernateDAO.update(aArithabortFlag) failed with DLSPersistenceOptimisticLockStopException",
							ex);
			throw new DLSPersistenceFatalException(
					"DLSCommonHibernateDAO.update(aArithabortFlag) failed with DLSPersistenceOptimisticLockStopException",
					ex);
		}
	}
	/**
	 * Use this method if a primary Key of the table is available. Only one row
	 * returns if the key is correct. Make sure you call setId() in BO before
	 * calling this method. It will not load the children
	 */
	@Deprecated
	protected BO retrieveParentByKey() {
		return doRetrieveParentByKey(getBusinessObject());
	}
	/**
	 * Use retrieve()if you have a list of filter other than the primary key of
	 * the table. It retrieves a list of BO
	 */
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
	
}
	@Deprecated
	private BO theBusinessObject;

}