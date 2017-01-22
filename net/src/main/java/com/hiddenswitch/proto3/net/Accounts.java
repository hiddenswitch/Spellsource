package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.amazon.LoginRequest;
import com.hiddenswitch.proto3.net.amazon.LoginResponse;

/**
 * Created by bberman on 12/8/16.
 */
public interface Accounts {
	@Suspendable
	CreateAccountResponse createAccount(CreateAccountRequest request);

	@Suspendable
	LoginResponse login(LoginRequest request);
}
