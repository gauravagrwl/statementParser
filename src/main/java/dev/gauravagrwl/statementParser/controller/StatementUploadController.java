package dev.gauravagrwl.statementParser.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import dev.gauravagrwl.statementParser.controller.ChasePdfParser.ChaseRecord;

@RestController
@RequestMapping("/uploadFile")
public class StatementUploadController {

	@RequestMapping(path = "/singlefileupload/", method = RequestMethod.POST)
	public ResponseEntity<List<ChaseRecord>> processFile(@RequestParam("file") MultipartFile file)
			throws IOException, SAXException, TikaException {
		List<ChasePdfParser.ChaseRecord> full = new ArrayList<>();
		InputStream inputStream =  new BufferedInputStream(file.getInputStream());
		System.out.println("File Name: " + file.getOriginalFilename());
		String year = StringUtils.left(file.getOriginalFilename(), 4);
		ContentHandler contenthandler = new BodyContentHandler();
		AutoDetectParser pdfparser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		pdfparser.parse(inputStream, contenthandler, metadata);
		String data = contenthandler.toString();
		List<ChasePdfParser.ChaseRecord> chaseRecords = ChasePdfParser.parse(data, year);
		full.addAll(chaseRecords);
		return (new ResponseEntity<List<ChasePdfParser.ChaseRecord>>(full, null, HttpStatus.OK));
	}

	@RequestMapping(path = "/multiplefileupload/", method = RequestMethod.POST)
	public ResponseEntity<List<ChaseRecord>> processFile(@RequestParam("files") List<MultipartFile> files) throws IOException, SAXException, TikaException {
		List<ChasePdfParser.ChaseRecord> full = new ArrayList<>();

		for (MultipartFile file : files) {
			System.out.println("File Name: " + file.getOriginalFilename());
			String year = StringUtils.left(file.getOriginalFilename(), 4);
			InputStream inputStream =  new BufferedInputStream(file.getInputStream());
			ContentHandler contenthandler = new BodyContentHandler();
			AutoDetectParser pdfparser = new AutoDetectParser();
			Metadata metadata = new Metadata();
			pdfparser.parse(inputStream, contenthandler, metadata);
			String data = contenthandler.toString();
			List<ChasePdfParser.ChaseRecord> chaseRecords = ChasePdfParser.parse(data, year);
			full.addAll(chaseRecords);
			
		}

		return (new ResponseEntity<List<ChasePdfParser.ChaseRecord>>(full, null, HttpStatus.OK));
	}
}
