package com.springboot.azureapp.service;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.springboot.azureapp.exception.DemoModelNotFoundException;
import com.springboot.azureapp.model.DemoModel;
import com.springboot.azureapp.repository.DemoRepository;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Optional;
import com.azure.core.util.BinaryData;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DemoService {

    @Value("${azure.cognitiveservices.endpoint1}")
    private String endpoint1;

    @Value("${azure.cognitiveservices.subscription-key}")
    private String subscriptionKey;

    @Autowired
    private DemoRepository demoRepository;



    public DemoModel addEntries(DemoModel demoModel) {
        return demoRepository.save(demoModel);
    }

    public DemoModel getEntries(Long id) throws DemoModelNotFoundException {
        Optional<DemoModel> demoModel = demoRepository.findById(id);
        if (!demoModel.isPresent()){
            throw new DemoModelNotFoundException("Demo Model Not Found ");
        }
        else
            return demoModel.get();
    }


    public List<DemoModel> getAllEntries() {
        return demoRepository.findAll();
    }


    public DemoModel getEntriesByName(String name) {
        return demoRepository.findByTitle(name);
    }

// Azure Form Recognizer

    @Value("${azure.formrecognizer.endpoint}")
    private String formRecognizerEndpoint;

    @Value("${azure.formrecognizer.apikey}")
    private String formRecognizerApiKey;

    public AnalyzeResult extractDataFromPdf(MultipartFile file) throws Exception {
          InputStream inputStream = file.getInputStream();
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(formRecognizerApiKey))
                .endpoint(formRecognizerEndpoint)
                .buildClient();
        BinaryData documentData = BinaryData.fromStream( inputStream,file.getSize());

        return client.beginAnalyzeDocument("PO", documentData).getFinalResult();


    }


    @Value("${application-insights.app-id}")
    private String appId;

    @Value("${application-insights.api-key}")
    private String apiKey2;

    private final RestTemplate restTemplate;

    public DemoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // metrices
    public String getMetrics(String metricName, String timespan) {
        String url = "https://api.applicationinsights.io/v1/apps/" + appId + "/metrics/" + metricName + "?timespan=" + timespan;

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey2);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make GET request to Application Insights API
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }
// BLOB
    @Value("${azure.storage.blob.connection-string}")
    private String blobConnectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobClient getBlobClient(String fileName) {
        return new BlobClientBuilder()
                .connectionString(blobConnectionString)
                .containerName(containerName)
                .blobName(fileName)
                .buildClient();
    }
    public String uploadFile(MultipartFile file) throws IOException {
        InputStream fileInputStream = file.getInputStream();
        BlobClient blobClient = getBlobClient(file.getOriginalFilename());
        blobClient.upload(fileInputStream, file.getSize(), true);

        return blobClient.getBlobUrl();
    }

}


