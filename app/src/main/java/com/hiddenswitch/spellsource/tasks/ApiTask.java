package com.hiddenswitch.spellsource.tasks;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Created by bberman on 2/13/17.
 */
public class ApiTask<R> extends Task<R> {
	private ApiException error;
	private ApiRequestResponse<R> runnable;

	@FunctionalInterface
	public interface ApiRequestResponse<T> {
		T run(DefaultApi api) throws ApiException;
	}

	public ApiTask(ApiRequestResponse<R> runnable) {
		this.runnable = runnable;
	}

	@Override
	protected R call() throws Exception {
		DefaultApi api = new DefaultApi();

		R result;
		try {
			result = runnable.run(api);
		} catch (ApiException e) {
			this.error = e;
			return null;
		}

		return result;
	}

	public ApiException getError() {
		return error;
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

		this.setOnSucceeded(e -> {
			dialog.close();
		});

		this.setOnFailed(e -> {
			dialog.close();
		});

		new Thread(this).start();

		dialog.showAndWait();
	}
}
