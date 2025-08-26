package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.base;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.BusinessObjectBase;
import gov.usda.fsa.fcao.flp.flpids.common.dao.dao.util.DAOUtil;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSPersistenceFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSPersistenceOptimisticLockStopException;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * @author chris.caruthers
 * @date Last Updated on April 19/2018
 */
public abstract class DLSSimpleJdbcDaoSupport<BO extends BusinessObjectBase, Key extends Serializable>
		extends DLSReadonlyJdbcDaoSupport<BO, Key> {

	public static boolean treatUpdateCountZeroAsPersistenceOptimisticLockStopException = true;
	public static boolean isTreatUpdateCountZeroAsPersistenceOptimisticLockStopException() {
		return treatUpdateCountZeroAsPersistenceOptimisticLockStopException;
	}
	public static void setTreatUpdateCountZeroAsPersistenceOptimisticLockStopException(
			boolean treatUpdateCountZeroAsPersistenceOptimisticLockStopException) {
		DLSSimpleJdbcDaoSupport.treatUpdateCountZeroAsPersistenceOptimisticLockStopException = treatUpdateCountZeroAsPersistenceOptimisticLockStopException;
	}
	/*
 * default implementation
 */
	protected void setId(BO bo, Key id){
		if(id instanceof Integer){
			bo.setId((Integer)id);
		}else if(id instanceof Long){
			bo.setId((Integer)id);
		}
	}
	protected void create(String sql, BO domainObject)
			throws DLSPersistenceFatalException {
		int rc = -1;
		String msg = "";
		KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		try {
			SqlParameterSource localParamSource = new BeanPropertySqlParameterSource(
					domainObject);
			
			NamedParameterJdbcTemplate jdbcTemplate = new DAOUtil().validateNamedParameterJdbcTemplate(this.getNamedParameterJdbcTemplate());
			rc = jdbcTemplate.update(sql, localParamSource, generatedKeyHolder);			
			
			if (rc == 1) {
				try {
					Number key = generatedKeyHolder.getKey();
					if (key != null) {
						Integer generatedId = key.intValue();
						domainObject.setId(generatedId);
					} else {
						logGenerateKeySet(generatedKeyHolder);
					}
				} catch (DataRetrievalFailureException drfe) {
					/**
					 * It is ok if we failed to get single primary key. Try
					 * composite key
					 */
					logGenerateKeySet(generatedKeyHolder);
				}
			}
		} catch (DataAccessException hbEx) {
			msg = "DLSSimpleJdbcDaoSupport.create(): DataAccessException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, hbEx);
		} catch (Exception exc) {
			msg = "DLSSimpleJdbcDaoSupport.create(): Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
		if (rc == 0) {
			msg = "DLSSimpleJdbcDaoSupport.create(): record not inserted";
			throw new DLSPersistenceFatalException(msg);
		}
	}

	protected void update(String sql, BO domainObject)
			throws DLSPersistenceFatalException,
			DLSPersistenceOptimisticLockStopException {
		doUpdate(sql,domainObject);
	}
	
	// Common method to handle update/delete operations (eliminates code duplication)
	private int executeUpdateOperation(String sql, BO domainObject, String operationType) 
	        throws DLSPersistenceFatalException {
	    int rc = -1;
	    String msg = "";
	    try {
	        SqlParameterSource localParamSource = new BeanPropertySqlParameterSource(
	                domainObject);
	        
	        NamedParameterJdbcTemplate jdbcTemplate = new DAOUtil().validateNamedParameterJdbcTemplate(this.getNamedParameterJdbcTemplate());
	        rc = jdbcTemplate.update(sql, localParamSource);
	        
	    } catch (DataAccessException hbEx) {
	        msg = "DLSSimpleJdbcDaoSupport." + operationType + "(): DataAccessException caught, convert and throw: ["
	                + hbEx + "]";
	        logger.error(msg);
	        throw new DLSPersistenceFatalException(msg, hbEx);
	    } catch (Exception exc) {
	        msg = "DLSSimpleJdbcDaoSupport." + operationType + "(): Exception caught, convert and throw: ["
	                + exc + "]";
	        logger.error(msg);
	        throw new DLSPersistenceFatalException(msg, exc);
	    }
	    return rc;
	}

	protected void doUpdate(String sql, BO domainObject)
	        throws DLSPersistenceFatalException,
	        DLSPersistenceOptimisticLockStopException {
	    
	    int rc = executeUpdateOperation(sql, domainObject, "update");
	    
	    if (rc == 0 && isTreatUpdateCountZeroAsPersistenceOptimisticLockStopException()) {
	        String msg = "DLSSimpleJdbcDaoSupport.update(): optimistic lock on record exists";
	        throw new DLSPersistenceOptimisticLockStopException(msg);
	    }
	}

	protected void delete(String sql, BO domainObject)
	        throws DLSPersistenceFatalException, DLSPersistenceOptimisticLockStopException {
	    
	    int rc = executeUpdateOperation(sql, domainObject, "delete");
	    
	    if (rc == 0 && isTreatUpdateCountZeroAsPersistenceOptimisticLockStopException()) {
	        String msg = "DLSSimpleJdbcDaoSupport.delete(): record not deleted";
	        throw new DLSPersistenceOptimisticLockStopException(msg);
	    }
	}	

	private void logGenerateKeySet(KeyHolder generatedKeyHolder) {
		Map<?,?> keyMap = generatedKeyHolder.getKeys();
		
	    if (keyMap == null) {
	        logger.info("No keys returned from insert operation");
	        return;
	    }		
		
		logger.info("Key and Value from insert())");
		Iterator<?> keyIterator = keyMap.keySet().iterator();
		Iterator<?> valueIterator = keyMap.values().iterator();
		for (int index = 0; index < keyMap.values().size(); index++) {
			logger.info("key=" + keyIterator.next() + " value="
					+ valueIterator.next());
		}
	}

	/**
	 * This method can be used to create any BO in this Dao (the generic type
	 * not used)
	 * 
	 * @param sql
	 * @param domainObject
	 * @throws DLSPersistenceFatalException
	 */
	protected int executeGenericSQLScript(String sql,
			BusinessObjectBase domainObject)
			throws DLSPersistenceFatalException {
		int rc = -1;
		String msg = "";
		KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		try {
			SqlParameterSource localParamSource = new BeanPropertySqlParameterSource(
					domainObject);
			
			NamedParameterJdbcTemplate jdbcTemplate = new DAOUtil().validateNamedParameterJdbcTemplate(this.getNamedParameterJdbcTemplate());
			rc = jdbcTemplate.update(sql, localParamSource, generatedKeyHolder);			

		} catch (DataAccessException hbEx) {
			msg = "executeGenericSQLScript: DataAccessException caught, convert and throw: ["
					+ hbEx + "]";
			logger.error(msg);
		} catch (Exception exc) {
			msg = "executeGenericSQLScript: Exception caught, convert and throw: ["
					+ exc + "]";
			logger.error(msg);
			throw new DLSPersistenceFatalException(msg, exc);
		}
		return rc;
	}
	
///////////////////////////////////////****BELOW METHOD ARE DEPRECATED****///////////////////////////	
	@Deprecated
	public void setId(Key id){
		
	}
}
