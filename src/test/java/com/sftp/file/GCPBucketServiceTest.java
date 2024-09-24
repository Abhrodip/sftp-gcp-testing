package com.sftp.file;

import com.hdfcbank.messageconnect.config.PubSubOptions;
import com.hdfcbank.messageconnect.dapr.producer.DaprProducer;
import com.sftp.file.routes.SFTPRoute;
import com.sftp.file.service.EmailService;
import com.sftp.file.service.GCPBucketService;
import com.sftp.file.service.SecretManagerService;
import io.dapr.client.DaprClient;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        "file.upload.pubsubname=il-imps-audit-pubsub",
        "file.upload.topicname=il-sftp-gcp-bucket-metadata",
        "file.uploader.storage.provider=GCP Bucket",
        "sftp.keyFilePath=/mock/keyfile/path"
})
class GCPBucketServiceTest {


    @MockBean
    @Qualifier("buildDaprClient")
    DaprClient daprClient;
    @InjectMocks
    private GCPBucketService gcpBucketService;

    @Mock
    private DaprProducer daprProducer;

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



    private static final String TOPIC_NAME = "mocked-topic";
    private static final String PUBSUB_NAME = "mocked-pubsub";
    private static final String STORAGE_PROVIDER_NAME = "mocked-storage-provider";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testPushFileMetadata() throws Exception {
        // Set up mock headers for the Exchange message
        when(exchange.getIn()).thenReturn(message);
        when(message.getHeader("CamelGoogleCloudStorageContentLength", Long.class)).thenReturn(1024L);
        when(message.getHeader("CamelGoogleCloudStorageBucketName", String.class)).thenReturn("test-bucket");
        when(message.getHeader("CamelGoogleCloudStorageObjectName", String.class)).thenReturn("test-folder/testfile.txt");
        when(message.getHeader("CamelFileName", String.class)).thenReturn("testfile.txt");

        // Mock daprProducer to return a successful Mono response
        when(daprProducer.invokeDaprPublishEvent(any())).thenReturn(Mono.empty());

        // Execute the method under test
        gcpBucketService.pushFileMetadata(exchange);

        // Verify that daprProducer.invokeDaprPublishEvent was called with the expected parameters
        verify(daprProducer, times(1)).invokeDaprPublishEvent(any());
    }

    @Test
    void testPublishFileMetadataToKafka() {
        // Mock metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", "testfile.txt");
        metadata.put("bucketName", "test-bucket");

        // Mock daprProducer to return a successful Mono response
        when(daprProducer.invokeDaprPublishEvent(any())).thenReturn(Mono.empty());

        // Execute the method under test
        gcpBucketService.publishFileMetadataToKafk(metadata);

        // Verify that daprProducer.invokeDaprPublishEvent was called with the expected parameters
        verify(daprProducer, times(1)).invokeDaprPublishEvent(any());
    }


    @Test
    void testPublishDataToKafka_SubscriptionComplete() {
        // Prepare the payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("kashkey", "hashvalue");

        // Mock the Mono to simulate completion
        when(daprProducer.invokeDaprPublishEvent(any())).thenReturn(Mono.empty());

        // Call the method
        gcpBucketService.publishFileMetadataToKafk(payload);

        // Verify that the Mono is subscribed to
        verify(daprProducer, times(1)).invokeDaprPublishEvent(any());
    }


}

