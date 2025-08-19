package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import gov.usda.fsa.fcao.flp.flpids.common.utilities.DateUtil;

@DisplayName("Testing OperationProfileSubCategory Entity")
class OperationProfileSubCategoryTest {

	OperationProfileSubCategory operationProfileSubCategory;
	
	OperationProfileCategory operationProfileCategory;
	
	@BeforeEach
	public void setUp() throws Exception {
		
		operationProfileCategory = OperationProfileCategory.builder()
				.operationProfileCategoryIdentifier(1231)
				.applicationBorrowerIdentifier(6255)
				.dataStatusCode("A")
				.enterpriseTypeCode("Y")
				.operationCategoryCode("SD")
				.lastChangeUserName("Test").creationUserName("Test").creationDate(DateUtil.stringtoDate("01/01/2023"))
				.lastChangeDate(DateUtil.stringtoDate("01/01/2023"))
				.creationDate(DateUtil.stringtoDate("01/01/2023"))
				.build();
		
		operationProfileSubCategory = OperationProfileSubCategory.builder()
				.operationProfileSubcategoryIdentifier(6736)
				.operatioProfileCategoryIdentifier(operationProfileCategory.getOperationProfileCategoryIdentifier())
				.operationSubcategoryCode("CRE")
				.operationProfileSubcategoryItemCode("FRU")
				.dataStatusCode("A")
				.breedName("Flowers")
				.buyerName("Test")
				.organicIndicator("Y")
				.lastChangeUserName("Test").creationUserName("Test")
				.creationDate(DateUtil.stringtoDate("01/01/2023"))
				.lastChangeDate(DateUtil.stringtoDate("01/01/2023"))
				.build();


		operationProfileCategory.addOperationProfileSubCategory(operationProfileSubCategory);
	}
	
	@Test
	void testOperationProfileSubCategory() {
		Assertions.assertEquals(6736, operationProfileSubCategory.getOperationProfileSubcategoryIdentifier());
		Assertions.assertEquals(1231, operationProfileSubCategory.getOperatioProfileCategoryIdentifier());
		Assertions.assertEquals("A", operationProfileSubCategory.getDataStatusCode());
		Assertions.assertEquals("CRE", operationProfileSubCategory.getOperationSubcategoryCode());

		Assertions.assertEquals("FRU", operationProfileSubCategory.getOperationProfileSubcategoryItemCode());
		Assertions.assertEquals("Flowers", operationProfileSubCategory.getBreedName());
		Assertions.assertEquals("Test", operationProfileSubCategory.getBuyerName());
		Assertions.assertEquals("Y", operationProfileSubCategory.getOrganicIndicator());
	}
	
	@Test
	void testHashcodeAndEquals() {
		OperationProfileSubCategory a = OperationProfileSubCategory.builder().operationProfileSubcategoryIdentifier(1231).build();
		OperationProfileSubCategory b = OperationProfileSubCategory.builder().operationProfileSubcategoryIdentifier(1231).build();
		if (a.equals(b)) {
			Assertions.assertEquals(a.getOperationProfileSubcategoryIdentifier(), b.getOperationProfileSubcategoryIdentifier());
			Assertions.assertEquals(a.hashCode(), b.hashCode());
		}

	}

	@Test
	void testAuditData() {
		OperationProfileSubCategory a = OperationProfileSubCategory.builder().creationDate(new Date())
				.creationUserName("admin").lastChangeDate(new Date()).lastChangeUserName("test").build();

		OperationProfileSubCategory b = OperationProfileSubCategory.builder().creationDate(new Date())
				.creationUserName("admin").lastChangeDate(new Date()).lastChangeUserName("test").build();
		Assertions.assertEquals(a.getCreationUserName(), b.getCreationUserName());
		Assertions.assertEquals(a.getLastChangeUserName(), b.getLastChangeUserName());

		Assertions.assertEquals(a.getLastChangeDate(), b.getLastChangeDate());
		Assertions.assertEquals(a.getCreationDate(), b.getCreationDate());

	}

	@Test
	void testCategoryAndSubCategory() {

		Set<OperationProfileSubCategory> operationProfileSubCategorySet = operationProfileCategory
				.getOperationProfileSubCategorySet();

		Assertions.assertEquals("Y", operationProfileCategory.getEnterpriseTypeCode());

		Optional<OperationProfileSubCategory> operationProfileSubCategoryOptional = operationProfileSubCategorySet
				.stream().findAny();

		if (operationProfileSubCategoryOptional.isPresent()) {

			OperationProfileSubCategory expected = operationProfileSubCategoryOptional.get();

			Assertions.assertEquals(expected.getLastChangeUserName(),
					operationProfileSubCategory.getLastChangeUserName());

			Assertions.assertEquals(expected.getCreationUserName(), operationProfileSubCategory.getCreationUserName());

			Assertions.assertEquals(expected.getOperationProfileSubcategoryIdentifier(),
					operationProfileSubCategory.getOperationProfileSubcategoryIdentifier());
			Assertions.assertEquals(expected.getOperatioProfileCategoryIdentifier(),
					operationProfileSubCategory.getOperatioProfileCategoryIdentifier());
			Assertions.assertEquals(expected.getDataStatusCode(), operationProfileSubCategory.getDataStatusCode());
			Assertions.assertEquals(expected.getOperationSubcategoryCode(),
					operationProfileSubCategory.getOperationSubcategoryCode());

			Assertions.assertEquals(expected.getOperationProfileSubcategoryItemCode(),
					operationProfileSubCategory.getOperationProfileSubcategoryItemCode());
			Assertions.assertEquals(expected.getBreedName(), operationProfileSubCategory.getBreedName());
			Assertions.assertEquals(expected.getBuyerName(), operationProfileSubCategory.getBuyerName());
			Assertions.assertEquals(expected.getOrganicIndicator(), operationProfileSubCategory.getOrganicIndicator());

		}
	}


}
	

