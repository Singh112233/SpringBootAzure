package com.springboot.azureapp.controller;
import com.aspose.words.SaveOutputParameters;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.springboot.azureapp.exception.DemoModelNotFoundException;
import com.springboot.azureapp.model.DemoModel;
import com.springboot.azureapp.service.DemoService;
import jakarta.servlet.http.HttpServletResponse;
import com.aspose.words.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Paragraph;

@RestController
@RequestMapping("/api")
public class  DemoController {
    @Autowired
    private DemoService demoService;

    @GetMapping("/healthcheck")
    public String healthCheck(){
        return "OK";
    }
    @GetMapping("/get/entries")
    public List<DemoModel> getAllEntries(){
        return demoService.getAllEntries();
    }

    @GetMapping("/get/entries/{id}")
    public DemoModel getEntries(@PathVariable Long id) throws DemoModelNotFoundException {
        return demoService.getEntries(id);
    }
    @PostMapping("/add/entries")
    public DemoModel addEntries(@RequestBody DemoModel demoModel){
        return demoService.addEntries(demoModel);
    }

    @GetMapping("/update/entries/name")
    public DemoModel getEntriesByName(@RequestParam String name){
        return demoService.getEntriesByName(name);
    }

    @PostMapping("/extract")
    public AnalyzeResult extractPdfData(@RequestParam("file") MultipartFile file) throws Exception {
       // String blobUrl = demoService.uploadFile(file);
        return demoService.extractDataFromPdf(file);

    }

    @GetMapping("/liveMetrics")
    public String getMetrics(@RequestParam String metricName, @RequestParam String timespan) {
        return demoService.getMetrics(metricName, timespan);
    }
    @PostMapping("/convertdocx")
    public ResponseEntity<String> convertToPdf(@RequestParam("file") MultipartFile file) {
        if (!file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Only DOCX files are supported.");
        }


         //   byte[] pdfBytes = demoService.convertDocxToPdf(file.getInputStream());

            // Get the original file name without extension
            String originalFileName = file.getOriginalFilename();
            String baseFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + baseFileName + "_pdf.pdf\"");
            headers.set(HttpHeaders.CONTENT_TYPE, "application/pdf");

         //   return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        return null;
    }
//    @RequestMapping("/convertWordToPdf")
//    public void convertWordToPdf(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws Exception {
//        // Load the Word document from the uploaded file
//        XWPFDocument doc = new XWPFDocument(file.getInputStream());
//
//        // Set response headers for PDF file
//        response.setContentType("application/pdf");
//        String originalFileName = file.getOriginalFilename();
//        String baseFileName = originalFileName != null && originalFileName.contains(".")
//                ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
//                : "converted";
//        response.setHeader("Content-Disposition", "attachment; filename=\"" + baseFileName + "_pdf.pdf\"");
//
//        // Write the PDF content to the response's output stream
//        PdfWriter writer = new PdfWriter(response.getOutputStream());
//        PdfDocument pdf = new PdfDocument(writer);
//        Document pdfDoc = new Document(pdf);
//
//        // Add each paragraph from the Word document to the PDF
//        for (XWPFParagraph paragraph : doc.getParagraphs()) {
//            pdfDoc.add(new Paragraph(paragraph.getText()));
//        }
//
//        // Add each table from the Word document to the PDF
//        for (XWPFTable table : doc.getTables()) {
//            int numCols = table.getRow(0).getTableCells().size();
//            Table pdfTable = new Table(numCols);
//            for (XWPFTableRow row : table.getRows()) {
//                for (XWPFTableCell cell : row.getTableCells()) {
//                    pdfTable.addCell(new Cell().add(new Paragraph(cell.getText())));
//                }
//            }
//            pdfDoc.add(pdfTable);
//        }
//
//        // Close resources
//        pdfDoc.close();
//        doc.close();
//    }

    @RequestMapping("/convertWordToPdf")
    public MultipartFile convertWordToPdf1(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws Exception {
        // Load the Word document using Aspose.Words
        Document doc = new Document(file.getInputStream());

        // Set response headers for PDF file
        response.setContentType("application/pdf");
        String originalFileName = file.getOriginalFilename();
        String baseFileName = originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                : "converted";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + baseFileName + "_pdf.pdf\"");

        // Save the document as PDF directly to the response output stream
        return (MultipartFile) doc.save(response.getOutputStream(), com.aspose.words.SaveFormat.PDF);
    }

 
  
    @RequestMapping("/docx-to-pdf")
    public ResponseEntity<String> convertDocxToPdf(@RequestParam("file") MultipartFile docxFile) {
        if (docxFile == null || docxFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        try {
            // Step 1: Create a new conversion task
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Prepare request body for creating a conversion task
            String createTaskBody = new JSONObject()
                    .put("input", "upload")
                    .put("output_format", "pdf")
                    .put("file", "file")
                    .toString();

            HttpEntity<String> taskEntity = new HttpEntity<>(createTaskBody, headers);
            ResponseEntity<String> taskResponse = restTemplate.exchange(API_URL, HttpMethod.POST, taskEntity, String.class);

            if (taskResponse.getStatusCode() == HttpStatus.OK) {
                // Parse the task creation response to get the upload URL
                JSONObject taskResponseBody = new JSONObject(taskResponse.getBody());
                String uploadUrl = taskResponseBody.getJSONObject("data").getString("upload_url");

                // Step 2: Upload the file to the CloudConvert upload URL
                HttpHeaders uploadHeaders = new HttpHeaders();
                uploadHeaders.set("Authorization", "Bearer " + API_KEY);
                uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> uploadBody = new LinkedMultiValueMap<>();
                uploadBody.add("file", docxFile.getResource());

                HttpEntity<MultiValueMap<String, Object>> uploadEntity = new HttpEntity<>(uploadBody, uploadHeaders);

                ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.POST, uploadEntity, String.class);

                if (uploadResponse.getStatusCode() == HttpStatus.OK) {
                    // Step 3: Start the conversion after the file is uploaded
                    JSONObject uploadResponseBody = new JSONObject(uploadResponse.getBody());
                    String fileId = uploadResponseBody.getJSONObject("data").getString("id");

                    // Step 4: Start the task and wait for the conversion to complete
                    String startConversionBody = new JSONObject()
                            .put("input_file", fileId)
                            .put("output_format", "pdf")
                            .toString();

                    HttpEntity<String> startConversionEntity = new HttpEntity<>(startConversionBody, uploadHeaders);
                    ResponseEntity<String> startConversionResponse = restTemplate.exchange("https://api.cloudconvert.com/v2/tasks/" + fileId + "/start", HttpMethod.POST, startConversionEntity, String.class);

                    if (startConversionResponse.getStatusCode() == HttpStatus.OK) {
                        // Step 5: Check for completion and download the converted file URL
                        JSONObject conversionResponseBody = new JSONObject(startConversionResponse.getBody());
                        String downloadUrl = conversionResponseBody.getJSONObject("data").getString("download_url");
                        return ResponseEntity.ok("Conversion started successfully. You can download the PDF from: " + downloadUrl);
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting conversion.");
                    }

                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed.");
                }

            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Task creation failed.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during conversion: " + e.getMessage());
        }
    }
}



