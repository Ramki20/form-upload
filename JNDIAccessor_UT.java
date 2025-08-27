package gov.usda.fsa.fcao.flp.flpids.common.utilities;

import javax.naming.NameNotFoundException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JNDIAccessor_UT extends JndiMockTestBase{
	private JNDIAccessor jndiAccessor;
	@Before
	public void setUp () throws Exception{
		jndiAccessor = new JNDIAccessor();
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			context.unbind("usda.fsa.fcao.counter");
		} catch (Exception ex) {
			// attempt to remove JNDI entry.
		}
	}
	
	@Test (expected=NameNotFoundException.class)
	public void test_getJNDIIntegerWithFullPath_NameNotBind() throws Exception{		
		String entry = "usda.fsa.fcao.counter";
		
		jndiAccessor.getJNDIIntegerWithFullPath(entry);
	}
	
	@Test (expected=ClassCastException.class)
	public void test_getJNDIIntegerWithFullPath_exception() throws Exception{
		
		String entry = "usda.fsa.fcao.counter";
		context.rebind(entry, "123");
		
		Integer value = jndiAccessor.getJNDIIntegerWithFullPath(entry);
		
		Assert.assertEquals(123,  value.intValue());
	}
	
	@Test 
	public void test_getJNDIIntegerWithFullPath() throws Exception{
		
		String entry = "usda.fsa.fcao.counter";
		context.rebind(entry, 123);
		
		Integer value = jndiAccessor.getJNDIIntegerWithFullPath(entry);
		
		Assert.assertEquals(123,  value.intValue());
	}
	
	@Test 
	public void test_getJNDIStringWithFullPath() throws Exception{
		
		String entry = "usda.fsa.fcao.counter";
		context.rebind(entry, "123");
		
		String value = jndiAccessor.getJNDIStringWithFullPath(entry);
		
		Assert.assertEquals("123",  value);
	}
	
	@Test 
	public void test_getJNDIInteger() throws Exception{
		
		String entry = "java:comp/env/greetings";
		context.rebind(entry, "hello");
		
		context.rebind("java:comp/env/inventory/max", 256);
		
		Integer value = jndiAccessor.getJNDIInteger(entry);
		
		Assert.assertEquals(256,  value.intValue());
	}
	
	@Test 
	public void test_getJNDIString() throws Exception{
		
		String entry = "java:comp/env/greetings";
		context.rebind(entry, "hello");
		
		context.rebind("java:comp/env/inventory/max", "256");
		
		String value = jndiAccessor.getJNDIString(entry);
		
		Assert.assertEquals("256",  value);
	}
	
	
}
