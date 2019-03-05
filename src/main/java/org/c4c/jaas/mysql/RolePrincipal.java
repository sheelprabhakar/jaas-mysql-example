package org.c4c.jaas.mysql;

import java.io.Serializable;
import java.security.Principal;

public class RolePrincipal implements Principal, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2624175522296838720L;
	private Roles _role;

	public RolePrincipal(Roles role) {
		this._role = role;
	}

	public Roles getRole() {
		return this._role;
	}

	@Override
	public String getName() {

		return this._role.name();
	}

	@Override
	public int hashCode() {
		return this._role.name().hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (this == obj)
			return true;

		if (!(obj instanceof RolePrincipal))
			return false;
		RolePrincipal that = (RolePrincipal) obj;

		if (this.getName().equals(that.getName()))
			return true;
		return false;
	}

}
