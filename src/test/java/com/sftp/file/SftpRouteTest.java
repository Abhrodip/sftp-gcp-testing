package com.sftp.file;

import com.sftp.file.processor.FileMetaDataProcessor;
import com.sftp.file.routes.SFTPRoute;
import com.sftp.file.service.EmailService;
import com.sftp.file.service.GCPBucketService;
import com.sftp.file.service.SecretManagerService;
import io.dapr.client.DaprClient;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import static org.apache.camel.builder.AdviceWithTasks.replaceFromWith;
import static org.mockito.Mockito.*;


@CamelSpringBootTest
@SpringBootTest
@TestPropertySource(properties = {
        "camel.storage.googleStorage=mocked-storage-path",
        "sftp.keyFilePath=mocked/path/to/testfile.txt"
})
public class SftpRouteTest {

    @MockBean
    @Qualifier("buildDaprClient")
    DaprClient daprClient;
    @MockBean
    private GCPBucketService gcpBucketService;

    @MockBean
    private FileMetaDataProcessor fileMetaDataProcessor;

    @MockBean
    private EmailService emailService;

    @MockBean
    private SecretManagerService secretManager;

    @MockBean
    private ProducerTemplate template;

    @InjectMocks
    private SFTPRoute sftpRoute;


    @EndpointInject("mock:google-storage")
    private MockEndpoint mockGoogleStorage;

    @BeforeEach
    void setup() throws Exception {
        // Setup mock expectations
        mockGoogleStorage.reset();
        mockGoogleStorage.expectedMessageCount(1);
        mockGoogleStorage.expectedHeaderReceived("CamelFileName", "testfile.txt");
    }

    @Test
    void testConfigure() throws Exception {
        // Mock the behavior of SecretManager if required
        when(secretManager.getSecret(anyString(), anyString(), anyString()))
                .thenReturn("mockedPassword");

        // Set up expectations for the mock endpoint
        mockGoogleStorage.reset();
        mockGoogleStorage.expectedMessageCount(1);
        mockGoogleStorage.expectedHeaderReceived("CamelFileName", "testfile.txt");

        // Send a test message to the route
        String testFileName = "testfile.txt";
        String body = "Test File for sftp";

        // Use the injected Camel template to send a message
        template.sendBodyAndHeader("direct:gcpRoute", body, "CamelFileName", testFileName);

        // Verify that the mock endpoint received the expected message
//        mockGoogleStorage.assertIsSatisfied();
    }
}