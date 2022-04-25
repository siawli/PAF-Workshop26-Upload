package AmazonS3.workshop2555.AmazonS3.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@RestController
public class UploadController {

    @Autowired
    private AmazonS3 s3;
    
    @PostMapping(path="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(
            @RequestParam("img-file") MultipartFile imageFile, 
            @RequestPart String name,
            @RequestPart String shortNote) {

        String fileName = imageFile.getOriginalFilename();
        Long imageSize = imageFile.getSize();
        String contentType = imageFile.getContentType();
        
        SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
        String timeStamp = date.format(new Date());

        byte[] buff = new byte[0];

        try {
            buff = imageFile.getBytes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String uuid = UUID.randomUUID().toString().substring(0, 8);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(imageSize);
        metadata.addUserMetadata("Original-Name", fileName);
        metadata.addUserMetadata("Create-Time", timeStamp);
        metadata.addUserMetadata("Notes", shortNote);
        metadata.addUserMetadata("Uploader", name);
        metadata.addUserMetadata("Image-Size", imageSize.toString());
        //key cannot have spaces in them -> if not s3 amazon will throw 400 badrequest error

        // Map<String, String> customUserMetadata = new HashMap<>();
        // customUserMetadata.put("original file name", fileName);
        // customUserMetadata.put("upload timestamp", timeStamp);
        // customUserMetadata.put("short note", shortNote);
        // metadata.setUserMetadata(customUserMetadata);

        JsonObjectBuilder messageB = Json.createObjectBuilder();

        try {
            PutObjectRequest putReq = new PutObjectRequest("paf-siawli", "image/" + uuid,
                imageFile.getInputStream(), metadata);
            putReq.setCannedAcl(CannedAccessControlList.PublicRead);
            s3.putObject(putReq);
            JsonObject message = messageB.add("UUID", uuid).build();
            return ResponseEntity.ok(message.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            JsonObject message = messageB.add("error", ex.getMessage()).build();
            return ResponseEntity.status(500).body(message.toString());
        }
    }

    @GetMapping("/blob/{id}")
    public ResponseEntity<byte[]> returnImage(@PathVariable String id) {

        GetObjectRequest getReq = new GetObjectRequest("paf-siawli", "image/" + id);
        
        S3Object obj = null;
        // need to catch Exception to show ResponseEntity.notFound().build() page
        // if not, errorStackTrace will be shown
        try {
            obj = s3.getObject(getReq);
        } catch (Exception ex) {
            System.out.println(">>>>>>>>ERROR");
            ex.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        
        // S3Object obj = s3.getObject(getReq);
        // if (obj == null) {
        //     return ResponseEntity.notFound().build();
        // }

        ObjectMetadata metadata = obj.getObjectMetadata();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", metadata.getContentType());
        headers.set("Original-Name", metadata.getUserMetaDataOf("Original-Name"));
        headers.set("Create-Time", metadata.getUserMetaDataOf("Create-Time"));
        headers.set("Uploader", metadata.getUserMetaDataOf("Uploader"));
        headers.set("Notes", metadata.getUserMetaDataOf("Notes"));

        try {
            byte[] buff = IOUtils.toByteArray(obj.getObjectContent());
            return ResponseEntity.ok().headers(headers).body(buff);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).build();
        }

    }
    
}






