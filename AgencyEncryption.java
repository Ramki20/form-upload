package gov.usda.fsa.fcao.flp.flpids.common.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public final class AgencyEncryption {
	public String decode(String myString) throws IOException {
		logDebugInfo("Begin,..");
		return decode(myString, 1);
	}

	public String encode(String myString) {
		logDebugInfo("Begin,..");
		return encode(myString, 1);
	}

	public String decode5(String myString) throws IOException {
		logDebugInfo("Begin,..");
		return decode(myString, TRIES);
	}

	public String encode5(String myString) {
		logDebugInfo("Begin,..");
		return encode(myString, TRIES);
	}

	private String decode(String myString, int times) throws IOException {
		logDebugInfo("Begin,..");
		if (times > 0) {
			byte myByteArray[] = Base64.getDecoder().decode(myString);
			try {
				myString = decode(new String(myByteArray, "ISO8859-1"), times - 1);
			} catch (UnsupportedEncodingException e) {
				log.warn("ISO8859-1 not found, using default instead");
				myString = decode(new String(myByteArray), times - 1);
			}
		}
		return myString;
	}

	private void logDebugInfo(String info) {
		if (log.isDebugEnabled()) {
			log.debug(info);
		}
	}

	private String encode(String myString, int times) {
		logDebugInfo("Begin,..");
		if (times > 0) {
			byte myByteArray[];
			try {
				myByteArray = myString.getBytes("ISO8859-1");
			} catch (UnsupportedEncodingException e) {
				log.warn("Unable to find ISO8859-1, using default instead");
				myByteArray = myString.getBytes();
			}
			myString = encode(Base64.getEncoder().encodeToString(myByteArray), times - 1);
		}
		return myString;
	}

	public static final String VERSION = "$Revision: 1.4 $";
	private static Logger log = LogManager
			.getLogger(gov.usda.fsa.fcao.flp.flpids.common.utilities.AgencyEncryption.class);
	private static final int TRIES = 5;
	private static final AgencyEncryption AGENCY_ENCRYPION = new AgencyEncryption();

	public static AgencyEncryption getInstance() {
		return AGENCY_ENCRYPION;
	}

	private AgencyEncryption() {
	}
}
