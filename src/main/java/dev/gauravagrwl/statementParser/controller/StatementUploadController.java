package dev.gauravagrwl.statementParser.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import dev.gauravagrwl.statementParser.model.TransactionDetails;
import dev.gauravagrwl.statementParser.parser.BankTypeOne;

@RestController
@RequestMapping("/uploadFile")
public class StatementUploadController {
	
	@Autowired
	private BankTypeOne statementParser;

	@RequestMapping(path = "/singlefileupload/", method = RequestMethod.POST, produces = MediaType.ALL_VALUE)
	public ResponseEntity<String> processFile(@RequestParam("file") MultipartFile file)
			throws IOException, SAXException, TikaException {
		InputStream inputStream =  new BufferedInputStream(file.getInputStream());
		System.out.println("File Name: " + file.getOriginalFilename());
		String year = StringUtils.left(file.getOriginalFilename(), 4);
		ContentHandler contenthandler = new BodyContentHandler();
		AutoDetectParser pdfparser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		pdfparser.parse(inputStream, contenthandler, metadata);
		String data = contenthandler.toString();
		TransactionDetails parse = statementParser.parse(data, year);
		
		ObjectMapper mapper = JsonMapper.builder()
				   .addModule(new JavaTimeModule())
				   .build();
		
		String jsonString = mapper.writeValueAsString(parse);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(
			      jsonString, headers, HttpStatus.OK);
	}

	@RequestMapping(path = "/multiplefileupload/", method = RequestMethod.POST, produces = MediaType.ALL_VALUE)
	public ResponseEntity<String> processFile(@RequestParam("files") List<MultipartFile> files) throws IOException, SAXException, TikaException {
		List<TransactionDetails> details = Lists.newArrayList();
		for (MultipartFile file : files) {
			System.out.println("File Name: " + file.getOriginalFilename());
			String year = StringUtils.left(file.getOriginalFilename(), 4);
			InputStream inputStream =  new BufferedInputStream(file.getInputStream());
			ContentHandler contenthandler = new BodyContentHandler();
			AutoDetectParser pdfparser = new AutoDetectParser();
			Metadata metadata = new Metadata();
			pdfparser.parse(inputStream, contenthandler, metadata);
			String data = contenthandler.toString();
			TransactionDetails parse = statementParser.parse(data, year);
			details.add(parse);
		}
		ObjectMapper mapper = JsonMapper.builder()
				.addModule(new ParameterNamesModule())
				   .addModule(new Jdk8Module())
				   .addModule(new JavaTimeModule())
				   .build();
		String jsonString = mapper.writeValueAsString(details);
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(
			      jsonString, headers, HttpStatus.OK);
	}
}
