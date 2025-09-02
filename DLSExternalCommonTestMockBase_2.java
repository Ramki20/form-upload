package gov.usda.fsa.fcao.flp.flpids.common.business.common;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring 6.x compatible base test class replacing SimpleNamingContextBuilder
 * with Mockito-based JNDI mocking for JDK 17
 */
public class DLSExternalCommonTestMockBase extends DLSExternalCommonTestAgency {
    
    protected static MockInitialContextFactory mockContextFactory;
    protected static Context mockContext;
    protected static Context mockSubContext;
    
    @BeforeAll
    public static void jndiSetup() throws Exception {
        // Set up the mock JNDI context factory
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
        
        mockContextFactory = new MockInitialContextFactory();
        mockContext = mock(Context.class);
        mockSubContext = mock(Context.class);
        
        // Set up the mock context factory to return our mock context
        MockInitialContextFactory.setMockContext(mockContext);
        
        // Configure all JNDI bindings
        configureJndiBindings();
    }
    
    private static void configureJndiBindings() throws NamingException {
        // Configure namespace root
        when(mockContext.lookup("java:comp/env/name_space_root")).thenReturn("cell/persistent");
        when(mockContext.lookup("cell/persistent")).thenReturn(mockSubContext);
        
        // Configure application identifier
        when(mockSubContext.lookup("java:comp/env/application_identifier")).thenReturn("cbs-client");
        when(mockContext.lookup("java:comp/env/application_identifier")).thenReturn("cbs-client");
        
        // Configure CBS service
        when(mockSubContext.lookup("gov/usda/fsa/common/citso/cbs/sharedservice_specifier")).thenReturn("WS");
        when(mockSubContext.lookup("gov/usda/fsa/common/citso/cbs/web_service_endpoint_url"))
            .thenReturn("http://int1-internal-services.fsa.usda.gov/cbs-ejb/services/CommonBusinessDataServicePort?wsdl");
        
        // Configure FRS service - multiple binding paths
        when(mockContext.lookup("cell/persistent/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        when(mockSubContext.lookup("gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        when(mockContext.lookup("java:comp/env/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        when(mockSubContext.lookup("java:comp/env/gov/fsa/usda/common/frs_service_specifier")).thenReturn("WS");
        
        // Configure SCIMS customer support - both underscore and dot notation
        when(mockContext.lookup("cell/persistent/gov/usda/fsa/fcao/flp/dls/support_create_scims_customer")).thenReturn("Y");
        when(mockContext.lookup("gov/usda/fsa/fcao/flp/dls/support_create_scims_customer")).thenReturn("Y");
        when(mockContext.lookup("gov/usda/fsa/fcao/flp/dls/support.create.scims.customer")).thenReturn("Y");
        when(mockSubContext.lookup("gov/usda/fsa/fcao/flp/dls/support.create.scims.customer")).thenReturn("Y");
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        test_jndiconfig();
    }
    
    protected void test_jndiconfig() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            InitialContext mockInitialContext = mock(InitialContext.class);
            mockedStatic.when(() -> new InitialContext()).thenReturn(mockInitialContext);
            mockedStatic.when(() -> new InitialContext(any(Hashtable.class))).thenReturn(mockInitialContext);
            
            // Configure the mock to return our predefined values
            when(mockInitialContext.lookup("java:comp/env/name_space_root")).thenReturn("cell/persistent");
            when(mockInitialContext.lookup("cell/persistent")).thenReturn(mockSubContext);
            when(mockInitialContext.lookup("cell/persistent/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
            when(mockInitialContext.lookup("java:comp/env/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
            
            when(mockSubContext.lookup("gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
            
            // Test the JNDI configuration
            InitialContext ctx = new InitialContext();
            String contextRoot = (String) ctx.lookup("java:comp/env/name_space_root");
            Context subContext = (Context) ctx.lookup(contextRoot);
            assertNotNull(subContext);

            String value1 = (String) subContext.lookup("gov/usda/fsa/common/frs_service_specifier");
            String value2 = (String) ctx.lookup("cell/persistent/gov/usda/fsa/common/frs_service_specifier");
            String value3 = (String) ctx.lookup("java:comp/env/gov/usda/fsa/common/frs_service_specifier");

            assertNotNull(value1);
            assertNotNull(value2);
            assertNotNull(value3);
            assertEquals(value1, value2);
            assertEquals(value2, value3);
        }
    }
    
    /**
     * Utility method to update JNDI bindings during test execution
     * Replaces the old builder.bind() functionality
     */
    protected void updateJndiBinding(String jndiName, String value) throws NamingException {
        // Update the mock context bindings
        when(mockContext.lookup(jndiName)).thenReturn(value);
        when(mockSubContext.lookup(jndiName)).thenReturn(value);
        
        // Handle common JNDI path variations
        if (jndiName.contains("support.create.scims.customer")) {
            String underscoreVersion = jndiName.replace("support.create.scims.customer", "support_create_scims_customer");
            when(mockContext.lookup(underscoreVersion)).thenReturn(value);
            when(mockSubContext.lookup(underscoreVersion)).thenReturn(value);
            
            // Also update cell/persistent prefix versions
            when(mockContext.lookup("cell/persistent/" + jndiName)).thenReturn(value);
            when(mockContext.lookup("cell/persistent/" + underscoreVersion)).thenReturn(value);
        }
    }
    
    // Custom InitialContextFactory for testing - Spring 6.x compatible
    public static class MockInitialContextFactory implements InitialContextFactory {
        private static Context mockContext;
        
        public static void setMockContext(Context context) {
            mockContext = context;
        }
        
        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return mockContext;
        }
    }
}