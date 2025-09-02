package gov.usda.fsa.fcao.flp.flpids.common.business.common.utilities;

import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class NormalizeAreaOfResponsibility_UT extends DLSExternalCommonTestMockBase{
	private NormalizeAreaOfResponsibility tester;
	@Before
	public void setUp() throws Exception{
		super.setUp();
		tester = (NormalizeAreaOfResponsibility) ReflectionUtility.createObject(NormalizeAreaOfResponsibility.class);
	}
	@Test
	public void testProcessWithEmptySource() throws Exception{
		Collection<String> source = new ArrayList<String>(); 
		String eAuthID = "123232";
		
		Collection<String> result = 
			tester.process(source, eAuthID);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(0, result.size());
	}
	@Test
	public void testProcessWithNullSource() throws Exception{
		String eAuthID = "123232";
		
		Collection<String> result = 
			tester.process(null, eAuthID);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(0, result.size());
	}
	@Test
	public void testProcessWithSingleEmptySource() throws Exception{
		Collection<String> source = new ArrayList<String>(); 
		source.add("");
		String eAuthID = "123232";
		
		Collection<String> result = 
			tester.process(source, eAuthID);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(0, result.size());
	}
	@Test
	public void testProcessWithWrongLengthOfOfficeCode() throws Exception{
		Collection<String> source = new ArrayList<String>(); 
		source.add("12323241");
		String eAuthID = "123232";
		
		Collection<String> result = tester.process(source, eAuthID);
		
		Assert.assertTrue(result.isEmpty());
	}
	@Test 
	public void testProcessWithWrongStateCode() throws Exception{
		Collection<String> source = new ArrayList<String>(); 
		source.add("12923241");
		String eAuthID = "123232";
		
		Collection<String> result = tester.process(source, eAuthID);
		
		Assert.assertTrue(result.isEmpty());
	}
	@Test 
	public void testProcessWithCodeNotInteger() throws Exception{
		Collection<String> source = new ArrayList<String>(); 
		source.add("afaerer");
		String eAuthID = "123232";
		
		Collection<String> result =  tester.process(source, eAuthID);
		
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test 
	public void testProcessWithSingleValidSource() throws Exception{
		Collection<String> source = new ArrayList<String>(); 
		source.add("34339");
		String eAuthID = "123232";
		
		Collection<String> result = 
			tester.process(source, eAuthID);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
	}
	
	@Test 
	public void testProcessWithSingleValidStateCode() throws Exception{
		Collection<String> source = new ArrayList<String>(); 
		source.add("34330");
		String eAuthID = "123232";
		
		Collection<String> result = 
			tester.process(source, eAuthID);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
	}
}
