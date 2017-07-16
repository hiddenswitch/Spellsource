package com.hiddenswitch.spellsource.util;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;

public class ClientConnectionConfigurationSerializer extends ObjectSerializer<ClientConnectionConfiguration> implements JsonSerializer<ClientConnectionConfiguration>, JsonDeserializer<ClientConnectionConfiguration> {
}
