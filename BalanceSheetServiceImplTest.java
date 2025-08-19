package gov.usda.fsa.fcao.flp.ola.core.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetAccountReceivable;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetBreedingLivestock;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetCashEquivalent;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetCrop;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetDueLiability;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetInvestment;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetLivestockProduct;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetMachinery;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetMarketLivestock;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetNonFarmBusiness;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetOtherAsset;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetOtherLiability;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetPrepaidExpense;
import gov.usda.fsa.fcao.flp.ola.core.entity.BalanceSheetRealEstate;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerApplicationBalanceSheet;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerApplicationBalanceSheetLiability;
import gov.usda.fsa.fcao.flp.ola.core.entity.SupportingDocument;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetAccountReceivableRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetBreedingLivestockRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetCashEquivalentRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetCropRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetDueLiabilityRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetInvestmentRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetLivestockProductRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetMachineryRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetMarketLivestockRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetNonFarmBusinessRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetOtherAssetRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetOtherLiabilityRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetPlainRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetPrepaidExpenseRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetRealEstateRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.BalanceSheetRepository;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.BalanceSheetAssetsContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.BalanceSheetContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.BalanceSheetLiabilityContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.SupportingDocumentContract;
import gov.usda.fsa.fcao.flp.ola.core.service.impl.BalanceSheetServiceImpl;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.service.validator.OlaValidator;

@ExtendWith(MockitoExtension.class)
public class BalanceSheetServiceImplTest {

	@Mock
	private OlaAgencyToken token;

	@InjectMocks
	private BalanceSheetServiceImpl balanceSheetServiceImpl = new BalanceSheetServiceImpl();  
	
	@Mock
	private BalanceSheetPlainRepository balanceSheetPlainRepository;

	@Mock
	private BalanceSheetCashEquivalentRepository balanceSheetCashEquivalentRepository;

	@Mock
	private BalanceSheetAccountReceivableRepository balanceSheetAccountReceivableRepository;

	@Mock
	BalanceSheetInvestmentRepository investmentRepository;

	@Mock
	BalanceSheetBreedingLivestockRepository breedingLivestockRepository;

	@Mock
	BalanceSheetMarketLivestockRepository marketLivestockRepository;

	@Mock
	BalanceSheetLivestockProductRepository livestockProductRepository;

	@Mock
	BalanceSheetCropRepository balanceSheetCropRepository;

	@Mock
	BalanceSheetMachineryRepository machineryRepository;

	@Mock
	BalanceSheetRealEstateRepository realEstateRepository;

	@Mock
	BalanceSheetPrepaidExpenseRepository prepaidExpenseRepository;

	@Mock
	BalanceSheetOtherAssetRepository balanceSheetOtherAssetRepository;

	@Mock
	BalanceSheetNonFarmBusinessRepository nonFarmBusinessRepository;

	@Mock
	BalanceSheetDueLiabilityRepository balanceSheetDueLiabilityRepository;

	@Mock
	BalanceSheetOtherLiabilityRepository balanceSheetOtherLiabilityRepository;

	@Mock
	BalanceSheetRepository balanceSheetRepository;

	@Mock
	ISupportingDocumentService supportingDocumentService;

	@Mock
	private OlaValidator<BalanceSheetContract> createBalanceSheetValidator;

	@Mock
	private OlaValidator<BalanceSheetAssetsContract> balanceSheetAssetsContractValidator;

	@Mock
	private OlaValidator<BalanceSheetLiabilityContract> balanceSheetLiabilityContractValidator;

	BalanceSheetAssetsContract contract;

	BorrowerApplicationBalanceSheet balancSheet;
	BalanceSheetCashEquivalent cashEquivalent;
	BalanceSheetAccountReceivable accountReceivable;
	BalanceSheetInvestment investment;
	BalanceSheetBreedingLivestock breedingLiveStocks;
	BalanceSheetMarketLivestock marketLiveStocks;
	BalanceSheetLivestockProduct liveStocksProduct;
	BalanceSheetCrop balanceSheetCrop;
	BalanceSheetMachinery balanceSheetMachinery;
	BalanceSheetRealEstate realEstate;
	BalanceSheetPrepaidExpense prepaidExpense;
	BalanceSheetOtherAsset otherAssets;
	BalanceSheetNonFarmBusiness nonFarmBusiness;

	@BeforeEach
	public void setUp() throws Exception {
		Set<BalanceSheetCashEquivalent> cashSet = new HashSet<BalanceSheetCashEquivalent>();
		cashEquivalent = BalanceSheetCashEquivalent.builder().accountTypeCode("T")
				.borrowerApplicationBalanceSheetIdentifier(100).bankAccountBalanceAmount(new BigDecimal(5000))
				.creationUserName("Creation").dataStatusCode("A").build();
		cashSet.add(cashEquivalent);
		Set<BalanceSheetAccountReceivable> accountRecSet = new HashSet<BalanceSheetAccountReceivable>();
		accountReceivable = BalanceSheetAccountReceivable.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.receivableAmount(new BigDecimal(5000)).creationUserName("Creation").dataStatusCode("D").build();
		accountRecSet.add(accountReceivable);
		Set<BalanceSheetInvestment> investmentSet = new HashSet<BalanceSheetInvestment>();
		investment = BalanceSheetInvestment.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.estimatedMarketValue(new BigDecimal(5000)).creationUserName("Creation").dataStatusCode("D").build();
		investmentSet.add(investment);
		Set<BalanceSheetBreedingLivestock> breedingLiveStockSet = new HashSet<BalanceSheetBreedingLivestock>();
		breedingLiveStocks = BalanceSheetBreedingLivestock.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.breedingLivestockCode("C").creationUserName("Creation").dataStatusCode("D").build();
		breedingLiveStockSet.add(breedingLiveStocks);
		Set<BalanceSheetMarketLivestock> marketLiveStockSet = new HashSet<BalanceSheetMarketLivestock>();
		marketLiveStocks = BalanceSheetMarketLivestock.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.marketLivestockCode("C").creationUserName("Creation").dataStatusCode("D").build();
		marketLiveStockSet.add(marketLiveStocks);
		Set<BalanceSheetLivestockProduct> liveStocksProductSet = new HashSet<BalanceSheetLivestockProduct>();
		liveStocksProduct = BalanceSheetLivestockProduct.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.livestockProductCode("C").creationUserName("Creation").dataStatusCode("D").build();
		liveStocksProductSet.add(liveStocksProduct);
		Set<BalanceSheetCrop> balanceSheetCropSet = new HashSet<BalanceSheetCrop>();
		balanceSheetCrop = BalanceSheetCrop.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.cropAssetTypeCode("HC").creationUserName("Creation").dataStatusCode("D").build();
		balanceSheetCropSet.add(balanceSheetCrop);
		Set<BalanceSheetMachinery> balanceSheetMachinerySet = new HashSet<BalanceSheetMachinery>();
		balanceSheetMachinery = BalanceSheetMachinery.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.machineryTypeCode("M").creationUserName("Creation").dataStatusCode("D").build();
		balanceSheetMachinerySet.add(balanceSheetMachinery);
		Set<BalanceSheetRealEstate> balanceSheetRealEstateSet = new HashSet<BalanceSheetRealEstate>();
		realEstate = BalanceSheetRealEstate.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.realEstateBalanceSheetTypeCode("A").creationUserName("Creation").dataStatusCode("D").build();
		balanceSheetRealEstateSet.add(realEstate);
		Set<BalanceSheetPrepaidExpense> prepaidExpenseSet = new HashSet<BalanceSheetPrepaidExpense>();
		prepaidExpense = BalanceSheetPrepaidExpense.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.prepaidExpenseDescription("A").creationUserName("Creation").dataStatusCode("D").build();
		prepaidExpenseSet.add(prepaidExpense);
		Set<BalanceSheetOtherAsset> otherAssetsSet = new HashSet<BalanceSheetOtherAsset>();
		otherAssets = BalanceSheetOtherAsset.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.otherAssetDescription("A").creationUserName("Creation").dataStatusCode("D").build();
		otherAssetsSet.add(otherAssets);
		Set<BalanceSheetNonFarmBusiness> nonFarmBusinessSet = new HashSet<BalanceSheetNonFarmBusiness>();
		nonFarmBusiness = BalanceSheetNonFarmBusiness.builder().borrowerApplicationBalanceSheetIdentifier(100)
				.businessDescription("A").creationUserName("Creation").dataStatusCode("D").build();
		nonFarmBusinessSet.add(nonFarmBusiness);
		Set<BalanceSheetDueLiability> dueLiabilitySet = new HashSet<BalanceSheetDueLiability>();
		BalanceSheetDueLiability dueLiability = BalanceSheetDueLiability.builder()
				.borrowerApplicationBalanceSheetIdentifier(100).creationUserName("Creation").dataStatusCode("A")
				.dueLiabilityTypeCode("A").build();
		dueLiabilitySet.add(dueLiability);

		balancSheet = BorrowerApplicationBalanceSheet.builder().balanceSheetName("Name")
				.borrowerApplicationBalanceSheetIdentifier(100).applicationBorrowerIdentifier(500)
				.creationUserName("Creation").dataStatusCode("D").cashEquivalentSet(cashSet)
				.accountReceivableSet(accountRecSet).investmentSet(investmentSet)
				.breedingLivestockSet(breedingLiveStockSet).marketLivestockSet(marketLiveStockSet)
				.livestockProductSet(liveStocksProductSet).cropsSet(balanceSheetCropSet)
				.machinerySet(balanceSheetMachinerySet).realEstateSet(balanceSheetRealEstateSet)
				.prepaidExpenseSet(prepaidExpenseSet).otherAssetsSet(otherAssetsSet)
				.nonFarmBusinessSet(nonFarmBusinessSet).dueLiabilitySet(dueLiabilitySet).build();

	}
	
	@Test
	public void saveNewBalanceSheet() {
		BalanceSheetPlain balancSheetPlain = BalanceSheetPlain.builder().balanceSheetName("Name")
				.borrowerApplicationBalanceSheetIdentifier(100).applicationBorrowerIdentifier(500)
				.creationUserName("Creation").dataStatusCode("D").build();
		
		BalanceSheetContract bcontract = BalanceSheetContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheetPlain)
				.sectionCode("CSH").build();

		balanceSheetServiceImpl.save(token, bcontract);
		
		verify(balanceSheetPlainRepository, times(1)).findByApplicationAndCoreIdentifier(
				bcontract.getApplicationIdentifier(), bcontract.getCoreCustomerIdentifier()); 

		verify(balanceSheetPlainRepository, times(1)).save(bcontract.getBalanceSheet());

	}
	
	@Test
	public void saveUpdatedBalanceSheet() {
		BalanceSheetPlain balancSheetPlain = BalanceSheetPlain.builder().balanceSheetName("Name")
				.borrowerApplicationBalanceSheetIdentifier(100).applicationBorrowerIdentifier(500)
				.creationUserName("Creation").dataStatusCode("B").build();
		
		BalanceSheetContract bcontract = BalanceSheetContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheetPlain)
				.sectionCode("CSH").build();
		
		Date myDate = new Date();

		BalanceSheetPlain savedBalanceSheetSaved = BalanceSheetPlain.builder().balanceSheetName("Name")
				.borrowerApplicationBalanceSheetIdentifier(100).applicationBorrowerIdentifier(500)
				.creationDate(myDate).dataStatusCode("A")
				.creationUserName("Creation").build();
		
		when(balanceSheetPlainRepository.findByApplicationAndCoreIdentifier(
				bcontract.getApplicationIdentifier(), bcontract.getCoreCustomerIdentifier())).thenReturn(savedBalanceSheetSaved);

		balanceSheetServiceImpl.save(token, bcontract);
		
		verify(balanceSheetPlainRepository, times(1)).findByApplicationAndCoreIdentifier(
				bcontract.getApplicationIdentifier(), bcontract.getCoreCustomerIdentifier()); 

		verify(balanceSheetPlainRepository, times(1)).save(savedBalanceSheetSaved);
		
	}

	@Test
	public void saveNewCashEquialentData() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("CSH").build();
		Set<BalanceSheetCashEquivalent> existing = new HashSet<BalanceSheetCashEquivalent>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetCashEquivalentRepository.findExistingCashEquivalent(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetCashEquivalentRepository, times(1)).delete(100);
		verify(balanceSheetCashEquivalentRepository, times(1)).save(cashEquivalent);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveExistingCashEquialentData() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("CSH").build();
		Set<BalanceSheetCashEquivalent> existing = new HashSet<BalanceSheetCashEquivalent>();
		Date myDate = new Date();
		BalanceSheetCashEquivalent cashEquivalentExisting = BalanceSheetCashEquivalent.builder().accountTypeCode("T")
				.borrowerApplicationBalanceSheetIdentifier(100).bankAccountBalanceAmount(new BigDecimal(5000))
				.creationDate(myDate)
				.creationUserName("Creation").dataStatusCode("A").build();
		existing.add(cashEquivalentExisting);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetCashEquivalentRepository.findExistingCashEquivalent(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetCashEquivalentRepository, times(1)).delete(100);
		verify(balanceSheetCashEquivalentRepository, times(1)).save(cashEquivalentExisting);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveAccountReceivableDataNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("REC").build();
		Set<BalanceSheetAccountReceivable> existing = new HashSet<BalanceSheetAccountReceivable>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetAccountReceivableRepository.findExistingBalanceSheetAccountReceivable(100))
				.thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetAccountReceivableRepository, times(1)).delete(100);
		verify(balanceSheetAccountReceivableRepository, times(1)).save(accountReceivable);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveAccountReceivableDataUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("REC").build();
		Set<BalanceSheetAccountReceivable> existing = new HashSet<BalanceSheetAccountReceivable>();
		existing.add(accountReceivable);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetAccountReceivableRepository.findExistingBalanceSheetAccountReceivable(100))
				.thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetAccountReceivableRepository, times(1)).delete(100);
		verify(balanceSheetAccountReceivableRepository, times(1)).save(accountReceivable);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveStocksNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("SBS").build();
		Set<BalanceSheetInvestment> existing = new HashSet<BalanceSheetInvestment>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(investmentRepository.findExistingInvestments(100, "S")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(investmentRepository, times(1)).delete(100, "S");
		verify(investmentRepository, times(1)).save(investment);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveStocksupdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("SBS").build();
		Set<BalanceSheetInvestment> existing = new HashSet<BalanceSheetInvestment>();
		existing.add(investment);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(investmentRepository.findExistingInvestments(100, "S")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(investmentRepository, times(1)).delete(100, "S");
		verify(investmentRepository, times(1)).save(investment);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveRetirementsNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("RET").build();
		Set<BalanceSheetInvestment> existing = new HashSet<BalanceSheetInvestment>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(investmentRepository.findExistingInvestments(100, "R")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(investmentRepository, times(1)).delete(100, "R");
		verify(investmentRepository, times(1)).save(investment);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveRetirementsUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("RET").build();
		Set<BalanceSheetInvestment> existing = new HashSet<BalanceSheetInvestment>();
		existing.add(investment);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(investmentRepository.findExistingInvestments(100, "R")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(investmentRepository, times(1)).delete(100, "R");
		verify(investmentRepository, times(1)).save(investment);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveBreedingLiveStocksNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("BLK").build();
		Set<BalanceSheetBreedingLivestock> existing = new HashSet<BalanceSheetBreedingLivestock>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(breedingLivestockRepository.findExistingBreedingLivestocks(100, "C")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(breedingLivestockRepository, times(1)).delete(100);
		verify(breedingLivestockRepository, times(1)).save(breedingLiveStocks);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveBreedingLiveStocksUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("BLK").build();
		Set<BalanceSheetBreedingLivestock> existing = new HashSet<BalanceSheetBreedingLivestock>();
		existing.add(breedingLiveStocks);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(breedingLivestockRepository.findExistingBreedingLivestocks(100, "C")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(breedingLivestockRepository, times(1)).delete(100);
		verify(breedingLivestockRepository, times(1)).save(breedingLiveStocks);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveMarketLiveStocksNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("MSS").build();
		Set<BalanceSheetMarketLivestock> existing = new HashSet<BalanceSheetMarketLivestock>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(marketLivestockRepository.findExistingMarketLivestocks(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(marketLivestockRepository, times(1)).delete(100);
		verify(marketLivestockRepository, times(1)).save(marketLiveStocks);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveMarketLiveStocksUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("MSS").build();
		Set<BalanceSheetMarketLivestock> existing = new HashSet<BalanceSheetMarketLivestock>();
		existing.add(marketLiveStocks);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(marketLivestockRepository.findExistingMarketLivestocks(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(marketLivestockRepository, times(1)).delete(100);
		verify(marketLivestockRepository, times(1)).save(marketLiveStocks);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveLiveStockProductsNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("LPS").build();
		Set<BalanceSheetLivestockProduct> existing = new HashSet<BalanceSheetLivestockProduct>();
		existing.add(liveStocksProduct);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(livestockProductRepository.findExistingLivestockProducts(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(livestockProductRepository, times(1)).delete(100);
		verify(livestockProductRepository, times(1)).save(liveStocksProduct);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveLiveStockProductsUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("LPS").build();
		Set<BalanceSheetLivestockProduct> existing = new HashSet<BalanceSheetLivestockProduct>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(livestockProductRepository.findExistingLivestockProducts(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(livestockProductRepository, times(1)).delete(100);
		verify(livestockProductRepository, times(1)).save(liveStocksProduct);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveHarvestCropsDataNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("HCS").build();
		Set<BalanceSheetCrop> existing = new HashSet<BalanceSheetCrop>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetCropRepository.findExistingCrops(100, "HC")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetCropRepository, times(1)).deleteCrops(100, "HC");
		verify(balanceSheetCropRepository, times(1)).save(balanceSheetCrop);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveHarvestCropsDataUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("HCS").build();
		Set<BalanceSheetCrop> existing = new HashSet<BalanceSheetCrop>();
		existing.add(balanceSheetCrop);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetCropRepository.findExistingCrops(100, "HC")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetCropRepository, times(1)).deleteCrops(100, "HC");
		verify(balanceSheetCropRepository, times(1)).save(balanceSheetCrop);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveGrowingCropsDataNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("GCS").build();
		Set<BalanceSheetCrop> existing = new HashSet<BalanceSheetCrop>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetCropRepository.findExistingCrops(100, "HC")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetCropRepository, times(1)).deleteCrops(100, "GC");
		verify(balanceSheetCropRepository, times(1)).save(balanceSheetCrop);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void savesaveMachineriesDataNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("FMS").build();
		Set<BalanceSheetMachinery> existing = new HashSet<BalanceSheetMachinery>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(machineryRepository.findExistingMachineries(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(machineryRepository, times(1)).delete(100, "M");
		verify(machineryRepository, times(1)).save(balanceSheetMachinery);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void savesaveMachineriesDataUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("FMS").build();
		Set<BalanceSheetMachinery> existing = new HashSet<BalanceSheetMachinery>();
		existing.add(balanceSheetMachinery);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(machineryRepository.findExistingMachineries(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(machineryRepository, times(1)).delete(100, "M");
		verify(machineryRepository, times(1)).save(balanceSheetMachinery);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveVechcleMachineriesData() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("FVS").build();
		Set<BalanceSheetMachinery> existing = new HashSet<BalanceSheetMachinery>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(machineryRepository.findExistingMachineries(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(machineryRepository, times(1)).delete(100, "V");
		verify(machineryRepository, times(1)).save(balanceSheetMachinery);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void savePersonalMachineriesData() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("PVS").build();
		Set<BalanceSheetMachinery> existing = new HashSet<BalanceSheetMachinery>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(machineryRepository.findExistingMachineries(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(machineryRepository, times(1)).delete(100, "R");
		verify(machineryRepository, times(1)).save(balanceSheetMachinery);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveFarmRealEstateData() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("REF").build();
		Set<BalanceSheetRealEstate> existing = new HashSet<BalanceSheetRealEstate>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(realEstateRepository.findExistingRealEstates(100, "FR")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(realEstateRepository, times(1)).delete(100, "FR");
		verify(realEstateRepository, times(1)).save(realEstate);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveFarmRealEstateDataUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("REF").build();
		Set<BalanceSheetRealEstate> existing = new HashSet<BalanceSheetRealEstate>();
		existing.add(realEstate);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(realEstateRepository.findExistingRealEstates(100, "FR")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(realEstateRepository, times(1)).delete(100, "FR");
		verify(realEstateRepository, times(1)).save(realEstate);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveNonFarmRealEstateData() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("REN").build();
		Set<BalanceSheetRealEstate> existing = new HashSet<BalanceSheetRealEstate>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(realEstateRepository.findExistingRealEstates(100, "NR")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(realEstateRepository, times(1)).delete(100, "NR");
		verify(realEstateRepository, times(1)).save(realEstate);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void savePrepaidDataNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("PRE").build();
		Set<BalanceSheetPrepaidExpense> existing = new HashSet<BalanceSheetPrepaidExpense>();
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(prepaidExpenseRepository.findExistingPrepaidExpenses(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(prepaidExpenseRepository, times(1)).delete(100);
		verify(prepaidExpenseRepository, times(1)).save(prepaidExpense);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void savePrepaidDataUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("PRE").build();
		Set<BalanceSheetPrepaidExpense> existing = new HashSet<BalanceSheetPrepaidExpense>();
		existing.add(prepaidExpense);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(prepaidExpenseRepository.findExistingPrepaidExpenses(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(prepaidExpenseRepository, times(1)).delete(100);
		verify(prepaidExpenseRepository, times(1)).save(prepaidExpense);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveOtherAssets() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("ONF").build();
		Set<BalanceSheetOtherAsset> existing = new HashSet<BalanceSheetOtherAsset>();
//		existing.add(liveStocksProduct);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetOtherAssetRepository.findExistingOtherAssets(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetOtherAssetRepository, times(1)).delete(100);
		verify(balanceSheetOtherAssetRepository, times(1)).save(otherAssets);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveOtherAssetsUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("ONF").build();
		Set<BalanceSheetOtherAsset> existing = new HashSet<BalanceSheetOtherAsset>();
		existing.add(otherAssets);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetOtherAssetRepository.findExistingOtherAssets(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetOtherAssetRepository, times(1)).delete(100);
		verify(balanceSheetOtherAssetRepository, times(1)).save(otherAssets);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveNonFarmBusinsessNew() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("PNF").build();
		Set<BalanceSheetNonFarmBusiness> existing = new HashSet<BalanceSheetNonFarmBusiness>();
//		existing.add(liveStocksProduct);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(nonFarmBusinessRepository.findExistingNonFarmBusinesses(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(nonFarmBusinessRepository, times(1)).delete(100);
		verify(nonFarmBusinessRepository, times(1)).save(nonFarmBusiness);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveNonFarmBusinsessUpdate() {
		contract = BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).applicationIdentifier(100)
				.applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).balanceSheet(balancSheet)
				.sectionCode("PNF").build();
		Set<BalanceSheetNonFarmBusiness> existing = new HashSet<BalanceSheetNonFarmBusiness>();
		existing.add(nonFarmBusiness);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(nonFarmBusinessRepository.findExistingNonFarmBusinesses(100)).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(nonFarmBusinessRepository, times(1)).delete(100);
		verify(nonFarmBusinessRepository, times(1)).save(nonFarmBusiness);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveDueLiabilityData() {

		Set<BalanceSheetDueLiability> dueLiabilitySet = new HashSet<BalanceSheetDueLiability>();

		BalanceSheetDueLiability dueLiability = BalanceSheetDueLiability.builder()
				.borrowerApplicationBalanceSheetIdentifier(100).creationUserName("Creation").dataStatusCode("A")
				.dueLiabilityTypeCode("A").build();
		dueLiabilitySet.add(dueLiability);

		BorrowerApplicationBalanceSheetLiability liabilityBalanceSheet = BorrowerApplicationBalanceSheetLiability
				.builder().applicationBorrowerIdentifier(500).dataStatusCode("A").dueLiabilitySet(dueLiabilitySet)
				.borrowerApplicationBalanceSheetIdentifier(100).build();

		BalanceSheetLiabilityContract contract = BalanceSheetLiabilityContract.builder()
				.coreCustomerIdentifier(10039293).applicationIdentifier(100).applicationBorrowerIdentifier(500)
				.sectionCode("C").olaToken(token).balanceSheet(liabilityBalanceSheet).sectionCode("FAS").build();

		Set<BalanceSheetDueLiability> existing = new HashSet<BalanceSheetDueLiability>();
//		existing.add(nonFarmBusiness);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetDueLiabilityRepository.findExistingBalanceSheetDueLiability(100, "A")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetDueLiabilityRepository, times(1)).delete(100, "A");
		verify(balanceSheetDueLiabilityRepository, times(1)).save(dueLiability);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveDueLiabilityDataUpdate() {

		Set<BalanceSheetDueLiability> dueLiabilitySet = new HashSet<BalanceSheetDueLiability>();

		BalanceSheetDueLiability dueLiability = BalanceSheetDueLiability.builder()
				.borrowerApplicationBalanceSheetIdentifier(100).creationUserName("Creation").dataStatusCode("A")
				.dueLiabilityTypeCode("A").build();
		dueLiabilitySet.add(dueLiability);

		BorrowerApplicationBalanceSheetLiability liabilityBalanceSheet = BorrowerApplicationBalanceSheetLiability
				.builder().applicationBorrowerIdentifier(500).dataStatusCode("A").dueLiabilitySet(dueLiabilitySet)
				.borrowerApplicationBalanceSheetIdentifier(100).build();

		BalanceSheetLiabilityContract contract = BalanceSheetLiabilityContract.builder()
				.coreCustomerIdentifier(10039293).applicationIdentifier(100).applicationBorrowerIdentifier(500)
				.sectionCode("C").olaToken(token).balanceSheet(liabilityBalanceSheet).sectionCode("FAS").build();

		Set<BalanceSheetDueLiability> existing = new HashSet<BalanceSheetDueLiability>();
		existing.add(dueLiability);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetDueLiabilityRepository.findExistingBalanceSheetDueLiability(100, "A")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetDueLiabilityRepository, times(1)).delete(100, "A");
		verify(balanceSheetDueLiabilityRepository, times(1)).save(dueLiability);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveDueLiabilityDataWithDocuments() {

		Set<BalanceSheetDueLiability> dueLiabilitySet = new HashSet<BalanceSheetDueLiability>();
		Set<SupportingDocument> documentSet = new HashSet<SupportingDocument>();
		SupportingDocument document = SupportingDocument.builder().documentName("Document").creationUserName("Creation")
				.dataStatusCode("A").olaDocumentTypeCode("A").build();
		documentSet.add(document);
		BalanceSheetDueLiability dueLiability = BalanceSheetDueLiability.builder()
				.borrowerApplicationBalanceSheetIdentifier(100).creationUserName("Creation").dataStatusCode("A")
				.dueLiabilityTypeCode("A").build();
		dueLiability.addSupportingDocument(document);
		dueLiabilitySet.add(dueLiability);

		BorrowerApplicationBalanceSheetLiability liabilityBalanceSheet = BorrowerApplicationBalanceSheetLiability
				.builder().applicationBorrowerIdentifier(500).dataStatusCode("A").dueLiabilitySet(dueLiabilitySet)
				.borrowerApplicationBalanceSheetIdentifier(100).build();

		BalanceSheetLiabilityContract contract = BalanceSheetLiabilityContract.builder()
				.coreCustomerIdentifier(10039293).applicationIdentifier(100).applicationBorrowerIdentifier(500)
				.sectionCode("C").olaToken(token).balanceSheet(liabilityBalanceSheet).sectionCode("FAS").build();

		Set<BalanceSheetDueLiability> existing = new HashSet<BalanceSheetDueLiability>();
		existing.add(dueLiability);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetDueLiabilityRepository.findExistingBalanceSheetDueLiability(100, "A")).thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);
		when(supportingDocumentService.save(Mockito.any(SupportingDocumentContract.class))).thenReturn(documentSet);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetDueLiabilityRepository, times(1)).delete(100, "A");
//		verify(balanceSheetDueLiabilityRepository).save(dueLiability);

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	@Test
	public void saveOtherLiabilityDataWithDocuments() {

		Set<BalanceSheetOtherLiability> otherLiabilitySet = new HashSet<BalanceSheetOtherLiability>();
		Set<SupportingDocument> documentSet = new HashSet<SupportingDocument>();
		SupportingDocument document = SupportingDocument.builder().documentName("Document").creationUserName("Creation")
				.dataStatusCode("A").olaDocumentTypeCode("A").build();
		documentSet.add(document);
		BalanceSheetOtherLiability otherLiability = BalanceSheetOtherLiability.builder()
				.borrowerApplicationBalanceSheetIdentifier(100).creationUserName("Creation").dataStatusCode("A")
				.liabilityTypeCode("LL").build();
		otherLiability.addSupportingDocument(document);
		otherLiabilitySet.add(otherLiability);

		BorrowerApplicationBalanceSheetLiability liabilityBalanceSheet = BorrowerApplicationBalanceSheetLiability
				.builder().applicationBorrowerIdentifier(500).dataStatusCode("A").otherLiabilitySet(otherLiabilitySet)
				.borrowerApplicationBalanceSheetIdentifier(100).build();

		BalanceSheetLiabilityContract contract = BalanceSheetLiabilityContract.builder()
				.coreCustomerIdentifier(10039293).applicationIdentifier(100).applicationBorrowerIdentifier(500)
				.sectionCode("C").olaToken(token).balanceSheet(liabilityBalanceSheet).sectionCode("LTS").build();

		Set<BalanceSheetOtherLiability> existing = new HashSet<BalanceSheetOtherLiability>();
		existing.add(otherLiability);
		Optional<BorrowerApplicationBalanceSheet> optional = Optional.of(balancSheet);
		when(balanceSheetOtherLiabilityRepository.findExistingBalanceSheetOtherLiability(100, "LL"))
				.thenReturn(existing);
		when(balanceSheetRepository.findByApplicationBorrower(500)).thenReturn(optional);
		when(supportingDocumentService.save(Mockito.any(SupportingDocumentContract.class))).thenReturn(documentSet);

		BorrowerApplicationBalanceSheet result = balanceSheetServiceImpl.save(token, contract);

		verify(balanceSheetOtherLiabilityRepository, times(1)).delete(100, "LL");

		Assertions.assertNotNull(result);
		Assertions.assertTrue(result.getBorrowerApplicationBalanceSheetIdentifier() > 0);
	}

	/*
	 * @Test public void testOLAServiceExceptionThrownForAsset() throws Exception {
	 * contract =
	 * BalanceSheetAssetsContract.builder().coreCustomerIdentifier(10039293).
	 * applicationIdentifier(100)
	 * .applicationBorrowerIdentifier(500).sectionCode("C").olaToken(token).
	 * balanceSheet(balancSheet) .sectionCode("ZZZ").build();
	 * 
	 * try { balanceSheetServiceImpl.save(token, contract); } catch
	 * (OLAServiceException e) {
	 * Assertions.assertEquals("Section code not found to save the assets data.",
	 * e.getMessage()); } }
	 */

	/*
	 * @Test public void testOLAServiceExceptionThrownForLiability() throws
	 * Exception { BalanceSheetLiabilityContract contract =
	 * BalanceSheetLiabilityContract.builder()
	 * .coreCustomerIdentifier(10039293).applicationIdentifier(100).
	 * applicationBorrowerIdentifier(500)
	 * .sectionCode("C").olaToken(token).sectionCode("ZZZ").build();
	 * 
	 * try { balanceSheetServiceImpl.save(token, contract); } catch
	 * (OLAServiceException e) { Assertions.
	 * assertEquals("Section code not found to save the liabilities data.",
	 * e.getMessage()); } }
	 */

}