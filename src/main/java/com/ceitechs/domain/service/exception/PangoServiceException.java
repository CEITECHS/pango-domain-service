package com.ceitechs.domain.service.exception;

import lombok.Getter;
import lombok.Setter;

public class PangoServiceException extends RuntimeException {
	
	private static final long serialVersionUID = -8093401978502316980L;
	
	@Getter
	@Setter
	private String errorCode;
	
	

	public PangoServiceException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
	
	public PangoServiceException(String errorCode, String errorMessage, Throwable cause) {
		super(errorMessage, cause);
		this.errorCode= errorCode;
    }

}
