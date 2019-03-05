package org.c4c.jaas.mysql;

import java.io.Serializable;
import java.security.Principal;

public class UserPrincipal implements Principal, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8856817248669408110L;

	private String _name;

	public UserPrincipal(String name) {
		this._name = name;
	}

	@Override
	public String getName() {

		return null;
	}

	@Override
	public int hashCode() {

		return _name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (this == obj)
			return true;

		if (!(obj instanceof UserPrincipal))
			return false;
		UserPrincipal that = (UserPrincipal) obj;

		if (this.getName().equals(that.getName()))
			return true;
		return false;
	}

}
