package org.c4c.jaas.mysql;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class MySqlLoginCallbackHandler implements CallbackHandler{
	
	private String _username;
	private char[] _passwd;
	public MySqlLoginCallbackHandler(String username, char[]passwd) {
		this._username = username;
		this._passwd = passwd;
	}
	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		NameCallback nameCallBack = (NameCallback)callbacks[0];
		PasswordCallback passwordCallback = (PasswordCallback)callbacks[1];
		nameCallBack.setName(_username);
		passwordCallback.setPassword(_passwd);
		
	}

}
