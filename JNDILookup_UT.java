package gov.usda.fsa.fcao.flp.flpids.common.utilities;

import junit.framework.Assert;

import org.junit.Test;
/*
 * @author Christophor.Caruthers
 */
public class JNDILookup_UT extends JndiMockTestBase{
	@Test
	public void testCommonFLPIDSLookup() throws Exception{
		String value = JNDILookup.commonFLPIDSLookup(JNDILookup.JNDI_NAME_SPACE);
		
		Assert.assertEquals("cell/persistent", value);
	}
	@Test
	public void testCommonFSALookup() throws Exception{
		String value = JNDILookup.commonFSALookup(JNDILookup.JNDI_NAME_SPACE);
		
		Assert.assertEquals("cell/persistent", value);
	}
	@Test
	public void testCommonFSADirectLookup() throws Exception{
		String value = JNDILookup.commonFSADirectLookup(JNDILookup.JNDI_NAME_SPACE);
		
		Assert.assertEquals("cell/persistent", value);
	}
	
	@Test
	public void testCommonFSADirectLookup_expectValue() throws Exception{
		context.rebind("cell/persistent/gov/usda/common/environment",
		"local");
		String value = JNDILookup.commonFSADirectLookup(
				"gov/usda/common/environment");
		
		Assert.assertEquals("local", value);
	}
	@Test
	public void testCommonFSADirectLookup_getFromCache() throws Exception{
		context.rebind("cell/persistent/gov/usda/common/environment",
		"remote");
		String value = JNDILookup.commonFSADirectLookup(
				"gov/usda/common/environment");
		
		Assert.assertEquals("remote", value);
	}
	@Test
	public void testCommonFSADirectLookup_clearCache() throws Exception{
		context.rebind("cell/persistent/gov/usda/common/environment",
		"remote");
		JNDILookup.resetJNDICache();
		String value = JNDILookup.commonFSADirectLookup(
				"gov/usda/common/environment");
		
		Assert.assertEquals("remote", value);
	}
}
