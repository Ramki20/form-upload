package gov.usda.fsa.fcao.flp.ola.core.repository;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import gov.usda.fsa.fcao.flp.flpids.common.utilities.DateUtil;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrowerPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationPlain;
import gov.usda.fsa.fcao.flp.ola.core.enums.LoanApplicantType;

@DataJdbcTest
@ContextConfiguration(classes = DataSourceConfigTest.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ApplicationBorrowerPlainRepositoryTest {

	@Autowired
	public DataSource dataSource;

	@Autowired
	public ApplicationBorrowerPlainRepository applicationBorrowerPlainRepository;

	@Autowired
	public ApplicationPlainRepository applicationPlainRepository;

	ApplicationBorrowerPlain applicationBorrower;

	ApplicationPlain applicationPlain;

	@BeforeEach
	public void setUp() throws Exception {

		Assertions.assertTrue(dataSource != null);

		applicationPlain = ApplicationPlain.builder().loanApplicantTypeCode(LoanApplicantType.INDIVIDUAL.getCode())
				.stateLocationAreaFlpCode("1234")
				.officeFlpCode("4567")
				.assignedOfficeFlpCode("6756")
				.applicationNumber(6677)
				.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
				.lastChangeUserName("Test").creationDate(DateUtil.getCurrentDateFromCalendar()).creationUserName("Test")
				.dataStatusCode("A").build();

		applicationPlain = applicationPlainRepository.save(applicationPlain);

		applicationBorrower = ApplicationBorrowerPlain.builder()
				.applicationIdentifier(applicationPlain.getApplicationIdentifier()).businessRegistrationNumber("N")
				.businessRegistrationStateAbbreviation("MO").lastChangeDate(DateUtil.getCurrentDateFromCalendar())
				.lastChangeUserName("Test").creationDate(DateUtil.getCurrentDateFromCalendar()).creationUserName("Test")
				.dataStatusCode("A").coreCustomerIdentifier(123).loanRelationshipTypeCode("PR")
				.residentStateFlpCode("R").residentCountyFlpCode("R")
				.emailAddress("test@gmail.com")
				.maritalStatusCode("SI")
				.businessFormationDate(DateUtil.getCurrentDateFromCalendar()).build();

		applicationBorrower = applicationBorrowerPlainRepository.save(applicationBorrower);
	}

	@AfterEach
	public void tearDown() throws Exception {

		applicationBorrowerPlainRepository.deleteById(applicationBorrower.getApplicationBorrowerIdentifier());
	}

	@Test
	public void findByIdTest() {

		Assertions.assertTrue(applicationBorrower.getApplicationBorrowerIdentifier() != null);
		Assertions.assertTrue(applicationBorrower.getApplicationIdentifier() != null);

		Assertions.assertTrue(applicationBorrower.getBusinessRegistrationNumber().equalsIgnoreCase("N"));
		Assertions.assertTrue(applicationBorrower.getBusinessRegistrationStateAbbreviation().equalsIgnoreCase("MO"));
		Assertions.assertTrue(applicationBorrower.getDataStatusCode().equalsIgnoreCase("A"));
		Assertions.assertTrue(applicationBorrower.getMaritalStatusCode().equalsIgnoreCase("SI"));
		Assertions.assertTrue(applicationBorrower.getBusinessFormationDate() != null);
		Assertions.assertTrue(applicationBorrower.getLastChangeDate() != null);
		Assertions.assertTrue(applicationBorrower.getLastChangeUserName().equalsIgnoreCase("Test"));
		Assertions.assertTrue(applicationBorrower.getCreationDate() != null);
		Assertions.assertTrue(applicationBorrower.getCreationUserName() != null);
		Assertions.assertTrue(applicationBorrower.toString() != null);
	}

	@Test
	public void updateBorrowerIdentificationTest() {

		applicationBorrower = ApplicationBorrowerPlain.builder()
				.applicationIdentifier(applicationPlain.getApplicationIdentifier()).businessRegistrationNumber("A")
				.lastChangeDate(DateUtil.getCurrentDateFromCalendar()).lastChangeUserName("Test")
				.creationDate(DateUtil.getCurrentDateFromCalendar()).creationUserName("Test").dataStatusCode("A")
				.build();

		applicationBorrower = applicationBorrowerPlainRepository.save(applicationBorrower);

		Assertions.assertTrue(applicationBorrower.getApplicationBorrowerIdentifier() != null
				&& applicationBorrower.getBusinessRegistrationNumber().equalsIgnoreCase("A"));

	}

	@Test
	public void ExistByApplicationIdTest() {

		Assertions.assertTrue(dataSource != null);

		Assertions.assertTrue(
				applicationBorrowerPlainRepository.existsById(applicationBorrower.getApplicationBorrowerIdentifier()));
	}

}
