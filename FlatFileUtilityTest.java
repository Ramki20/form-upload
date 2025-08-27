package gov.usda.fsa.fcao.flp.flpids.common.utilities.filehandling;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
@RunWith(PowerMockRunner.class)
@PrepareForTest({FlatFileUtility.class, Date.class})
public class FlatFileUtilityTest {


	@Test
	public void testGenerateFileName() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(2015, Calendar.OCTOBER, 1, 12, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date date = cal.getTime();
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(date);
		FlatFileUtility util = FlatFileUtility.getInstance();
		String name = util.generateFileName("test", null);
		assertEquals("test.20151001T120000000.txt", name);
	}

}
