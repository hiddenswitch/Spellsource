import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;
import java.util.Base64;


public class SshTunnelService implements BuildService<SshTunnelService.Params>, AutoCloseable {

	private final Session session;

	@Override
	@Inject
	public Params getParameters() {
		return null;
	}

	public interface Params extends BuildServiceParameters {
		Property<String> getUsername();

		RegularFileProperty getIdentity();

		Property<String> getPassword();

		Property<String> getHost();

		Property<String> getHostKey();

		Property<String> getRemoteHost();

		Property<Integer> getRemotePort();

		Property<Integer> getLocalPort();

		Property<Integer> getSshPort();

		Property<Long> getConnectionTimeout();
	}

	public SshTunnelService() throws JSchException {
		JSch jSch = new JSch();
		var params = getParameters();

		if (params.getIdentity().isPresent()) {
			jSch.addIdentity(params.getIdentity().getAsFile().get().getAbsolutePath());
		}

		if (params.getHostKey().isPresent()
				&& params.getHost().isPresent()) {
			jSch.getHostKeyRepository().add(new HostKey(params.getHost().get(), Base64.getDecoder().decode(params.getHostKey().get())), null);
		}

		session = jSch.getSession(params.getUsername().get(), params.getHost().get(), params.getSshPort().getOrElse(22));
		var password = params.getPassword().getOrElse("");
		if (params.getPassword().isPresent()) {
			session.setPassword(password);
		}

		session.connect(params.getConnectionTimeout().getOrElse(1000L).intValue());
		session.setPortForwardingL(params.getLocalPort().get(), params.getRemoteHost().get(), params.getRemotePort().get());
	}

	public Session getSession() {
		return session;
	}

	@Override
	public void close() throws Exception {
		session.disconnect();
	}
}
