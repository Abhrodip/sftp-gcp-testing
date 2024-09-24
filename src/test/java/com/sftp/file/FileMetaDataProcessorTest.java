package com.sftp.file;

import com.sftp.file.processor.FileMetaDataProcessor;
import com.sftp.file.routes.SFTPRoute;
import com.sftp.file.service.EmailService;
import com.sftp.file.service.GCPBucketService;
import com.sftp.file.service.SecretManagerService;
import io.dapr.client.DaprClient;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.google.storage.GoogleCloudStorageConstants;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.*;


@CamelSpringBootTest
@SpringBootTest
@ComponentScan(basePackages = {"com.sftp.file"})
@TestPropertySource(properties = {
        "sftp.host=localhost",
        "sftp.port=22",
        "sftp.path=/path/to/files",
        "sftp.username=testuser",
        "sftp.password=testpassword",
        "sftp.delay=1000",
        "sftp.targetpath=/target/path",
        "camel.component.gcs.bucket.folder=/gcs/folder",
        "camel.component.gcs.bucket=test-bucket",
        "gcp.projectId=test-project",
        "gcp.secret.sftpSecret.secretId=secret-id",
        "gcp.secret.sftpSecret.secretVersion=1",
        "gcp.secret.sftpUsername.secretId=username-secret-id",
        "gcp.secret.sftpUsername.secretVersion=1",
        "mail.restURL=mail.restURL=http://localhost:8080/api/mail",
        "sftp.keyFilePath=/mock/keyfile/path"
})
class FileMetaDataProcessorTest {

    @MockBean
    @Qualifier("buildDaprClient")
    DaprClient daprClient;

    @MockBean
    private GCPBucketService gcpBucketService;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @MockBean
    private SecretManagerService secretManager;

    @MockBean
    private ProducerTemplate template;

    @MockBean
    private EmailService emailService;

    @InjectMocks
    private SFTPRoute sftpRoute;


    @InjectMocks
    private FileMetaDataProcessor fileMetaDataProcessor;




    private final String bucketName = "test-bucket";
    private final String gcpTargetPath = "gcp/path/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileMetaDataProcessor = new FileMetaDataProcessor(bucketName, gcpTargetPath);

        // Mock the exchange's message
        when(exchange.getIn()).thenReturn(message);
    }

    @Test
    void testProcess() throws Exception {
        // Mock the file name and file size
        String testFileName = "test-file.txt";
        Long testFileSize = 1024L;

        // Set up the message headers
        when(message.getHeader("CamelFileName", String.class)).thenReturn(testFileName);
        when(message.getHeader(Exchange.FILE_LENGTH, Long.class)).thenReturn(testFileSize);

        // Call the process method
        fileMetaDataProcessor.process(exchange);

        // Verify that the correct headers were set
        verify(message, times(1)).setHeader(GoogleCloudStorageConstants.CONTENT_LENGTH, testFileSize);
        verify(message, times(1)).setHeader(GoogleCloudStorageConstants.OBJECT_NAME, gcpTargetPath + testFileName);
        verify(message, times(1)).setHeader(GoogleCloudStorageConstants.BUCKET_NAME, bucketName);
        verify(message, times(1)).setHeader(GoogleCloudStorageConstants.FILE_NAME, testFileName);
    }
}

