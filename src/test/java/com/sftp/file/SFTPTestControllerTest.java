package com.sftp.file;

import com.sftp.file.controller.SFTPTestController;
import com.sftp.file.processor.FileMetaDataProcessor;
import com.sftp.file.routes.SFTPRoute;
import com.sftp.file.service.EmailService;
import com.sftp.file.service.GCPBucketService;
import com.sftp.file.service.SecretManagerService;
import io.dapr.client.DaprClient;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@TestPropertySource(properties = {
        "camel.storage.googleStorage=mocked-storage-path",
        "sftp.keyFilePath=mocked/path/to/testfile.txt"
})
public class SFTPTestControllerTest {

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

    private MockMvc mockMvc;


    @InjectMocks
    private SFTPTestController sftpTestController;



    @BeforeEach
    public void setUp() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this);
        // Build MockMvc for simulating HTTP requests
        mockMvc = MockMvcBuilders.standaloneSetup(sftpTestController).build();
    }

    @Test
    public void testPublishDataToKafka_Success() throws Exception {
        // Given: Mocked data and successful scenario
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        payload.put("data", data);

        // Mock GCPBucketService to do nothing (success scenario)
        doNothing().when(gcpBucketService).publishFileMetadataToKafk(data);

        // When: Perform the POST request and verify the response
        mockMvc.perform(post("/sftp/publishDataToKafka")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"data\": { \"key\": \"value\" } }")) // Request body as JSON
                .andExpect(status().isOk())
                .andExpect(content().string("Publish Data Successfully to Kafka Topic"));
    }

    @Test
    public void testPublishDataToKafka_InternalServerError() throws Exception {
        // Given: Mocked data and failure scenario
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        payload.put("data", data);

        // Mock GCPBucketService to throw an exception (error scenario)
        doThrow(new RuntimeException("Kafka publishing failed")).when(gcpBucketService).publishFileMetadataToKafk(data);

        // When: Perform the POST request and verify the response
        mockMvc.perform(post("/sftp/publishDataToKafka")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"data\": { \"key\": \"value\" } }")) // Request body as JSON
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Kafka publishing failed"));
    }
}