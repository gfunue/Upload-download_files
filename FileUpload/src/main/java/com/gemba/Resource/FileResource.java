package com.gemba.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@RestController
@RequestMapping("/file")
public class FileResource {
	//Define the file location
	public static final String DIRECTORY = System.getProperty("user.home") + "/";
	
	//Method to upload file
	@PostMapping("/upload")
	public ResponseEntity<List<String>> uploadFile(@RequestParam("files") List<MultipartFile> multipartFiles) throws IOException{
		List<String> fileNames = new ArrayList<>();	
		for(MultipartFile file : multipartFiles) {
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			Path fileStorage = get(DIRECTORY, fileName).toAbsolutePath().normalize();
			copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
		    fileNames.add(fileName);
		}
		return ResponseEntity.ok().body(fileNames);
	}
	
	//Method to download file
	@GetMapping("/download/{fileName}")
	public ResponseEntity<Resource> downloadFiles(@PathVariable("fileName") String fileName) throws IOException{
		Path filePath = get(DIRECTORY).toAbsolutePath().normalize().resolve(fileName);
		if(!Files.exists(filePath)) {
			throw new FileNotFoundException(fileName + "was not found");
		}
		Resource resource = new UrlResource(filePath.toUri());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("File-Name", fileName);
		httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + resource.getFilename());
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(filePath))).headers(httpHeaders).body(resource);
	}

}
