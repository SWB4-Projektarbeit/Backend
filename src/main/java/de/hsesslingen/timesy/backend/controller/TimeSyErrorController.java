package de.hsesslingen.timesy.backend.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class TimeSyErrorController implements ErrorController {

	/**
	 * Basic mapping for error
	 *
	 * @return 404
	 */
	@RequestMapping("/error")
	public ResponseEntity<?> handleError() {
		return new ResponseEntity<>(Map.of("message", "This page does not exist"), HttpStatus.NOT_FOUND);
	}
}