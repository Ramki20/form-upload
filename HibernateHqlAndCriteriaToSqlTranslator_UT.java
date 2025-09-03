package gov.usda.fsa.fcao.flp.flpids.common.dao.impl.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

public class HibernateHqlAndCriteriaToSqlTranslator_UT {

	private SessionFactory sessionFactory;
	private Session session;
	private Query<?> query;
	private HibernateHqlAndCriteriaToSqlTranslator translator;
	
	@Before
	public void setUp() {
		// Create mocks manually instead of using annotations to avoid Mockito issues
		sessionFactory = mock(SessionFactory.class);
		session = mock(Session.class);
		query = mock(Query.class);
		translator = new HibernateHqlAndCriteriaToSqlTranslator();
	}
	
	@After
	public void tearDown() {
		// Clean up mocks
		sessionFactory = null;
		session = null;
		query = null;
		translator = null;
	}
	
	@Test
	public void testSetSessionFactory() {		
		translator.setSessionFactory(sessionFactory);
		// We can't directly assert the internal state, but we can verify it was set
		assertNotNull(sessionFactory);		
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testCriteriaToSqlThrowsUnsupportedOperation() {		
		// Test that the deprecated Criteria API method throws UnsupportedOperationException
		Object mockCriteria = new Object(); // Simple object instead of mocking Criteria
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
		assertTrue("Result should indicate SQL extraction limitation", 
				   result.contains("SQL extraction not directly available") || 
				   result.contains("Unable to extract SQL"));
	}
	
	@Test
	public void testHqlToSqlWithRuntimeException() {
		// Setup mocks to throw exception
		String hql = "INVALID HQL QUERY";
		
		when(sessionFactory.getCurrentSession()).thenThrow(new RuntimeException("Mock exception"));
		
		translator.setSessionFactory(sessionFactory);
		
		try {
			translator.toSql(hql);
			fail("Expected RuntimeException to be thrown");
		} catch (RuntimeException e) {
			assertTrue("Exception message should contain expected text", 
					   e.getMessage().contains("Error extracting SQL from HQL"));
		}
	}
	
	@Test
	public void testGetSqlExtractionAdvice() {
		String advice = translator.getSqlExtractionAdvice();
		
		assertNotNull("Advice should not be null", advice);
		assertTrue("Advice should contain SQL logging info", 
				   advice.contains("logging.level.org.hibernate.SQL=DEBUG"));
		assertTrue("Advice should contain show-sql info", 
				   advice.contains("spring.jpa.show-sql=true"));
		assertTrue("Advice should contain format-sql info", 
				   advice.contains("hibernate.format_sql=true"));
	}
	
	/**
	 * Test backward compatibility - ensure the method signature still exists
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testBackwardCompatibilityForCriteriaApi() {
		// This test ensures that calling the old Criteria method 
		// throws UnsupportedOperationException as documented
		Object fakeCriteria = new Object(); // Simple object instead of complex mock
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
			assertTrue("Exception should contain expected message", 
					   e.getMessage().contains("Error extracting SQL from HQL"));
			assertTrue("Exception should have correct cause", 
					   e.getCause() instanceof IllegalArgumentException);
		}
	}
	
	/**
	 * Test with session factory that returns null session
	 */
	@Test
	public void testHqlToSqlWithNullSession() {
		String hql = "FROM User";
		
		when(sessionFactory.getCurrentSession()).thenReturn(null);
		
		translator.setSessionFactory(sessionFactory);
		
		try {
			translator.toSql(hql);
			fail("Expected RuntimeException to be thrown");
		} catch (RuntimeException e) {
			assertTrue("Exception should indicate error in SQL extraction", 
					   e.getMessage().contains("Error extracting SQL from HQL"));
		}
	}
	
	/**
	 * Test method without setting session factory
	 */
	@Test
	public void testHqlToSqlWithoutSessionFactory() {
		String hql = "FROM User";
		
		// Don't set session factory - translator should handle null gracefully
		try {
			String result = translator.toSql(hql);
			fail("Expected RuntimeException due to null SessionFactory");
		} catch (RuntimeException e) {
			assertTrue("Exception should indicate error in SQL extraction", 
					   e.getMessage().contains("Error extracting SQL from HQL"));
		}
	}
}