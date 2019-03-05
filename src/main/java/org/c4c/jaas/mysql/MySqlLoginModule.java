package org.c4c.jaas.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class MySqlLoginModule implements LoginModule {

	private Map<String, ?> _options;
	private Map<String, ?> _sharedState;
	private CallbackHandler _callbackHandler;
	private Subject _subject;
	private boolean _debug = false;
	// username and password
	private String _username;
	private char[] _password;
	// the authentication status
	private boolean _succeeded = false;
	private boolean _commitSucceeded = false;
	private UserPrincipal _userPrincipal;
	private Set<RolePrincipal> _rolePrincipal;
	private String _db_driver;
	private String _db_url;
	private String _db_user;
	private String _db_passwd;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {

		this._options = options;
		this._callbackHandler = callbackHandler;
		this._subject = subject;
		this._sharedState = sharedState;
		this._debug = "true".equalsIgnoreCase((String) options.get("debug"));
		this._db_driver = (String) options.get("db_driver");
		this._db_url = (String) options.get("db_url");
		this._db_user = (String) options.get("db_user");
		this._db_passwd = (String) options.get("db_passwd");
	}

	@Override
	public boolean login() throws LoginException {

		if (_callbackHandler == null)
			throw new LoginException(
					"Error: no CallbackHandler available " + "to garner authentication information from the user");

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("user name: ");
		callbacks[1] = new PasswordCallback("password: ", false);

		try {
			_callbackHandler.handle(callbacks);
			_username = ((NameCallback) callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
			if (tmpPassword == null) {
				// treat a NULL password as an empty password
				tmpPassword = new char[0];
			}
			_password = new char[tmpPassword.length];
			System.arraycopy(tmpPassword, 0, _password, 0, tmpPassword.length);
			((PasswordCallback) callbacks[1]).clearPassword();

		} catch (java.io.IOException ioe) {
			throw new LoginException(ioe.toString());
		} catch (UnsupportedCallbackException uce) {
			throw new LoginException("Error: " + uce.getCallback().toString()
					+ " not available to garner authentication information " + "from the user");
		}

		// print debugging information
		if (_debug) {
			System.out.println("\t\t[SampleLoginModule] " + "user entered user name: " + _username);
			System.out.print("\t\t[SampleLoginModule] " + "user entered password: ");
			for (int i = 0; i < _password.length; i++)
				System.out.print(_password[i]);
			System.out.println();
		}

		// verify the username/password

		if (testUserPasswd(_username, _password)) {
			if (_debug)
				System.out.println("\t\t[SampleLoginModule] " + "authentication succeeded");
			_succeeded = true;
			return true;
		} else {

			if (_debug)
				System.out.println("\t\t[SampleLoginModule] " + "authentication failed");
			_succeeded = false;
		}
		return false;
	}

	private boolean testUserPasswd(String username, char[] passwd) {
		Connection con = null;
		ResultSet rs = null;
		boolean result = false;
		try {
			Class.forName(this._db_driver);
			con = DriverManager.getConnection(this._db_url, this._db_user, this._db_passwd);
			if (con != null) {
				PreparedStatement prepStmt = con.prepareStatement("select * from users where user_name=? and passwd=?");
				prepStmt.setString(1, username);
				prepStmt.setString(2, new String(passwd).trim());

				rs = prepStmt.executeQuery();
				if (rs != null && rs.next()) {
					int userId = rs.getInt("user_id");
					result = true;

					// Read user roles
					prepStmt = con.prepareStatement(
							"select roles.role_name from user_role inner join roles on user_role.role_id = roles.role_id and user_role.user_id = ?");
					prepStmt.setInt(1, userId);

					rs.close();

					rs = prepStmt.executeQuery();
					if (rs != null) {
						_rolePrincipal = new HashSet<>();
						while (rs.next()) {
							String role = rs.getString("role_name");
							_rolePrincipal.add(new RolePrincipal(Roles.valueOf(role)));
						}
					}
				}

			}
		} catch (ClassNotFoundException e) {
			if (_debug) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			if (_debug) {
				e.printStackTrace();
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return result;
	}

	@Override
	public boolean commit() throws LoginException {
		if (_succeeded == false) {
			return false;
		} else {
			// add a Principal (authenticated identity)
			// to the Subject

			// assume the user we authenticated is the SamplePrincipal
			_userPrincipal = new UserPrincipal(_username);
			if (!this._subject.getPrincipals().contains(this._userPrincipal))
				this._subject.getPrincipals().add(this._userPrincipal);

			if (_debug) {
				System.out.println("\t\t[SampleLoginModule] " + "added SamplePrincipal to Subject");
			}

			for (RolePrincipal p : _rolePrincipal) {
				this._subject.getPrincipals().add(p);
			}
			// in any case, clean out state
			_username = null;

			for (int i = 0; i < _password.length; i++)
				_password[i] = ' ';
			_password = null;

			_commitSucceeded = true;
			return true;
		}
	}

	@Override
	public boolean abort() throws LoginException {
		if (_succeeded == false) {
			return false;
		} else if (_succeeded == true && _commitSucceeded == false) {
			// login succeeded but overall authentication failed
			_succeeded = false;
			_username = null;
			if (_password != null) {
				for (int i = 0; i < _password.length; i++)
					_password[i] = ' ';
				_password = null;
			}
			_userPrincipal = null;
			_rolePrincipal = null;
		} else {
			// overall authentication succeeded and commit succeeded,
			// but someone else's commit failed
			logout();
		}
		return true;
	}

	@Override
	public boolean logout() throws LoginException {
		_subject.getPrincipals().remove(_userPrincipal);
		_succeeded = false;
		_succeeded = _commitSucceeded;
		_username = null;
		if (_password != null) {
			for (int i = 0; i < _password.length; i++)
				_password[i] = ' ';
			_password = null;
		}
		_userPrincipal = null;
		return true;
	}

}
