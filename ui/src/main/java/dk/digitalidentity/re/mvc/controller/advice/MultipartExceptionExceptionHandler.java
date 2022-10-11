package dk.digitalidentity.re.mvc.controller.advice;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class MultipartExceptionExceptionHandler {

	@ExceptionHandler(MultipartException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public String handleMultipartException(MultipartException ex, HttpServletResponse response, Model model) {
		return "error/maxfilesizeerror";
	}

}