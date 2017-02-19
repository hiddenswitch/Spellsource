package net.demilich.metastone.gui.accounts;

/**
 * Created by bberman on 2/13/17.
 */
public class AccountsViewOptions {
	private boolean create;

	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	public AccountsViewOptions withCreate(boolean create) {
		this.create = create;
		return this;
	}
}
