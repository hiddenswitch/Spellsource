package com.hiddenswitch.spellsource;

import java.io.InputStream;

public interface CardResource {

	String getFileName();

	InputStream getInputStream();
}
