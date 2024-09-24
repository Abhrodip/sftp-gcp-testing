package com.sftp.file;

import com.hdfc.ef.secretManager.service.SecretProvider;
import com.hdfcbank.ef.apiconnect.builder.GetRequest;
import com.hdfcbank.ef.apiconnect.builder.PostRequest;
import com.hdfcbank.ef.apiconnect.service.APIClient;
import com.sftp.file.model.Email;
import com.sftp.file.service.EmailService;
import com.sftp.file.service.SecretManagerService;
import io.dapr.client.DaprClient;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
        "file.uploader.storage.provider=GCP Bucket",
        "sftp.keyFilePath=mocked/path/to/testfile.txt",
        "camel.storage.googleStorage=mocked-storage-path",
        "gcp.secret.cacheName=mockedCacheName",
        "mail.restURL=http://mocked-url.com",
        "email.config.success.to=mocked-to@example.com",
        "email.config.success.from=mocked-from@example.com",
        "email.config.success.cc=mocked-cc@example.com",
        "email.config.success.bcc=mocked-bcc@example.com",
        "email.config.success.status=success",
        "email.config.success.subject=Mocked Subject"

})
class EmailServiceTest {

    @MockBean
    @Qualifier("buildDaprClient")
    DaprClient daprClient;

    @InjectMocks
    private EmailService emailService;

    @MockBean
    private SecretProvider secretProvider;


    @Autowired
    private SecretManagerService secretManagerService;
    @MockBean
    private APIClient apiClient;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(emailService, "to", "mocked-to@example.com");
        ReflectionTestUtils.setField(emailService, "from", "mocked-from@example.com");
        ReflectionTestUtils.setField(emailService, "cc", "mocked-cc@example.com");
        ReflectionTestUtils.setField(emailService, "bcc", "mocked-bcc@example.com");
        ReflectionTestUtils.setField(emailService, "status", "mocked-status");
        ReflectionTestUtils.setField(emailService, "subject", "mocked-subject");
    }

    @Test
    public void testSendEmail() throws Exception {
        // Arrange: Mock APIClient behavior
        Mono<String> mockedResponse = Mono.just("Email sent successfully");

        // Spy on the execute method
        doReturn(mockedResponse).when(apiClient).execute(any(PostRequest.class));

        // Act: Call the method under test
        emailService.sendEmail("This is a test email body",
                "mocked-to@example.com",
                "mocked-from@example.com",
                "mocked-cc@example.com",
                "mocked-bcc@example.com",
                "success",
                "Mocked Subject");

        // Assert: Verify that the APIClient was called with the correct parameters
        verify(apiClient, times(1)).execute(any(PostRequest.class));
    }


    @Test
    public void testSetEmailData() throws Exception {

        EmailService emailService = new EmailService();

        // Given: Input parameters for the email
        String body = "This is a test email body.";
        String to = "recipient@example.com";
        String from = "sender@example.com";
        String cc = "cc@example.com";
        String bcc = "bcc@example.com";
        String status = "Sent";
        String subject = "Test Email Subject";

        // Use reflection to access the private method
        Method setEmailDataMethod = EmailService.class.getDeclaredMethod(
                "setEmailData", String.class, String.class, String.class, String.class, String.class, String.class, String.class
        );
        setEmailDataMethod.setAccessible(true);  // Allow access to the private method

        // When: Invoke the private method using reflection
        Email email = (Email) setEmailDataMethod.invoke(emailService, body, to, from, cc, bcc, status, subject);

        // Then: Verify that the Email object is created correctly
        assertEquals(to, email.getTo(), "Email 'to' address should match");
        assertEquals(from, email.getFrom(), "Email 'from' address should match");
        assertEquals(cc, email.getCc(), "Email 'cc' address should match");
        assertEquals(bcc, email.getBcc(), "Email 'bcc' address should match");
        assertEquals(status, email.getStatus(), "Email status should match");
        assertEquals(subject, email.getSubject(), "Email subject should match");
        assertEquals(body, email.getMailBody(), "Email body should match");
    }


    @Test
    public void testSetMailParams() {

        when(exchange.getIn()).thenReturn(message);

        // Call the method under test
        emailService.setMailParams(exchange);


        // Verify that the headers were set correctly
        verify(message).setHeader("to-address", "mocked-to@example.com");
        verify(message).setHeader("from-address", "mocked-from@example.com");
        verify(message).setHeader("cc", "mocked-cc@example.com");
        verify(message).setHeader("bcc", "mocked-bcc@example.com");
        verify(message).setHeader("status", "mocked-status");
        verify(message).setHeader("subject", "mocked-subject");
    }
}