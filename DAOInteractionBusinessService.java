package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.dao.dao.IDataAccessObject;
import gov.usda.fsa.fcao.flp.flpids.common.factory.IDataAccessObjectFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implementation of the IDAOInteractionBusinessService interface.<br>
 * <br>
 * Provides method of obtaining a DAO from the reference to the
 * IDataAccessObjectFactory object.<br>
 * <br>
 * 
 * @author Steve.Congdon
 * 
 */
public abstract class DAOInteractionBusinessService implements IDAOInteractionBusinessService, ApplicationContextAware {
	protected static final Logger LOGGER = LogManager.getLogger(DAOInteractionBusinessService.class);
	private ApplicationContext applicationContext;
	protected IDataAccessObjectFactory dAOFactory;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setdAOFactory(IDataAccessObjectFactory dAOFactory) {
		this.dAOFactory = dAOFactory;
	}

	/**
	 * @param key identifies the DAO type to return.
	 * @return DAO instance that matched the passed key value.
	 * @throws DLSBusinessFatalException indicates that there was an error finding a
	 *                                   reference to the IDataAccessObjectFactory
	 *                                   object. Check spring configuration file.
	 */
	protected IDataAccessObject<?, ?> getDAO(String key) throws DLSBusinessFatalException {
		if (dAOFactory == null) {
			dAOFactory = (IDataAccessObjectFactory) applicationContext.getBean("commonDaoFactory");
		}
		IDataAccessObject<?, ?> dataAccessObject = dAOFactory.getDataAccessObject(key);
		if (dataAccessObject == null) {
			dataAccessObject = (IDataAccessObject<?, ?>) applicationContext.getBean(key);
		}
		if (dataAccessObject == null) {
			throw new DLSBusinessFatalException("Failed to get Dao Instance from DAO Factory. The Dao type is: " + key);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getDAO() for Key " + key + " Dao instance is: " + dataAccessObject.toString());
		}
		return dataAccessObject;
	}

}
