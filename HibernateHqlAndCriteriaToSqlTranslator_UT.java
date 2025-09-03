package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.junit.Test;

public class HibernateHqlAndCriteriaToSqlTranslator_UT {

	private SessionFactory sessionFactory;
	private Criteria criteria;
	private String sqlString;
	
	@Test
	public void testSessionFactory() {		
		HibernateHqlAndCriteriaToSqlTranslator hbm = new HibernateHqlAndCriteriaToSqlTranslator();		
			
		sessionFactory = mock(SessionFactory.class);		
		hbm.setSessionFactory(sessionFactory);
		assertNotNull(sessionFactory);		
	}
	
	@Test(expected=RuntimeException.class)
	public void testRuntimeException() {		
		HibernateHqlAndCriteriaToSqlTranslator hbm = new HibernateHqlAndCriteriaToSqlTranslator();	
		
		criteria = mock(Criteria.class);
		criteria.setComment("Hibernate");
		sqlString = hbm.toSql(criteria);
	}
	
	//@Test
	public void testException_03() {		
		HibernateHqlAndCriteriaToSqlTranslator hbm = new HibernateHqlAndCriteriaToSqlTranslator();	
			
		criteria = mock(Criteria.class);
		sqlString = (String) hbm.toSql(criteria);
		assertNull(sqlString);
	}
}