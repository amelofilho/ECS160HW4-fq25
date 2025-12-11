package com.ecs160;
import com.ecs160.annotations.Microservice;
import com.ecs160.annotations.Endpoint;
import org.junit.After;
import org.junit.Test;
import org.junit.BeforeClass;

import com.sun.net.httpserver.HttpExchange;


import static org.junit.Assert.*;

import java.util.Map;


import java.nio.charset.StandardCharsets;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.net.URI;

/**
 * tests for the persistence framework: RedisDB & annotations
 * run with: mvn test (assuming ur in the persistence-framework dir)
 */
@Microservice
class MockMicroservice1 {
    @Endpoint(url = "mock1")
    public String mockfunction1(String code) {
        return "mockreturnval1";
    }

    public MockMicroservice1() {}
}

@Microservice
class MockMicroservice2 {
    public String mockfunction2(String code) {
        return "mockreturnval2";
    }

    public MockMicroservice2() {}
}

@Microservice
class MockMicroservice3 {
    @Endpoint(url = "mock3")
    public String mockfunction3(String code) {
        return code;
    }
    public MockMicroservice3() {}
}

class MockMicroservice4 {
    @Endpoint(url = "mock4")
    public String mockfunction3(String code) {
        return code;
    }
    public MockMicroservice4() {}
}

public class AppTest {
    
    private static Integer port = 8000;
    private static Launcher mockLauncher;
    private static MyHandler mockHandler;
    
    @BeforeClass
    public static void setUp(){
        @SuppressWarnings("unused")
        MockMicroservice1 mockMicroservice1 = new MockMicroservice1();
        @SuppressWarnings("unused")
        MockMicroservice2 mockMicroservice2 = new MockMicroservice2();
        @SuppressWarnings("unused")
        MockMicroservice3 mockMicroservice3 = new MockMicroservice3();
        @SuppressWarnings("unused")
        MockMicroservice4 mockMicroservice4 = new MockMicroservice4();
        Launcher launcher = new Launcher();
        mockLauncher = spy(launcher);

        doNothing().when(mockLauncher).createHttpServer(anyInt());

        mockLauncher.launch(port);

        Map<String, Method> endpointMap = mockLauncher.getEndPointMap();
        mockHandler = new MyHandler(endpointMap);
    }

    @After
    public void tearDown() {   
        //nothing?
        //delete the stuff it should be fine 

    }
    //test if can get return val from mock
    //test if wrong endpoint
    //test if the list stores mock1 and mock3
    
    //endpoint map tests
    @Test 
    public void testValidMicroserviceandEndpointSaved() throws Exception {
        assertNotNull(mockLauncher.getEndPointMethod("mock1"));
        assertEquals("mockfunction1", mockLauncher.getEndPointMethod("mock1"));

        assertNotNull(mockLauncher.getEndPointMethod("mock3"));
        assertEquals("mockfunction3", mockLauncher.getEndPointMethod("mock3"));
    }

    @Test 
    public void testValidMicroserviceNoEndpointFails() throws Exception {
        assertNull(mockLauncher.getEndPointMethod("mock2"));
        assertEquals(null, mockLauncher.getEndPointMethod("mock2"));
    }

    @Test 
    public void testNoMicroserviceWithValidEndpointFails() throws Exception {
        assertNull(mockLauncher.getEndPointMethod("mock4"));
        assertEquals(null, mockLauncher.getEndPointMethod("mock4"));
    }
    

    //handler tests
    @Test
    public void testHandlerValidRequest() throws Exception {
        String code = "mockparameters";
        ByteArrayInputStream mockParameters = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        
        HttpExchange mockExchange = mock(HttpExchange.class);
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(new URI("/mock1"));
        when(mockExchange.getRequestBody()).thenReturn(mockParameters);

        ByteArrayOutputStream mockBody = new ByteArrayOutputStream();
        when(mockExchange.getResponseBody()).thenReturn(mockBody);

        mockHandler.handle(mockExchange);

        String returnVal = mockBody.toString();
        assertEquals("mockreturnval1", returnVal);
    }

    @Test
    public void testHandlerValidRequestCorrectDecoding() throws Exception {
        String code = "This Should Be Returned";
        ByteArrayInputStream mockParameters = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        
        HttpExchange mockExchange = mock(HttpExchange.class);
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(new URI("/mock3"));
        when(mockExchange.getRequestBody()).thenReturn(mockParameters);

        ByteArrayOutputStream mockBody = new ByteArrayOutputStream();
        when(mockExchange.getResponseBody()).thenReturn(mockBody);

        mockHandler.handle(mockExchange);

        String returnVal = mockBody.toString();
        assertEquals(code, returnVal);
    }

    @Test
    public void testHandlerInvalidEndpointRejected() throws Exception {
        ByteArrayInputStream mockParameters = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));

        HttpExchange mockExchange = mock(HttpExchange.class);
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(new URI("/invalidmicroservice"));
        when(mockExchange.getRequestBody()).thenReturn(mockParameters);

        ByteArrayOutputStream mockBody = new ByteArrayOutputStream();
        when(mockExchange.getResponseBody()).thenReturn(mockBody);

        mockHandler.handle(mockExchange);

        String returnVal = mockBody.toString();
        assertEquals("Endpoint Not Found", returnVal);
    }

    @Test
    public void testHandlerInvalidMethodRejected() throws Exception {
        ByteArrayInputStream mockParameters = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));

        HttpExchange mockExchange = mock(HttpExchange.class);
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getRequestURI()).thenReturn(new URI("/mock1"));
        when(mockExchange.getRequestBody()).thenReturn(mockParameters);

        ByteArrayOutputStream mockBody = new ByteArrayOutputStream();
        when(mockExchange.getResponseBody()).thenReturn(mockBody);

        mockHandler.handle(mockExchange);

        String returnVal = mockBody.toString();
        assertEquals("Only Post Allowed", returnVal);
    }

}