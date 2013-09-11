package com.enterpriseandroid.googleappengineContacts.dao;

import java.io.IOException;

public class VersionNotMatchException extends IOException {

	private static final long serialVersionUID = -2569918372089349114L;

	public VersionNotMatchException (String message, Throwable cause)  {
		super(message, cause);
	}

	public VersionNotMatchException (String message)  {
		super(message);
	}

}	
