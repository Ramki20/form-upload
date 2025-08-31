.thenReturn(mockedState);

        // Act
        State state = mrtProxyBusinessService.retrieveStateByFlpFipsCode(flpCode);

        // Assert
        assertNull("State should be null for empty code", state);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveStateByFlpFipsCode_exception() throws Exception {
        // Arrange
        String flpCode = "ERROR_CODE";
        
        when(mockStateDataServiceProxy.byFlpFipsCode(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveStateByFlpFipsCode(flpCode);
    }

    // ===== FSA SERVICE CENTER TESTS =====

    @Test
    public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr() throws Exception {
        // Arrange
        String stateAbbr = "MO";
        List<Office> serviceCenterOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOffices.add(office);

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq(stateAbbr),
            eq(FlpOfficeProperties.id),
            eq(FlpOfficeProperties.mailingZipCode),
            eq(FlpOfficeProperties.mailingAddrInfoLine),
            eq(FlpOfficeProperties.mailingAddrLine),
            eq(FlpOfficeProperties.mailingCity),
            eq(FlpOfficeProperties.mailingStateAbbrev),
            eq(FlpOfficeProperties.stateFipsCode),
            eq(FlpOfficeProperties.stateName),
            eq(FlpOfficeProperties.officeCode),
            eq(FlpOfficeProperties.name),
            eq(FlpOfficeProperties.countyName),
            eq(FlpOfficeProperties.locCityName)))
            .thenReturn(serviceCenterOffices);

        // Act
        List<Office> offices = mrtProxyBusinessService.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);

        // Assert
        assertFalse("Offices should not be empty", offices.isEmpty());
        assertEquals("Should return expected office", office, offices.get(0));
    }

    @Test
    public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr_noData() throws Exception {
        // Arrange
        String stateAbbr = "N/A";
        List<Office> serviceCenterOffices = new ArrayList<Office>();

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq(stateAbbr), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(serviceCenterOffices);

        // Act
        List<Office> offices = mrtProxyBusinessService.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Offices should be empty for invalid state", offices.isEmpty());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr_exception() throws Exception {
        // Arrange
        String stateAbbr = "ERROR_STATE";
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq(stateAbbr), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);
    }

    // ===== LOCATION AREA TESTS =====

    @Test
    public void test_retrieveFlpAreaListByStateAbbr() throws Exception {
        // Arrange
        String stateAbbr = "MO";
        List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();
        LocationArea locArea = new LocationArea();
        locArea.setStateLocationAreaCode("30001");
        locArea.setName("Test County");
        fLPLocationAreaMRTBusinessObjectList.add(locArea);

        when(mockLocationAreaDataServiceProxy.flpByStateAbbr(
            eq(stateAbbr),
            eq(FlpLocationAreaProperties.stateCode),
            eq(FlpLocationAreaProperties.stateLocationAreaCode),
            eq(FlpLocationAreaProperties.stateName),
            eq(FlpLocationAreaProperties.shortName),
            eq(FlpLocationAreaProperties.stateRefId)))
            .thenReturn(fLPLocationAreaMRTBusinessObjectList);

        // Act
        List<LocationArea> locationAreaList = mrtProxyBusinessService.retrieveFlpAreaListByStateAbbr(stateAbbr);
        
        // Assert
        assertFalse("Location area list should not be empty", locationAreaList.isEmpty());
        assertEquals("Should return expected location area", locArea, locationAreaList.get(0));
    }

    @Test
    public void test_retrieveFlpAreaListByStateAbbr_noData() throws Exception {
        // Arrange
        String stateAbbr = "N/A";
        List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();

        when(mockLocationAreaDataServiceProxy.flpByStateAbbr(
            eq(stateAbbr), any(), any(), any(), any(), any()))
            .thenReturn(fLPLocationAreaMRTBusinessObjectList);

        // Act
        List<LocationArea> locationAreaList = mrtProxyBusinessService.retrieveFlpAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Location area list should be empty", locationAreaList.isEmpty());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpAreaListByStateAbbr_BusinessServiceBindingExceptionCovered() throws Exception {
        // Arrange
        when(mockLocationAreaDataServiceProxy.flpByStateAbbr(
            eq("AL"), any(), any(), any(), any(), any()))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
        
        // Act
        mrtProxyBusinessService.retrieveFlpAreaListByStateAbbr("AL");
    }

    @Test
    public void test_retrieveFlpLocationAreaCodesByServiceCenterOffices() throws Exception {
        // Arrange
        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        LocationArea object = new LocationArea();
        LocationArea object1 = new LocationArea();
        LocationArea object2 = new LocationArea();
        LocationArea object3 = new LocationArea();
        mockedLocationAreaObject.add(object);
        mockedLocationAreaObject.add(object1);
        mockedLocationAreaObject.add(object2);
        mockedLocationAreaObject.add(object3);
        
        String[] serviceCenters = {"04001"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(serviceCenters))
            .thenReturn(mockedLocationAreaObject);
            
        // Act
        List<LocationArea> fLPLocationAreListReturned = mrtProxyBusinessService.retrieveFlpLocationAreaCodesByServiceCenterOffices(serviceCenters);
        
        // Assert
        assertNotNull("Location area list should not be null", fLPLocationAreListReturned);
        assertEquals("Should return 4 location areas", 4, fLPLocationAreListReturned.size());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByFlpCodeList(serviceCenters);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpLocationAreaCodesByServiceCenterOffices_DLSBusinessFatalException() throws Exception {
        // Arrange
        String[] serviceCenters = {"04001"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(serviceCenters))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
        
        // Act
        mrtProxyBusinessService.retrieveFlpLocationAreaCodesByServiceCenterOffices(serviceCenters);
    }

    // ===== FSA STATE TESTS =====

    @Test
    public void test_retrieveFSAStateList() throws Exception {
        // Arrange
        List<State> mockedStateList = new ArrayList<State>();
        State state = new State();
        state.setCode("69");
        state.setName("Missouri");
        state.setAbbreviation("MO");
        mockedStateList.add(state);

        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
        
        when(mockStateDataServiceProxy.allFSA(
            eq(FsaStateProperties.code),
            eq(FsaStateProperties.name),
            eq(FsaStateProperties.abbreviation)))
            .thenReturn(mockedStateList);

        // Act
        List<MrtLookUpBO> mrtLookupBO = mrtProxyBusinessService.retrieveFSAStateList(retrieveFSAStateListBC);

        // Assert
        assertNotNull("Result should not be null", mrtLookupBO);
        assertEquals("Should return one state", 1, mrtLookupBO.size());
        assertNotNull("Code should not be null", mrtLookupBO.get(0).getCode());
        assertTrue("Code should match", "69".equalsIgnoreCase(mrtLookupBO.get(0).getCode()));
        
        verify(mockStateDataServiceProxy, times(1)).allFSA(
            eq(FsaStateProperties.code),
            eq(FsaStateProperties.name),
            eq(FsaStateProperties.abbreviation));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSAStateList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
        
        when(mockStateDataServiceProxy.allFSA(any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveFSAStateList(retrieveFSAStateListBC);
    }

    @Test
    public void test_retrieveFSAStateMap() throws Exception {
        // Arrange
        List<State> mockStateList = new ArrayList<State>();
        State state = new State();
        state.setCode("69");
        state.setName("Missouri");
        state.setAbbreviation("MO");
        mockStateList.add(state);

        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());

        when(mockStateDataServiceProxy.allFSA(
            eq(FsaStateProperties.code),
            eq(FsaStateProperties.name),
            eq(FsaStateProperties.abbreviation)))
            .thenReturn(mockStateList);

        // Act
        Map<String, MrtLookUpBO> mrtLookupBOMapReturned = mrtProxyBusinessService.retrieveFSAStateMap(retrieveFSAStateListBC);
        
        // Assert
        assertNotNull("Map should not be null", mrtLookupBOMapReturned);
        assertEquals("Map should contain one entry", 1, mrtLookupBOMapReturned.size());
        assertTrue("Map should contain expected key", mrtLookupBOMapReturned.containsKey("69"));
        
        MrtLookUpBO lookupBO = mrtLookupBOMapReturned.get("69");
        assertEquals("State code should match", "69", lookupBO.getCode());
        assertEquals("State name should match", "Missouri", lookupBO.getDescription());
        assertEquals("State abbreviation should match", "MO", lookupBO.getRefenceIdentifier());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSAStateMap_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
        
        when(mockStateDataServiceProxy.allFSA(any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveFSAStateMap(retrieveFSAStateListBC);
    }

    // ===== FSA COUNTY TESTS =====

    @Test
    public void test_retrieveFSACountyList() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("40001");
        item.setCode("C");
        item.setName("TestName");
        item.setId(1);
        flpOfficeList.add(item);
        
        RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
        
        when(mockLocationAreaDataServiceProxy.agByStateFsa(
            eq("CA"),
            eq(FsaLocationAreaProperties.id),
            eq(FsaLocationAreaProperties.code),
            eq(FsaLocationAreaProperties.name),
            eq(FsaLocationAreaProperties.shortName),
            eq(FsaLocationAreaProperties.categoryName)))
            .thenReturn(flpOfficeList);
            
        // Act
        List<MrtLookUpBO> mrtLookupList = mrtProxyBusinessService.retrieveFSACountyList(retrieveFsaCountyListBC);
        
        // Assert
        assertNotNull("Result should not be null", mrtLookupList);
        assertEquals("Should return one county", 1, mrtLookupList.size());
        assertEquals("Reference identifier should match", "1", mrtLookupList.get(0).getRefenceIdentifier());
        assertEquals("Code should match", "C", mrtLookupList.get(0).getCode());
        assertEquals("Description should match", "TestName", mrtLookupList.get(0).getDescription());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSACountyList_BusinessServiceBindingExceptionCovered() throws Exception {
        // Arrange
        RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
        
        when(mockLocationAreaDataServiceProxy.agByStateFsa(any(), any(), any(), any(), any(), any()))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
        
        // Act
        mrtProxyBusinessService.retrieveFSACountyList(retrieveFsaCountyListBC);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSACountyList_ThrowableCovered() throws Exception {
        // Arrange
        RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
        
        when(mockLocationAreaDataServiceProxy.agByStateFsa(any(), any(), any(), any(), any(), any()))
            .thenThrow(new NullPointerException("TestException"));
        
        // Act
        mrtProxyBusinessService.retrieveFSACountyList(retrieveFsaCountyListBC);
    }

    // ===== INTEREST RATE BY DATE RANGE TESTS =====

    @Test
    public void test_retrieveIntRatesByRateTypeIdLstAndDtRng() throws Exception {
        // Arrange
        List<String> typeIds = new ArrayList<String>();
        typeIds.add("1");
        
        List<InterestRate> interestRates = new ArrayList<InterestRate>();
        InterestRate interestRate = new InterestRate();
        interestRate.setId(1);
        interestRate.setIntRate(BigDecimal.ONE);
        interestRates.add(interestRate);
        
        Date fromDate = Calendar.getInstance().getTime();
        Date toDate = Calendar.getInstance().getTime();
        
        when(mockInterestRateDataServiceProxy.byTypeIdListAndDateRange(
            anyList(), eq(fromDate), eq(toDate)))
            .thenReturn(interestRates);

        // Act
        List<InterestRate> interestRateListReturned = mrtProxyBusinessService.retrieveIntRatesByRateTypeIdLstAndDtRng(typeIds, fromDate, toDate);
        
        // Assert
        assertNotNull("Interest rate list should not be null", interestRateListReturned);
        assertEquals("Should return one interest rate", 1, interestRateListReturned.size());
        assertEquals("Interest rate should match", interestRate, interestRateListReturned.get(0));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveIntRatesByRateTypeIdLstAndDtRng_DLSBusinessFatalException() throws Exception {
        // Arrange
        List<String> typeIds = new ArrayList<String>();
        typeIds.add("1");
        Date fromDate = Calendar.getInstance().getTime();
        Date toDate = Calendar.getInstance().getTime();
        
        when(mockInterestRateDataServiceProxy.byTypeIdListAndDateRange(anyList(), eq(fromDate), eq(toDate)))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveIntRatesByRateTypeIdLstAndDtRng(typeIds, fromDate, toDate);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveInterestRates_DLSBusinessFatalException() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Integer[] typeIds = {50500};
        
        when(mockInterestRateDataServiceProxy.byTypeIdListAndDate(typeIds, date.getTime()))
            .thenThrow(new RuntimeException("Service error"));
            
        // Act
        mrtProxyBusinessService.retrieveInterestRates(typeIds, date.getTime());
    }

    // ===== EMPLOYEE ORG CHART TESTS =====

    @Test
    public void test_retrieveOrgChartsByEmployeeId() throws Exception {
        // Arrange
        List<EmployeeOrgChart> employeeOrgChartList = new ArrayList<EmployeeOrgChart>();
        EmployeeOrgChart employeeOrgChart = new EmployeeOrgChart();
        employeeOrgChart.setEmployeeId("1");
        employeeOrgChart.setNoteText("Test");
        employeeOrgChartList.add(employeeOrgChart);
        
        when(mockEmployeeDataServiceProxy.orgChartsByEmployeeId("1"))
            .thenReturn(employeeOrgChartList);
            
        // Act
        List<EmployeeOrgChart> employeeOrgChartListReturned = mrtProxyBusinessService.retrieveOrgChartsByEmployeeId("1");
        
        // Assert
        assertNotNull("Org chart list should not be null", employeeOrgChartListReturned);
        assertEquals("Should return one org chart", 1, employeeOrgChartListReturned.size());
        assertEquals("Employee ID should match", "1", employeeOrgChartListReturned.get(0).getEmployeeId());
        
        verify(mockEmployeeDataServiceProxy, times(1)).orgChartsByEmployeeId("1");
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveOrgChartsByEmployeeId_ExceptionCovered() throws Exception {
        // Arrange
        when(mockEmployeeDataServiceProxy.orgChartsByEmployeeId("1"))
            .thenThrow(new RuntimeException("Service error"));
            
        // Act
        mrtProxyBusinessService.retrieveOrgChartsByEmployeeId("1");
    }

    // ===== FLP CODE LIST TESTS =====

    @Test
    public void test_flpByFlpCodeList() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("40001");
        item.setCode("C");
        item.setName("TestName");
        item.setId(1);
        flpOfficeList.add(item);
        
        String[] flpCds = {"CA"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds)).thenReturn(flpOfficeList);
        
        // Act
        List<LocationArea> flpOfficeListReturned = mrtProxyBusinessService.flpByFlpCodeList(flpCds);
        
        // Assert
        assertNotNull("Office list should not be null", flpOfficeListReturned);
        assertEquals("Should return one location area", 1, flpOfficeListReturned.size());
        assertEquals("ID should match", 1, flpOfficeListReturned.get(0).getId().intValue());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByFlpCodeList(flpCds);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_ExceptionCovered() throws Exception {
        // Arrange
        String[] flpCds = {"CA"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds))
            .thenThrow(new RuntimeException("Service error"));
            
        // Act
        mrtProxyBusinessService.flpByFlpCodeList(flpCds);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_BusinessServiceBindingExceptionCovered() throws Exception {
        // Arrange
        String[] flpCds = {"CA"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
            
        // Act
        mrtProxyBusinessService.flpByFlpCodeList(flpCds);
    }

    @Test
    public void test_flpByFlpCodeList_light() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("40001");
        item.setCode("C");
        item.setName("TestName");
        item.setId(1);
        flpOfficeList.add(item);

        String[] flpCds = {"CA"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(
            eq(flpCds),
            eq(FlpLocationAreaProperties.stateCode),
            eq(FlpLocationAreaProperties.stateLocationAreaCode),
            eq(FlpLocationAreaProperties.stateName),
            eq(FlpLocationAreaProperties.code),
            eq(FlpLocationAreaProperties.name),
            eq(FlpLocationAreaProperties.shortName)))
            .thenReturn(flpOfficeList);

        // Act
        List<LocationArea> flpOfficeListReturned = mrtProxyBusinessService.flpByFlpCodeList_light(flpCds);
        
        // Assert
        assertNotNull("Office list should not be null", flpOfficeListReturned);
        assertEquals("Should return one location area", 1, flpOfficeListReturned.size());
        assertEquals("Location area should match", item, flpOfficeListReturned.get(0));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_light_BusinessServiceBindingException() throws Exception {
        // Arrange
        String[] flpCds = {"CA"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(
            eq(flpCds), any(), any(), any(), any(), any(), any()))
            .thenThrow(new BusinessServiceBindingException("Test Error"));
            
        // Act
        mrtProxyBusinessService.flpByFlpCodeList_light(flpCds);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_light_Exception() throws Exception {
        // Arrange
        String[] flpCds = {"CA"};
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(
            eq(flpCds), any(), any(), any(), any(), any(), any()))
            .thenThrow(new NullPointerException("Test Error"));
            
        // Act
        mrtProxyBusinessService.flpByFlpCodeList_light(flpCds);
    }

    // ===== OFFICE CODE LOOKUP TESTS =====

    @Test
    public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode() throws Exception {
        // Arrange
        List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();
        LocationArea flpLocationArea1 = new LocationArea();
        flpLocationArea1.setStateLocationAreaCode("01001");
        flpLocationArea.add(flpLocationArea1);

        Office office = new Office();
        office.setOfficeCode("TEST001");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("01020"))
            .thenReturn(flpLocationArea);
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(any()))
            .thenReturn(officeList);

        String fsaStateCountyCode = "01020";

        // Act
        List<Office> allStateOffices = mrtProxyBusinessService.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

        // Assert
        assertTrue("State offices should not be empty", !allStateOffices.isEmpty());
        assertEquals("Should return expected office", office, allStateOffices.get(0));
    }

    @Test
    public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode_emptyLocationAreaList() throws Exception {
        // Arrange
        List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();

        Office office = new Office();
        office.setOfficeCode("TEST001");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("01020"))
            .thenReturn(flpLocationArea);

        String fsaStateCountyCode = "01020";

        // Act
        List<Office> allStateOffices = mrtProxyBusinessService.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

        // Assert
        assertTrue("State offices should be empty when no location areas found", allStateOffices.isEmpty());
    }

    // ===== SERVICE CENTER BY OIP TESTS =====

    @Test
    public void test_retrieveFlpServiceCentersByOIP() throws Exception {
        // Arrange
        List<String> stateOffices = new ArrayList<String>();
        stateOffices.add("01300");

        List<Office> officeList = new ArrayList<Office>();
        Office office1 = new Office();
        office1.setId(123213);
        officeList.add(office1);

        List<Office> officeList2 = new ArrayList<Office>();
        Office office2 = new Office();
        office2.setOfficeCode("01301");
        officeList2.add(office2);

        String[] expectedArray = {"01300"};
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(expectedArray))
            .thenReturn(officeList);

        Integer[] expectedIdArray = {123213};
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByOfficeIdList(expectedIdArray))
            .thenReturn(officeList2);

        // Act
        List<Office> allStateOffices = mrtProxyBusinessService.retrieveFlpServiceCentersByOIP(stateOffices);

        // Assert
        assertTrue("State offices should not be empty", !allStateOffices.isEmpty());
    }

    // ===== OFFICE LOCATION AREA TESTS =====

    @Test
    public void test_retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {
        // Arrange
        List<Office> officeList = new ArrayList<Office>();
        Office office1 = new Office();
        office1.setOfficeCode("63300");
        office1.setStateName("HAWAII");
        office1.setCountyName("state");
        officeList.add(office1);
        
        Office office2 = new Office();
        office2.setOfficeCode("63301");
        office2.setStateName("HAWAII");
        office2.setCountyName("County");
        officeList.add(office2);
        
        Office office3 = new Office();
        office3.setOfficeCode("63303");
        office3.setStateName("HAWAII");
        office3.setCountyName("County2");
        officeList.add(office3);

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq("HI"),
            eq(FlpOfficeProperties.stateFipsCode),
            eq(FlpOfficeProperties.stateName),
            eq(FlpOfficeProperties.officeCode),
            eq(FlpOfficeProperties.name),
            eq(FlpOfficeProperties.countyName)))
            .thenReturn(officeList);

        String stateAbbr = "HI";

        // Act
        List<FlpOfficeLocationAreaBO> allFlpOfficeLocationAreas = mrtProxyBusinessService.retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Office location areas should be empty on exception", allFlpOfficeLocationAreas.isEmpty());
    }

    @Test
    public void test_retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {
        // Arrange
        List<LocationArea> fsaLocAreaList = new ArrayList<LocationArea>();

        LocationArea locationArea1 = new LocationArea();
        locationArea1.setStateLocationAreaCode("63");
        locationArea1.setCode("001");
        locationArea1.setStateName("HAWAII");
        locationArea1.setName("Name 1");
        fsaLocAreaList.add(locationArea1);

        LocationArea locationArea2 = new LocationArea();
        locationArea2.setStateLocationAreaCode("63");
        locationArea2.setCode("000"); // This should be filtered out
        locationArea2.setStateName("HAWAII");
        locationArea2.setName("Name 2");
        fsaLocAreaList.add(locationArea2);

        LocationArea locationArea3 = new LocationArea();
        locationArea3.setStateLocationAreaCode("63");
        locationArea3.setCode("003");
        locationArea3.setStateName("HAWAII");
        locationArea3.setName("Name 3");
        fsaLocAreaList.add(locationArea3);

        when(mockLocationAreaDataServiceProxy.byStateAbbr(
            eq("HI"),
            eq(FsaLocationAreaProperties.stateLocationAreaCode),
            eq(FsaLocationAreaProperties.stateName),
            eq(FsaLocationAreaProperties.name),
            eq(FsaLocationAreaProperties.code)))
            .thenReturn(fsaLocAreaList);

        String stateAbbr = "HI";

        // Act
        List<FipsOfficeLocationAreaBO> result = mrtProxyBusinessService.retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertFalse("Result should not be empty", result.isEmpty());
        assertEquals("Should return 2 areas (filtering out code 000)", 2, result.size());
        
        // Verify that code "000" was filtered out
        boolean hasCode000 = result.stream().anyMatch(area -> 
            area.getFipsStateLocationAreaCode().contains("000"));
        assertFalse("Should not contain area with code 000", hasCode000);
    }

    @Test
    public void test_retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr_exception() throws Exception {
        // Arrange
        when(mockLocationAreaDataServiceProxy.byStateAbbr(
            eq("HI"), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        String stateAbbr = "HI";

        // Act
        List<FipsOfficeLocationAreaBO> result = mrtProxyBusinessService.retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Result should be empty on exception", result.isEmpty());
    }

    // ===== OFFICE CODE LOOKUP BY FSA STATE COUNTY CODE =====

    @Test
    public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode() throws Exception {
        // Arrange
        List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();
        LocationArea flpLocationArea1 = new LocationArea();
        flpLocationArea1.setStateLocationAreaCode("01001");
        flpLocationArea.add(flpLocationArea1);

        Office office = new Office();
        office.setOfficeCode("TEST001");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("01020"))
            .thenReturn(flpLocationArea);
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(any()))
            .thenReturn(officeList);

        String fsaStateCountyCode = "01020";

        // Act
        List<Office> allStateOffices = mrtProxyBusinessService.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

        // Assert
        assertTrue("State offices should not be empty", !allStateOffices.isEmpty());
        assertEquals("Should return expected office", office, allStateOffices.get(0));
    }

    @Test
    public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode_emptyLocationAreaList() throws Exception {
        // Arrange
        List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("01020"))
            .thenReturn(flpLocationArea);

        String fsaStateCountyCode = "01020";

        // Act
        List<Office> allStateOffices = mrtProxyBusinessService.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

        // Assert
        assertTrue("State offices should be empty when no location areas found", allStateOffices.isEmpty());
    }

    // ===== ADDITIONAL HELPER METHODS FOR COMPLETE COVERAGE =====

    private List<State> createMockStateList() {
        List<State> stateList = new ArrayList<State>();
        
        State state1 = new State();
        state1.setCode("30");
        state1.setName("Missouri");
        state1.setAbbreviation("MO");
        
        State state2 = new State();
        state2.setCode("17");
        state2.setName("Kansas");
        state2.setAbbreviation("KS");
        
        stateList.add(state1);
        stateList.add(state2);
        
        return stateList;
    }

    private List<Office> createMockOfficeList() {
        List<Office> officeList = new ArrayList<Office>();
        
        Office office1 = new Office();
        office1.setId(1);
        office1.setOfficeCode("01305");
        office1.setName("Kansas City Office");
        office1.setCountyName("Jackson");
        
        Office office2 = new Office();
        office2.setId(2);
        office2.setOfficeCode("01306");
        office2.setName("Liberty Office");
        office2.setCountyName("Clay");
        
        officeList.add(office1);
        officeList.add(office2);
        
        return officeList;
    }

    private List<LocationArea> createMockLocationAreaList() {
        List<LocationArea> locationList = new ArrayList<LocationArea>();
        
        LocationArea area1 = new LocationArea();
        area1.setStateLocationAreaCode("30001");
        area1.setCode("001");
        area1.setName("Jackson County");
        area1.setId(1);
        
        LocationArea area2 = new LocationArea();
        area2.setStateLocationAreaCode("30002");
        area2.setCode("002");
        area2.setName("Clay County");
        area2.setId(2);
        
        locationList.add(area1);
        locationList.add(area2);
        
        return locationList;
    }

}reas = mrtProxyBusinessService.retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Office location areas should not be empty", !allFlpOfficeLocationAreas.isEmpty());
        assertEquals("Should return 3 office location areas", 3, allFlpOfficeLocationAreas.size());
    }

    @Test
    public void test_retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr_Exception() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq("HI"), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        String stateAbbr = "HI";

        // Act
        List<FlpOfficeLocationAreaBO> allFlpOfficeLocationApackage gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gov.usda.fsa.citso.cbs.bc.InterestTypeId;
import gov.usda.fsa.citso.cbs.bc.Surrogate;
import gov.usda.fsa.citso.cbs.bc.TaxId;
import gov.usda.fsa.citso.cbs.client.BusinessPartyDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.EmployeeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.InterestRateDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.StateDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.TaxIdSurrogateBusinessServiceProxy;
import gov.usda.fsa.citso.cbs.dto.AgencyEmployee;
import gov.usda.fsa.citso.cbs.dto.BusinessPartyInfo;
import gov.usda.fsa.citso.cbs.dto.BusinessPartyRole;
import gov.usda.fsa.citso.cbs.dto.EmployeeOrgChart;
import gov.usda.fsa.citso.cbs.dto.InterestRate;
import gov.usda.fsa.citso.cbs.dto.LocationArea;
import gov.usda.fsa.citso.cbs.dto.Office;
import gov.usda.fsa.citso.cbs.dto.State;
import gov.usda.fsa.citso.cbs.dto.metadata.AgencyEmployeeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpLocationAreaProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpOfficeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpStateProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaLocationAreaProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaOfficeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaStateProperties;
import gov.usda.fsa.citso.cbs.ex.BusinessServiceBindingException;
import gov.usda.fsa.citso.cbs.service.ErrorMessage;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveFSAStateListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveFsaCountyListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveInterestRateForAssistanceTypeBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTCountyListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTServiceCenterListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTStateListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FipsOfficeLocationAreaBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FlpOfficeLocationAreaBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.MrtLookUpBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;

/**
 * MRTProxyBS_UT_Mockito
 * 
 * Unit tests for MRTProxyBS using Mockito framework instead of PowerMock.
 * This version eliminates PowerMock dependencies for faster, more isolated testing.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MRTProxyBS_UT extends DLSExternalCommonTestMockBase {
    
    private IMRTProxyBS mrtProxyBusinessService;

    @Mock
    private OfficeDataServiceProxy mockOfficeDataServiceProxy;
    
    @Mock
    private StateDataServiceProxy mockStateDataServiceProxy;
    
    @Mock
    private InterestRateDataServiceProxy mockInterestRateDataServiceProxy;
    
    @Mock
    private LocationAreaDataServiceProxy mockLocationAreaDataServiceProxy;
    
    @Mock
    private BusinessPartyDataServiceProxy mockBusinessPartyDataServiceProxy;
    
    @Mock
    private EmployeeDataServiceProxy mockEmployeeDataServiceProxy;
    
    @Mock
    private MRTFacadeBusinessService mockMRTFacadeBusinessService;
    
    @Mock
    private TaxIdSurrogateBusinessServiceProxy mockSurrogateService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        // Get the Spring-managed service instance
        mrtProxyBusinessService = ServiceAgentFacade.getInstance().getMrtProxyBusinessService();
        
        // Cast to implementation to inject mocks
        assertTrue("Service should be instance of MRTProxyBS", 
                  mrtProxyBusinessService instanceof MRTProxyBS);
        MRTProxyBS mrtProxyBS = (MRTProxyBS) mrtProxyBusinessService;
        
        // Inject mocked dependencies
        mrtProxyBS.setSurrogateService(mockSurrogateService);
        mrtProxyBS.setBusinessPartyDataService(mockBusinessPartyDataServiceProxy);
        mrtProxyBS.setEmployeeDataServiceProxy(mockEmployeeDataServiceProxy);
        mrtProxyBS.setFlpLocationAreaDataMartBusinessService(mockLocationAreaDataServiceProxy);
        mrtProxyBS.setFlpOfficeMRTBusinessService(mockOfficeDataServiceProxy);
        mrtProxyBS.setFlpStateMRTBusinessService(mockStateDataServiceProxy);
        mrtProxyBS.setInterestRateDataMartBusinessService(mockInterestRateDataServiceProxy);
        mrtProxyBS.setMrtFacadeBusinessService(mockMRTFacadeBusinessService);
    }

    // ===== SURROGATE ID TESTS =====

    @Test
    public void testRetrieveSurrogateIdForTaxId() throws Exception {
        // Arrange
        List<String> taxIdList = new ArrayList<String>();
        String taxId1 = "123456789";
        taxIdList.add(taxId1);
        
        Map<String, Surrogate> expectedMap = new HashMap<String, Surrogate>();
        Surrogate surrogate = new Surrogate("SURR123");
        expectedMap.put(taxId1, surrogate);
        
        when(mockSurrogateService.transformTaxId(taxIdList)).thenReturn(expectedMap);
        
        // Act
        Map<String, Surrogate> taxIdSurrogateMap = mrtProxyBusinessService.retrieveSurrogateIdForTaxId(taxIdList);
        
        // Assert
        assertNotNull("Result should not be null", taxIdSurrogateMap);
        assertEquals("Should return expected map", expectedMap, taxIdSurrogateMap);
        
        verify(mockSurrogateService, times(1)).transformTaxId(taxIdList);
    }

    @Test
    public void testRetrieveTaxIdForSurrogateId() throws Exception {
        // Arrange
        List<String> surrIdList = new ArrayList<String>();
        String surrId = "123456789";
        surrIdList.add(surrId);
        
        Map<String, TaxId> expectedMap = new HashMap<String, TaxId>();
        TaxId taxId = new TaxId("TAX123");
        expectedMap.put(surrId, taxId);
        
        when(mockSurrogateService.transformSurrogate(surrIdList)).thenReturn(expectedMap);
        
        // Act
        Map<String, TaxId> surrogateIdTaxIdMap = mrtProxyBusinessService.retrieveTaxIdForSurrogateId(surrIdList);
        
        // Assert
        assertNotNull("Result should not be null", surrogateIdTaxIdMap);
        assertEquals("Should return expected map", expectedMap, surrogateIdTaxIdMap);
        
        verify(mockSurrogateService, times(1)).transformSurrogate(surrIdList);
    }

    // ===== EMPLOYEE LOOKUP TESTS =====

    @Test
    public void test_lookupUserForEmployeeId_exception() throws Exception {
        // Arrange
        when(mockBusinessPartyDataServiceProxy.infoByAuthId(eq("dummyEAuthID"), any()))
            .thenReturn(null);
        
        // Act
        String employeeId = mrtProxyBusinessService.lookupUserEmployeeCamsId("dummyEAuthID");

        // Assert
        assertTrue("Employee ID should be empty", StringUtil.isEmptyString(employeeId));
        
        verify(mockBusinessPartyDataServiceProxy, times(1)).infoByAuthId(eq("dummyEAuthID"), any());
    }

    @Test
    public void test_lookupUserForEmployeeId() throws Exception {
        // Arrange
        BusinessPartyInfo mockBusinessPartyInfo = mock(BusinessPartyInfo.class);
        AgencyEmployee[] agencyEmployees = new AgencyEmployee[1];
        AgencyEmployee agencyEmployee = new AgencyEmployee();
        agencyEmployees[0] = agencyEmployee;
        agencyEmployee.setCamsEmployeeId("testing ID");

        when(mockBusinessPartyDataServiceProxy.infoByAuthId("dummyEAuthID", AgencyEmployeeProperties.camsEmployeeId))
            .thenReturn(mockBusinessPartyInfo);
        when(mockBusinessPartyInfo.getAgencyEmployee()).thenReturn(agencyEmployees);
        
        // Act
        String employeeId = mrtProxyBusinessService.lookupUserEmployeeCamsId("dummyEAuthID");

        // Assert
        assertNotNull("Employee ID should not be null", employeeId);
        assertEquals("Employee ID should match", "testing ID", employeeId);
        
        verify(mockBusinessPartyDataServiceProxy, times(1)).infoByAuthId("dummyEAuthID", AgencyEmployeeProperties.camsEmployeeId);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_lookupUser_exception() throws Exception {
        // Arrange
        when(mockBusinessPartyDataServiceProxy.roleByAuthId(anyString()))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        mrtProxyBusinessService.lookupUser("testUser");
    }

    @Test
    public void test_lookupUser_valid() throws Exception {
        // Arrange
        BusinessPartyRole businessPartyRoleMock = new BusinessPartyRole();
        businessPartyRoleMock.setName("SO");
        
        when(mockBusinessPartyDataServiceProxy.roleByAuthId("28200310169021026877"))
            .thenReturn(businessPartyRoleMock);

        // Act
        BusinessPartyRole businessPartyRole = mrtProxyBusinessService.lookupUser("28200310169021026877");

        // Assert
        assertNotNull("Business party role should not be null", businessPartyRole);
        assertEquals("Role name should match", "SO", businessPartyRole.getName());
        
        verify(mockBusinessPartyDataServiceProxy, times(1)).roleByAuthId("28200310169021026877");
    }

    // ===== INTEREST RATE TESTS =====

    @Test
    public void test_retrieveInterestRate() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        
        InterestRate rate = new InterestRate();
        rate.setIntRate(BigDecimal.TEN);
        
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, date.getTime()))
            .thenReturn(rate);

        // Act
        InterestRate interestRate = mrtProxyBusinessService.retrieveInterestRate(50010, date.getTime());

        // Assert
        assertNotNull("Interest rate should not be null", interestRate);
        assertTrue("Interest rate should be greater than 0", interestRate.getIntRate().doubleValue() > 0.0);
        assertEquals("Interest rate should match", BigDecimal.TEN, interestRate.getIntRate());
        
        verify(mockInterestRateDataServiceProxy, times(1)).byTypeIdAndDate(50010, date.getTime());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveInterestRates_exception() throws Exception {
        // Arrange
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, null))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveInterestRate(50010, null);
    }

    @Test
    public void test_retrieveInterestRates() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Integer[] typeIds = {50500};

        InterestRate rate = new InterestRate();
        rate.setIntRate(BigDecimal.TEN);
        List<InterestRate> interestRatesMock = new ArrayList<InterestRate>();
        interestRatesMock.add(rate);
        
        when(mockInterestRateDataServiceProxy.byTypeIdListAndDate(typeIds, date.getTime()))
            .thenReturn(interestRatesMock);

        // Act
        List<InterestRate> interestRates = mrtProxyBusinessService.retrieveInterestRates(typeIds, date.getTime());

        // Assert
        assertNotNull("Interest rates should not be null", interestRates);
        assertFalse("Interest rates should not be empty", interestRates.isEmpty());
        assertEquals("Should return one rate", 1, interestRates.size());
        assertTrue("Interest rate should be greater than 0", interestRates.get(0).getIntRate().doubleValue() > 0.0);
        
        verify(mockInterestRateDataServiceProxy, times(1)).byTypeIdListAndDate(typeIds, date.getTime());
    }

    @Test
    public void testRretrieveInterestRateForAssistanceType() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Date cutOffDate = date.getTime();
        RetrieveInterestRateForAssistanceTypeBC contract = new RetrieveInterestRateForAssistanceTypeBC(null, "50010", cutOffDate);

        InterestRate rate = new InterestRate();
        rate.setIntRate(BigDecimal.ONE);

        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate)).thenReturn(rate);

        // Act
        double interest = mrtProxyBusinessService.retrieveInterestRateForAssistanceType(contract);

        // Assert
        assertTrue("Interest should be greater than 0", Double.valueOf(interest).compareTo(Double.valueOf(0.0)) > 0);
        
        verify(mockInterestRateDataServiceProxy, times(1)).byTypeIdAndDate(50010, cutOffDate);
    }

    @Test
    public void testRretrieveInterestRateForAssistanceType_BusinessServiceBindingException() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Date cutOffDate = date.getTime();
        RetrieveInterestRateForAssistanceTypeBC contract = new RetrieveInterestRateForAssistanceTypeBC(null, "50010", cutOffDate);
        
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));

        // Act
        Double interestRate = mrtProxyBusinessService.retrieveInterestRateForAssistanceType(contract);
        
        // Assert
        assertEquals("Should return 0.0 on exception", 0.00, interestRate.doubleValue(), 0.001);
    }

    @Test
    public void testRretrieveInterestRateForAssistanceType_Exception() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Date cutOffDate = date.getTime();
        RetrieveInterestRateForAssistanceTypeBC contract = new RetrieveInterestRateForAssistanceTypeBC(null, "50010", cutOffDate);
        
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate))
            .thenThrow(new NullPointerException("TestException"));

        // Act
        Double interestRate = mrtProxyBusinessService.retrieveInterestRateForAssistanceType(contract);
        
        // Assert
        assertEquals("Should return 0.0 on exception", 0.00, interestRate.doubleValue(), 0.001);
    }

    // ===== STATE OFFICE TESTS =====

    @Test
    public void test_retrieveFlpStateOffices() throws Exception {
        // Arrange
        Office office = new Office();
        office.setId(1);
        office.setOfficeCode("TEST001");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        when(mockOfficeDataServiceProxy.allFlpStateOffices()).thenReturn(officeList);

        // Act
        List<Office> allStateOffices = mrtProxyBusinessService.retrieveFlpStateOffices();

        // Assert
        assertNotNull("State offices should not be null", allStateOffices);
        assertFalse("State offices should not be empty", allStateOffices.isEmpty());
        assertEquals("Should return expected office", office, allStateOffices.get(0));
        
        verify(mockOfficeDataServiceProxy, times(1)).allFlpStateOffices();
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpStateOffices_DLSBusinessFatalException() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.allFlpStateOffices())
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        mrtProxyBusinessService.retrieveFlpStateOffices();
    }

    @Test
    public void test_retrieveFlpStateOffices_light() throws Exception {
        // Arrange
        List<Office> allStateOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setId(1);
        office.setAgencyAbbr("FSA");
        office.setCounty(true);
        allStateOffices.add(office);
        
        when(mockOfficeDataServiceProxy.allFlpStateOffices(
            eq(FlpOfficeProperties.stateName),
            eq(FlpOfficeProperties.stateAbbrev),
            eq(FlpOfficeProperties.stateFipsCode),
            eq(FlpOfficeProperties.officeCode)))
            .thenReturn(allStateOffices);

        // Act
        List<Office> officeListReturned = mrtProxyBusinessService.retrieveFlpStateOffices_light();
        
        // Assert
        assertNotNull("Office list should not be null", officeList);
        assertEquals("Should return one office", 1, officeList.size());
        assertEquals("Office code should match", "21047", officeList.get(0).getOfficeCode());
        
        verify(mockOfficeDataServiceProxy, times(1)).flpOfficesByFlpCodeList(expectedArray);
    }

    // ===== STATE LIST TESTS =====

    @Test
    public void testRetrieveFLPStateList() throws Exception {
        // Arrange
        State state = new State();
        state.setCode("30");
        state.setName("Missouri");
        state.setAbbreviation("MO");
        List<State> mockedStateList = new ArrayList<State>();
        mockedStateList.add(state);
        
        when(mockStateDataServiceProxy.allFlp(
            eq(FlpStateProperties.code),
            eq(FlpStateProperties.name),
            eq(FlpStateProperties.abbreviation),
            eq(FlpStateProperties.fipsCode),
            eq(FlpStateProperties.activeId)))
            .thenReturn(mockedStateList);

        // Act
        List<State> flpStateList = mrtProxyBusinessService.retrieveFLPStateList();

        // Assert
        assertNotNull("State list should not be null", flpStateList);
        assertTrue("State list should not be empty", flpStateList.size() > 0);
        assertEquals("Should return expected state", state, flpStateList.get(0));
        
        verify(mockStateDataServiceProxy, times(1)).allFlp(
            eq(FlpStateProperties.code),
            eq(FlpStateProperties.name),
            eq(FlpStateProperties.abbreviation),
            eq(FlpStateProperties.fipsCode),
            eq(FlpStateProperties.activeId));
    }

    @Test(expected = DLSBusinessStopException.class)
    public void testRetrieveFLPOfficeMRTBusinessObjectReadFacadeList_DLSBusinessStopException() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq("MO"),
            eq(FlpOfficeProperties.officeCode),
            eq(FlpOfficeProperties.name),
            eq(FlpOfficeProperties.refId),
            eq(FlpOfficeProperties.cityFipsCode),
            eq(FlpOfficeProperties.locCityName),
            eq(FlpOfficeProperties.locStateAbbrev)))
            .thenThrow(new RuntimeException("Service error"));
        
        // Act
        mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList("MO");
    }

    @Test
    public void testRetrieveFLPOfficeMRTBusinessObjectReadFacadeList() throws Exception {
        // Arrange
        String stateAbbr = "MO";
        
        Office office = new Office();
        office.setOfficeCode("TEST001");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq(stateAbbr),
            eq(FlpOfficeProperties.officeCode),
            eq(FlpOfficeProperties.name),
            eq(FlpOfficeProperties.refId),
            eq(FlpOfficeProperties.cityFipsCode),
            eq(FlpOfficeProperties.locCityName),
            eq(FlpOfficeProperties.locStateAbbrev)))
            .thenReturn(officeList);

        // Act
        List<Office> flpOfficeList = mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList(stateAbbr);

        // Assert
        assertNotNull("Office list should not be null", flpOfficeList);
        assertTrue("Office list should not be empty", flpOfficeList.size() > 0);
        assertEquals("Should return expected office", office, flpOfficeList.get(0));
    }

    // ===== LOOKUP BO TESTS =====

    @Test
    public void testRetrieveCountyList() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(this.createAgencyToken(), "123");

        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        LocationArea object = new LocationArea();
        object.setStateLocationAreaCode("CD");
        object.setStateRefId(1);
        object.setAlternateName("Alt_Name");
        mockedLocationAreaObject.add(object);
        
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(123))
            .thenReturn(mockedLocationAreaObject);

        // Act
        List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveCountyList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertEquals("Should return one county", 1, resultList.size());
        
        MrtLookUpBO result = resultList.get(0);
        assertEquals("County code should match", "CD", result.getCode());
        assertEquals("County name should match", "Alt_Name", result.getDescription());
        assertEquals("State ref ID should match", "1", result.getRefenceIdentifier());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByOfficeRefId(123);
    }

    @Test
    public void testRetrieveCountyListWithResult() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "58303");
        List<MrtLookUpBO> boList = new ArrayList<MrtLookUpBO>();
        
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(58303))
            .thenReturn(new ArrayList<LocationArea>());

        // Act
        List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveCountyList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertEquals("Should return empty list", 0, resultList.size());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveCountyList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "123");
        
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(123))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveCountyList(contract);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveCountyList_BusinessServiceBindingException() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "123");
        
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(123))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));

        // Act
        mrtProxyBusinessService.retrieveCountyList(contract);
    }

    @Test
    public void testRetrieveStateList() throws Exception {
        // Arrange
        RetrieveMRTStateListBC contract = new RetrieveMRTStateListBC(null);

        List<State> mockedStateList = new ArrayList<State>();
        State state = new State();
        state.setCode("69");
        state.setName("Missouri");
        state.setAbbreviation("MO");
        mockedStateList.add(state);
        
        when(mockStateDataServiceProxy.allFlp(
            eq(FlpStateProperties.code),
            eq(FlpStateProperties.name),
            eq(FlpStateProperties.abbreviation),
            eq(FlpStateProperties.fipsCode),
            eq(FlpStateProperties.activeId)))
            .thenReturn(mockedStateList);

        // Act
        List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveStateList(contract);
        
        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertTrue("Result list should not be empty", resultList.size() > 0);
        
        MrtLookUpBO mrtLookUpBO = resultList.get(0);
        assertNotNull("Code should not be null", mrtLookUpBO.getCode());
        assertNotNull("Description should not be null", mrtLookUpBO.getDescription());
        assertNotNull("Reference identifier should not be null", mrtLookUpBO.getRefenceIdentifier());
        assertNull("Type should be null", mrtLookUpBO.getType());
        
        assertEquals("Code should match", "69", mrtLookUpBO.getCode());
        assertEquals("Description should match", "Missouri", mrtLookUpBO.getDescription());
        assertEquals("Reference identifier should match", "MO", mrtLookUpBO.getRefenceIdentifier());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveStateList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveMRTStateListBC contract = new RetrieveMRTStateListBC(null);
        
        when(mockStateDataServiceProxy.allFlp(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveStateList(contract);
    }

    @Test
    public void testRetrieveServiceCenterList() throws Exception {
        // Arrange
        RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(null, "MO");

        List<Office> boList = new ArrayList<Office>();
        Office bo = new Office();
        bo.setOfficeCode("Code");
        bo.setName("Name");
        bo.setOfficeResp("offRefId");
        bo.setRefId(12345);
        boList.add(bo);

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq("MO"),
            eq(FlpOfficeProperties.officeCode),
            eq(FlpOfficeProperties.name),
            eq(FlpOfficeProperties.refId),
            eq(FlpOfficeProperties.cityFipsCode),
            eq(FlpOfficeProperties.locCityName),
            eq(FlpOfficeProperties.locStateAbbrev)))
            .thenReturn(boList);

        // Act
        List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveServiceCenterList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertTrue("Result list should not be empty", resultList.size() > 0);
        
        MrtLookUpBO result = resultList.get(0);
        assertEquals("Code should match", "Code", result.getCode());
        assertEquals("Description should match", "Name", result.getDescription());
        assertEquals("Reference identifier should match", "12345", result.getRefenceIdentifier());
    }

    @Test
    public void testRetrieveServiceCenterListWrongState() throws Exception {
        // Arrange
        RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(null, "MO");
        contract.setStateCode("AA");

        List<Office> boList = new ArrayList<Office>();
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq("AA"),
            any(), any(), any(), any(), any(), any()))
            .thenReturn(boList);

        // Act
        List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveServiceCenterList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertEquals("Result list should be empty", 0, resultList.size());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveServiceCenterList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(null, "MO");
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
            eq("MO"), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        mrtProxyBusinessService.retrieveServiceCenterList(contract);
    }

    // ===== FSA STATE COUNTY CODE TESTS =====

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode() throws Exception {
        // Arrange
        String office_flp_code_str = "123";

        Office office = new Office();
        office.setOfficeCode("123");
        office.setId(123);

        List<String> flpCodeList = new ArrayList<String>();
        flpCodeList.add(office_flp_code_str);

        String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);
        
        // Mock the FSA office lookup to return empty list (no corresponding FSA office)
        when(mockOfficeDataServiceProxy.byOfficeIdList(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new ArrayList<Office>());

        // Act
        String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

        // Assert
        assertNotNull("FSA code should not be null", fsaCode);
        assertEquals("Should return empty string when no FSA office found", "", fsaCode);
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_withSiteId() throws Exception {
        // Arrange
        String flpCode = "21047";
        String expectedResult = "21047::8247";

        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);

        List<String> flpCodeList = new ArrayList<String>();
        flpCodeList.add(flpCode);

        String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

        List<Integer> flpIdCodes = new ArrayList<Integer>();
        flpIdCodes.add(21047);
        Integer[] expectedIdArray = {21047};
        
        when(mockOfficeDataServiceProxy.byOfficeIdList(
            eq(expectedIdArray),
            eq(FsaOfficeProperties.id),
            eq(FsaOfficeProperties.locStateAbbrev),
            eq(FsaOfficeProperties.locCityName),
            eq(FsaOfficeProperties.stateAbbrev),
            eq(FsaOfficeProperties.officeCode),
            eq(FsaOfficeProperties.name),
            eq(FsaOfficeProperties.cityFipsCode),
            eq(FsaOfficeProperties.refId),
            eq(FsaOfficeProperties.siteId),
            eq(FsaOfficeProperties.mailingZipCode),
            eq(FsaOfficeProperties.mailingAddrInfoLine),
            eq(FsaOfficeProperties.mailingAddrLine)))
            .thenReturn(officeList);

        // Act
        String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(flpCode);

        // Assert
        assertNotNull("FSA code should not be null", fsaCode);
        assertEquals("FSA code should match expected", expectedResult, fsaCode);
        assertTrue("FSA code should be longer than 7 characters", expectedResult.length() > 7);
        assertNotNull("Site ID part should not be null", expectedResult.substring(7));
        assertEquals("Site ID part should be 4 characters", 4, expectedResult.substring(7).length());
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_DLSBusinessFatalException() throws Exception {
        // Arrange
        String office_flp_code_str = "123";

        List<String> flpCodeList = new ArrayList<String>();
        flpCodeList.add(office_flp_code_str);

        String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        String code = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

        // Assert
        assertEquals("Should return CBS_ERROR on exception", "CBS_ERROR", code);
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_NoError() throws Exception {
        // Arrange
        List<Office> serviceCenterOfficesList = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("123");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOfficesList.add(office);

        Office office2 = new Office();
        office2.setOfficeCode("312");
        office2.setId(61012);
        office2.setSiteId(6112);
        serviceCenterOfficesList.add(office2);
        
        String office_flp_code_str = "123";

        List<String> flpCodeList = new ArrayList<String>();
        flpCodeList.add(office_flp_code_str);
        List<Integer> flpCodeIntegerList = new ArrayList<Integer>();
        flpCodeIntegerList.add(21047);
        
        String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
        Integer[] flpIntegerCodesArray = flpCodeIntegerList.toArray(new Integer[0]);
        
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
            .thenReturn(serviceCenterOfficesList);
        when(mockOfficeDataServiceProxy.byOfficeIdList(
            eq(flpIntegerCodesArray),
            eq(FsaOfficeProperties.id),
            eq(FsaOfficeProperties.locStateAbbrev),
            eq(FsaOfficeProperties.locCityName),
            eq(FsaOfficeProperties.stateAbbrev),
            eq(FsaOfficeProperties.officeCode),
            eq(FsaOfficeProperties.name),
            eq(FsaOfficeProperties.cityFipsCode),
            eq(FsaOfficeProperties.refId),
            eq(FsaOfficeProperties.siteId),
            eq(FsaOfficeProperties.mailingZipCode),
            eq(FsaOfficeProperties.mailingAddrInfoLine),
            eq(FsaOfficeProperties.mailingAddrLine)))
            .thenReturn(serviceCenterOfficesList);

        // Act
        String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

        // Assert
        assertTrue("FSA code should match expected format", "123::8247".equalsIgnoreCase(fsaCode));
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_BusinessServiceBindingException() throws Exception {
        // Arrange
        List<Office> serviceCenterOfficesList = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("123");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOfficesList.add(office);
        
        String office_flp_code_str = "123";

        List<String> flpCodeList = new ArrayList<String>();
        flpCodeList.add(office_flp_code_str);
        List<Integer> flpCodeIntegerList = new ArrayList<Integer>();
        flpCodeIntegerList.add(21047);
        
        String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
        Integer[] flpIntegerCodesArray = flpCodeIntegerList.toArray(new Integer[0]);
        
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
            .thenReturn(serviceCenterOfficesList);
        when(mockOfficeDataServiceProxy.byOfficeIdList(eq(flpIntegerCodesArray), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new BusinessServiceBindingException("TestException"));

        // Act
        String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);
        
        // Assert
        assertTrue("FSA code should be empty on exception", StringUtil.isEmptyString(fsaCode));
    }

    // ===== SCIMS CUSTOMER TESTS =====

    @Test
    public void testRetrieveFLPOfficeCodeListForScimsCustomerEmptyInput() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();

        // Act
        List<LocationArea> objects = mrtProxyBusinessService.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

        // Assert
        assertEquals("Should return empty list for empty input", 0, objects.size());
    }

    @Test
    public void testRetrieveFLPOfficeCodeListForScimsCustomerCorrectCode() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("04001");

        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        LocationArea object = new LocationArea();
        LocationArea object1 = new LocationArea();
        LocationArea object2 = new LocationArea();
        LocationArea object3 = new LocationArea();
        mockedLocationAreaObject.add(object);
        mockedLocationAreaObject.add(object1);
        mockedLocationAreaObject.add(object2);
        mockedLocationAreaObject.add(object3);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("04001"))
            .thenReturn(mockedLocationAreaObject);

        // Act
        List<LocationArea> objects = mrtProxyBusinessService.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

        // Assert
        assertEquals("Should return 4 location areas", 4, objects.size());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByFsaStateLocArea("04001");
    }

    @Test
    public void testRetrieveFLPOfficeCodeListForScimsCustomerWrongCode() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("00000");

        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("00000"))
            .thenReturn(mockedLocationAreaObject);

        // Act
        List<LocationArea> objects = mrtProxyBusinessService.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

        // Assert
        assertEquals("Should return empty list for wrong code", 0, objects.size());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByFsaStateLocArea("00000");
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveFLPOfficeCodeListForScimsCustomer_DLSBusinessFatalException() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("00000");
        
        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("00000"))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));

        // Act
        mrtProxyBusinessService.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);
    }

    @Test
    public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerEmptyInput() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        AgencyToken token = null;

        // Act
        List<Office> offices = mrtProxyBusinessService.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);
        
        // Assert
        assertTrue("Should return empty list for empty input", offices.isEmpty());
    }

    @Test
    public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerExpectedException() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        AgencyToken token = null;
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode(null); // This should cause the method to skip processing
        flpOfficeList.add(item);

        // Act
        List<Office> offices = mrtProxyBusinessService.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);
        
        // Assert
        assertTrue("Should return empty list for null location area code", offices.isEmpty());
    }

    @Test
    public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerNoneEmptyInput() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("40001");
        flpOfficeList.add(item);
        LocationArea item2 = new LocationArea();
        item2.setStateLocationAreaCode("40002");
        flpOfficeList.add(item2);

        AgencyToken token = null;

        List<Office> flpOfficeLocXRefMRTBusinessObjects = new ArrayList<Office>();

        String[] completeFlpLocArLstArray1 = {"40001"};
        String[] completeFlpLocArLstArray2 = {"40002"};
        
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray1))
            .thenReturn(flpOfficeLocXRefMRTBusinessObjects);
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray2))
            .thenReturn(flpOfficeLocXRefMRTBusinessObjects);

        // Act
        List<Office> offices = mrtProxyBusinessService.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);
        
        // Assert
        assertTrue("Should return empty list when no offices found", offices.isEmpty());
    }

    @Test
    public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerNoneEmptyInputWithData() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("58062");
        flpOfficeList.add(item);

        List<Office> flpOfficeLocXRefMRTBusinessObjects = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        flpOfficeLocXRefMRTBusinessObjects.add(office);

        String[] completeFlpLocArLstArray = {"58062"};
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray))
            .thenReturn(flpOfficeLocXRefMRTBusinessObjects);

        // Act
        AgencyToken token = null;
        List<Office> results = mrtProxyBusinessService.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);

        // Assert
        assertEquals("Should return one office", 1, results.size());
        assertEquals("Office should match expected", office, results.get(0));
        
        verify(mockOfficeDataServiceProxy, times(1)).flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray);
    }

    // ===== STATE BY FIPS CODE TESTS =====

    @Test
    public void test_retrieveStateByFlpFipsCode() throws Exception {
        // Arrange
        String flpCode = "20324";
        State mockedState = new State();
        mockedState.setCode("20");
        mockedState.setName("Kansas");
        mockedState.setAbbreviation("KS");
        
        when(mockStateDataServiceProxy.byFlpFipsCode(
            eq(flpCode),
            eq(FlpStateProperties.code),
            eq(FlpStateProperties.name),
            eq(FlpStateProperties.abbreviation)))
            .thenReturn(mockedState);

        // Act
        State state = mrtProxyBusinessService.retrieveStateByFlpFipsCode(flpCode);

        // Assert
        assertNotNull("State should not be null", state);
        assertEquals("State should match expected", mockedState, state);
        
        verify(mockStateDataServiceProxy, times(1)).byFlpFipsCode(
            eq(flpCode),
            eq(FlpStateProperties.code),
            eq(FlpStateProperties.name),
            eq(FlpStateProperties.abbreviation));
    }

    @Test
    public void test_retrieveStateByFlpFipsCode_noStatefound() throws Exception {
        // Arrange
        String flpCode = "";
        State mockedState = null;
        
        when(mockStateDataServiceProxy.byFlpFipsCode(
            eq(flpCode),
            eq(FlpStateProperties.code),
            eq(FlpStateProperties.name),
            eq(FlpStateProperties.abbreviation)))
            .then officeListReturned);
        assertEquals("Should return one office", 1, officeListReturned.size());
        assertEquals("Office ID should match", 1, officeListReturned.get(0).getId().intValue());
        assertEquals("Agency abbreviation should match", "FSA", officeListReturned.get(0).getAgencyAbbr());
        
        verify(mockOfficeDataServiceProxy, times(1)).allFlpStateOffices(
            eq(FlpOfficeProperties.stateName),
            eq(FlpOfficeProperties.stateAbbrev),
            eq(FlpOfficeProperties.stateFipsCode),
            eq(FlpOfficeProperties.officeCode));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpStateOffices_light_ExceptionCovered() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.allFlpStateOffices(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));
        
        // Act
        mrtProxyBusinessService.retrieveFlpStateOffices_light();
    }

    // ===== SERVICE CENTER TESTS =====

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpServiceCentersByStateOffices_exception() throws Exception {
        // Arrange
        String[] states = {"StateNotExist"};
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(states))
            .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        mrtProxyBusinessService.retrieveFlpServiceCentersByStateOffices(states);
    }

    @Test
    public void test_retrieveFlpServiceCentersByStateOffices() throws Exception {
        // Arrange
        String[] states = {"MO"};
        
        Office office = new Office();
        office.setId(1);
        office.setOfficeCode("TEST001");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(states))
            .thenReturn(officeList);

        // Act
        List<Office> serviceCenterOffices = mrtProxyBusinessService.retrieveFlpServiceCentersByStateOffices(states);

        // Assert
        assertNotNull("Service center offices should not be null", serviceCenterOffices);
        assertFalse("Service center offices should not be empty", serviceCenterOffices.isEmpty());
        assertEquals("Should return expected office", office, serviceCenterOffices.get(0));
        
        verify(mockOfficeDataServiceProxy, times(1)).fsaFlpServiceCentreOfficesByOfficeFlpCodeList(states);
    }

    @Test
    public void test_retrieveFlpServiceCentersByStateOffices_light() throws Exception {
        // Arrange
        List<Office> serviceCenterOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOffices.add(office);
        
        String[] stateOffices = {"OF"};
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(
            eq(stateOffices),
            eq(FlpOfficeProperties.officeCode),
            eq(FlpOfficeProperties.countyFipsCode),
            eq(FlpOfficeProperties.countyName),
            eq(FlpOfficeProperties.stateFipsCode)))
            .thenReturn(serviceCenterOffices);

        // Act
        List<Office> officeListReturned = mrtProxyBusinessService.retrieveFlpServiceCentersByStateOffices_light(stateOffices);
        
        // Assert
        assertNotNull("Office list should not be null", officeListReturned);
        assertEquals("Should return expected offices", serviceCenterOffices, officeListReturned);
        
        verify(mockOfficeDataServiceProxy, times(1)).fsaFlpServiceCentreOfficesByOfficeFlpCodeList(
            eq(stateOffices),
            eq(FlpOfficeProperties.officeCode),
            eq(FlpOfficeProperties.countyFipsCode),
            eq(FlpOfficeProperties.countyName),
            eq(FlpOfficeProperties.stateFipsCode));
    }

    @Test
    public void test_retrieveFlpServiceCentersByStateAbbr() throws Exception {
        // Arrange
        List<Office> serviceCenterOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOffices.add(office);
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr("AL"))
            .thenReturn(serviceCenterOffices);
            
        // Act
        List<Office> officeListReturned = mrtProxyBusinessService.retrieveFlpServiceCentersByStateAbbr("AL");
        
        // Assert
        assertNotNull("Office list should not be null", officeListReturned);
        assertEquals("Should return one office", 1, officeListReturned.size());
        assertEquals("Office ID should match", 21047, officeListReturned.get(0).getId().intValue());
        
        verify(mockOfficeDataServiceProxy, times(1)).fsaFlpServiceCenterOfficesByStateAbbr("AL");
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpServiceCentersByStateAbbr_ExceptionCovered() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr("AL"))
            .thenThrow(new RuntimeException("Service error"));
        
        // Act
        mrtProxyBusinessService.retrieveFlpServiceCentersByStateAbbr("AL");
    }

    // ===== OFFICE LOOKUP TESTS =====

    @Test
    public void testRetrieveFSAOfficeListByOfficeIdentifierList() throws Exception {
        // Arrange
        List<Integer> flpIdCodes = new ArrayList<Integer>();
        flpIdCodes.add(60157);

        Office office = new Office();
        office.setId(60157);
        office.setOfficeCode("TEST001");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        Integer[] expectedArray = {60157};
        when(mockOfficeDataServiceProxy.byOfficeIdList(
            eq(expectedArray),
            eq(FsaOfficeProperties.id),
            eq(FsaOfficeProperties.locStateAbbrev),
            eq(FsaOfficeProperties.locCityName),
            eq(FsaOfficeProperties.stateAbbrev),
            eq(FsaOfficeProperties.officeCode),
            eq(FsaOfficeProperties.name),
            eq(FsaOfficeProperties.cityFipsCode),
            eq(FsaOfficeProperties.refId),
            eq(FsaOfficeProperties.siteId),
            eq(FsaOfficeProperties.mailingZipCode),
            eq(FsaOfficeProperties.mailingAddrInfoLine),
            eq(FsaOfficeProperties.mailingAddrLine)))
            .thenReturn(officeList);

        // Act
        List<Office> ofcList = mrtProxyBusinessService.retrieveFSAOfficeListByOfficeIdentifierList(flpIdCodes);

        // Assert
        assertNotNull("Office list should not be null", ofcList);
        assertTrue("Office list should not be empty", ofcList.size() > 0);
        assertEquals("Should return expected office", office, ofcList.get(0));
    }

    @Test
    public void testRetriveFLPOfficesByOfficeFLPCdMap() throws Exception {
        // Arrange
        List<String> flpCodeList = new ArrayList<String>();
        flpCodeList.add("01305");

        Office office = new Office();
        office.setOfficeCode("01305");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);

        String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

        // Act
        Map<String, Office> result = mrtProxyBusinessService.retriveFLPOfficesByOfficeFLPCdMap(flpCodeList);
        
        // Assert
        assertNotNull("Result map should not be null", result);
        assertFalse("Result map should not be empty", result.isEmpty());
        
        Office ofc = result.get("01305");
        assertNotNull("Office should be found in map", ofc);
        assertEquals("Office code should match", "01305", ofc.getOfficeCode());
        
        verify(mockOfficeDataServiceProxy, times(1)).flpOfficesByFlpCodeList(flpStringCodesArray);
    }

    @Test
    public void test_retrieveOfficesByFlpCodes() {
        // Arrange
        List<String> stringList = new ArrayList<String>();
        stringList.add("A");
        
        List<Office> serviceCenterOfficesList = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOfficesList.add(office);
        
        String[] expectedArray = {"A"};
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(expectedArray))
            .thenReturn(serviceCenterOfficesList);
            
        // Act
        List<Office> officeList = mrtProxyBusinessService.retrieveOfficesByFlpCodes(stringList);
        
        // Assert
        assertNotNull("Office list should not be null",