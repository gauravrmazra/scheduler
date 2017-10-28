/**
 * 
 */
package com.sacknibbles.sch.controller.avro.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sacknibbles.sch.avro.model.SchedulerResponse;

/**
 * @author Sachin
 *
 */



public final class Utils {

	private static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	private static final SpecificDatumWriter<SchedulerResponse> writer = new SpecificDatumWriter<>(SchedulerResponse.class);
	private static final ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	public static String getId(){
		return UUID.randomUUID().toString();
	}
	
	private static <T>String convertToAvroPayload(T t) throws IOException{
		String payload = null;
		JsonEncoder encoder = null;
		if(t instanceof SchedulerResponse){
			 encoder = EncoderFactory.get().jsonEncoder(SchedulerResponse.SCHEMA$, out );
			writer.write((SchedulerResponse)t, encoder);
			payload = out.toString();
			
		}
		if(Objects.nonNull(encoder))
			encoder.flush();
		
		return payload;
	}
	
	public static <T> ResponseEntity<String> generateResponseEntity(T t) {
		ResponseEntity<String> responseEntity = null;
		HttpStatus httpStatus = null;
		String body = null;
		if(t instanceof SchedulerResponse){
			 SchedulerResponse response = (SchedulerResponse) t;
			 if(Objects.nonNull(response.getException())){
				 httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			 }else{
				 httpStatus = HttpStatus.OK;
			 }
			 try {
				 body = convertToAvroPayload(t);
				} catch (Exception e) {
					 logger.error("",e);
					 httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
					 response.setException(e.toString());
					 response.setResponseMessage(response.getResponseMessage()+ "| Exception occured while generating service response");
					 body = response.toString();
				}
		}
		responseEntity = ResponseEntity.status(httpStatus).
				 body(body);
		return responseEntity;
	}
}