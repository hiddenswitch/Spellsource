package com.hiddenswitch.spellsource;

import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public abstract class AbstractCardResources<T> implements CardResources {
	protected final Logger LOGGER;
	private final Class<T> thisClass;
	private AtomicBoolean isLoaded = new AtomicBoolean();
	private List<ResourceInputStream> resources;

	protected AbstractCardResources(Class<T> thisClass) {
		this.thisClass = thisClass;
		LOGGER = LoggerFactory.getLogger(thisClass);
	}

	@Override
	public void load() {
		if (!isLoaded.compareAndSet(false, true)) {
			return;
		}
		try {
			ClassLoader classLoader = thisClass.getClassLoader();
			resources = ClassPath.from(classLoader)
					.getResources()
					.stream()
					.filter(resource -> resource.getResourceName().startsWith(getDirectoryPrefix()) && resource.getResourceName().endsWith(".json"))
					.map(resource -> new ResourceInputStream(resource.getResourceName(), CardResources.getInputStream(classLoader, true, resource.getResourceName()), false))
					.collect(Collectors.toList());
			LOGGER.debug("load {}: {} cards", getDirectoryPrefix(), resources.size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDirectoryPrefix() {
		return "cards";
	}

	@Override
	public List<ResourceInputStream> getResources() {
		load();
		return resources;
	}

}
