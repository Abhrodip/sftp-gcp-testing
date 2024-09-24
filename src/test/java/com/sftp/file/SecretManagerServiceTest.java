package com.sftp.file;

import com.hdfc.ef.secretManager.model.SecretConfigProps;
import com.hdfc.ef.secretManager.service.SecretProvider;
import com.sftp.file.service.EmailService;
import com.sftp.file.service.SecretManagerService;
import io.dapr.client.DaprClient;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "file.uploader.storage.provider=GCP Bucket",
        "sftp.keyFilePath=mocked/path/to/testfile.txt",
        "camel.storage.googleStorage=mocked-storage-path",
        "gcp.secret.cacheName=mockedCacheName",
        "mail.restURL=mail.restURL=http://localhost:8080/api/mail",
})
class SecretManagerServiceTest {

    @MockBean
    @Qualifier("buildDaprClient")
    DaprClient daprClient;
    @MockBean
    private SecretProvider secretProvider;

    @MockBean
    private EmailService emailService;

    @Autowired
    private SecretManagerService secretManagerService;



    private static final String PROJECT_ID = "test-project";
    private static final String SECRET_ID = "test-secret-id";
    private static final String VERSION_ID = "1";
    private static final String EXPECTED_SECRET = "mocked-secret";
    private static final String CACHE_NAME = "test-cache";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
//        secretManagerService = new SecretManagerService();
    }

//    @Test
//    void testGetSecret() throws Exception {
//        // Given: Set up the expected behavior for the mock
//        SecretConfigProps secretConfigProps = SecretConfigProps.builder()
//                .projectId(PROJECT_ID)
//                .secretId(SECRET_ID)
//                .secretVersion(VERSION_ID)
//                .isGCP(Boolean.TRUE)
//                .build();
//
//        // Mock the secretProvider's behavior
//        when(secretProvider.getSecret(secretConfigProps, CACHE_NAME, SECRET_ID)).thenReturn(EXPECTED_SECRET);
//
//        // When: Call the method under test
//        String actualSecret = secretManagerService.getSecret(PROJECT_ID, SECRET_ID, VERSION_ID);
//
//        // Then: Verify the result
//        assertEquals(EXPECTED_SECRET, actualSecret);
//
//        // Verify that the secretProvider.getSecret() was called with correct arguments
//        verify(secretProvider, times(1)).getSecret(secretConfigProps, CACHE_NAME, SECRET_ID);
//    }
@Test
void testGetSecret() throws Exception {
    // Given: Set up the expected behavior for the mock
    SecretConfigProps secretConfigProps = SecretConfigProps.builder()
            .projectId(PROJECT_ID)
            .secretId(SECRET_ID)
            .secretVersion(VERSION_ID)
            .isGCP(Boolean.TRUE)
            .build();

    // Mock the secretProvider's behavior
    when(secretProvider.getSecret(any(SecretConfigProps.class), eq("mockedCacheName"), eq(SECRET_ID)))
            .thenReturn(EXPECTED_SECRET);

    // When: Call the method under test
    String actualSecret = secretManagerService.getSecret(PROJECT_ID, SECRET_ID, VERSION_ID);

    // Then: Verify the result
    assertEquals(EXPECTED_SECRET, actualSecret);

    // Verify that the secretProvider.getSecret() was called with correct arguments
    verify(secretProvider, times(1)).getSecret(any(SecretConfigProps.class), eq("mockedCacheName"), eq(SECRET_ID));
}


    @Test
    void testGetSftpUsername() throws Exception {
        // Given: Mock the getSecret method to return the expected secret
        when(secretManagerService.getSecret(PROJECT_ID, SECRET_ID, VERSION_ID)).thenReturn(EXPECTED_SECRET);

        // When: Call the method under test
        String actualUsername = secretManagerService.getSftpUsername(PROJECT_ID, SECRET_ID, VERSION_ID);

        // Then: Verify the result
        assertEquals(EXPECTED_SECRET, actualUsername);

        // Verify that the getSecret() method was called once
//        verify(secretManagerService, times(1)).getSecret(PROJECT_ID, SECRET_ID, VERSION_ID);
    }
}

