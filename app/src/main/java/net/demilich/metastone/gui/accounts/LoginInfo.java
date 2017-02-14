package net.demilich.metastone.gui.accounts;

/**
 * Created by bberman on 2/13/17.
 */
public class LoginInfo {
	private String token;
	private boolean create;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public LoginInfo withToken(String token) {
		this.token = token;
		return this;
	}

	public LoginInfo withCreate(boolean create) {
		this.create = create;
		return this;
	}
}
