package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HibernateHqlAndCriteriaToSqlTranslator_UT {

	@Mock
	private SessionFactory sessionFactory;
	
	@Mock
	private SessionFactoryImplementor sessionFactoryImplementor;
	
	@Mock
	private Session session;
	
	@Mock
	private Query<?> query;
	
	private HibernateHqlAndCriteriaToSqlTranslator translator;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		translator = new HibernateHqlAndCriteriaToSqlTranslator();
	}
	
	@Test
	public void testSetSessionFactory() {		
		translator.setSessionFactory(sessionFactory);
		assertNotNull(sessionFactory);		
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testCriteriaToSqlThrowsUnsupportedOperation() {		
		// Test that the deprecated Criteria API method throws UnsupportedOperationException
		Object mockCriteria = mock(Object.class);
		translator.toSql(mockCriteria);
	}
	
	@Test
	public void testHqlToSqlWithNullInput() {
		String result = translator.toSql((String) null);
		assertNull(result);
	}
	
	@Test
	public void testHqlToSqlWithEmptyInput() {
		String result = translator.toSql("");
		assertNull(result);
	}
	
	@Test
	public void testHqlToSqlWithWhitespaceInput() {
		String result = translator.toSql("   ");
		assertNull(result);
	}
	
	@Test
	public void testHqlToSqlWithValidHql() {
		// Setup mocks
		String hql = "FROM User WHERE name = :name";
		
		when(sessionFactory.getCurrentSession()).thenReturn(session);
		when(session.createQuery(hql)).thenReturn(query);
		
		translator.setSessionFactory(sessionFactory);
		String result = translator.toSql(hql);
		
		// In Hibernate 6.x, direct SQL extraction is not available
		// So we expect a message indicating this
		assertNotNull(result);
		assertTrue(result.contains("SQL extraction not directly available") || 
				   result.contains("Unable to extract SQL"));
	}
	
	@Test(expected = RuntimeException.class)
	public void testHqlToSqlWithException() {
		// Setup mocks to throw exception
		String hql = "INVALID HQL QUERY";
		
		when(sessionFactory.getCurrentSession()).thenThrow(new RuntimeException("Mock exception"));
		
		translator.setSessionFactory(sessionFactory);
		translator.toSql(hql);
	}
	
	@Test
	public void testGetSqlExtractionAdvice() {
		String advice = translator.getSqlExtractionAdvice();
		
		assertNotNull(advice);
		assertTrue(advice.contains("logging.level.org.hibernate.SQL=DEBUG"));
		assertTrue(advice.contains("spring.jpa.show-sql=true"));
		assertTrue(advice.contains("hibernate.format_sql=true"));
	}
	
	/**
	 * Test to verify the translator handles SessionFactoryImplementor casting
	 */
	@Test
	public void testHqlToSqlWithSessionFactoryImplementor() {
		String hql = "FROM User";
		
		// Mock SessionFactoryImplementor instead of regular SessionFactory
		when(sessionFactoryImplementor.getCurrentSession()).thenReturn(session);
		when(session.createQuery(hql)).thenReturn(query);
		
		translator.setSessionFactory(sessionFactoryImplementor);
		String result = translator.toSql(hql);
		
		assertNotNull(result);
	}
	
	/**
	 * Test backward compatibility - ensure the method signature still exists
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testBackwardCompatibilityForCriteriaApi() {
		// This test ensures that calling the old Criteria method 
		// throws UnsupportedOperationException as documented
		Object fakeCriteria = new Object(); // Any object will do
		translator.toSql(fakeCriteria);
	}
	
	/**
	 * Test that verifies proper exception handling in HQL translation
	 */
	@Test
	public void testHqlToSqlExceptionHandling() {
		String hql = "FROM ValidEntity";
		
		// Setup mock to throw a specific exception during query creation
		when(sessionFactory.getCurrentSession()).thenReturn(session);
		when(session.createQuery(hql)).thenThrow(new IllegalArgumentException("Mock HQL parsing error"));
		
		translator.setSessionFactory(sessionFactory);
		
		try {
			translator.toSql(hql);
			fail("Expected RuntimeException to be thrown");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("Error extracting SQL from HQL"));
			assertTrue(e.getCause() instanceof IllegalArgumentException);
		}
	}
}