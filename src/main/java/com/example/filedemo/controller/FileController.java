package com.example.filedemo.controller;

import com.example.filedemo.model.DBFile;
import com.example.filedemo.payload.UploadFileResponse;
import com.example.filedemo.service.DBFileStorageService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
//@Controller
public class FileController {
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	
	@Autowired
	private DBFileStorageService DBFileStorageService;
	
	@PostMapping("/uploadFile")
//	@RequestMapping(value="/uploadFile", method=RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
		DBFile dbFile = DBFileStorageService.storeFile(file);
		
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/downloadFile/")
				.path(dbFile.getId())
				.toUriString();
		
		return new UploadFileResponse(dbFile.getFileName(), fileDownloadUri,
				file.getContentType(), file.getSize());
	}
	
	@PostMapping("/uploadMultipleFiles")
	public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files){
		return Arrays.asList(files)
				.stream()
				.map(file -> uploadFile(file))
				.collect(Collectors.toList());
				
	}
	
	@GetMapping("/downloadFile/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
		DBFile dbFile = DBFileStorageService.getFile(fileId);
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(dbFile.getFileName()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
				.body(new ByteArrayResource(dbFile.getData()));
	}
}
