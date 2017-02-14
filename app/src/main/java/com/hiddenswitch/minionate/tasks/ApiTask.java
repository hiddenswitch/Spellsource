package com.hiddenswitch.minionate.tasks;

import com.hiddenswitch.proto3.net.client.ApiClient;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.auth.ApiKeyAuth;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Created by bberman on 2/13/17.
 */
public class ApiTask<R> extends Task<R> {
	private String token;
	private R result;
	private ApiException error;
	private ApiRunnable<R> runnable;

	@FunctionalInterface
	public interface ApiRunnable<T> {
		T run(DefaultApi api) throws ApiException;
	}

	public ApiTask(ApiRunnable<R> runnable) {
		this.runnable = runnable;
	}

	public ApiTask(ApiRunnable<R> runnable, String token) {
		this.runnable = runnable;
		this.token = token;
	}

	public R getResult() {
		return result;
	}

	@Override
	protected R call() throws Exception {
		ApiClient client = Configuration.getDefaultApiClient();

		if (this.token != null) {
			ApiKeyAuth TokenSecurity = (ApiKeyAuth) client.getAuthentication("TokenSecurity");
			TokenSecurity.setApiKey(this.token);
		}

		DefaultApi api = new DefaultApi(client);

		try {
			result = runnable.run(api);
		} catch (ApiException e) {
			this.error = e;
			return null;
		}

		return result;
	}

	public String getToken() {
		return token;
	}

	public ApiException getError() {
		return error;
	}

	public ApiRunnable<R> getRunnable() {
		return runnable;
	}

	/**
	 * Execute and block the UI thread with a dialog window.
	 */
	public void blockingDialogExecute(String title, String message) {
		Alert dialog = new Alert(Alert.AlertType.INFORMATION,
				message,
				ButtonType.CANCEL);

		dialog.setTitle(title);
		dialog.setHeaderText(null);

		new Thread(this).start();

		this.setOnSucceeded(e -> {
			dialog.close();
		});

		this.setOnFailed(e -> {
			dialog.close();
		});

		dialog.showAndWait();
		this.cancel(true);
	}
}
