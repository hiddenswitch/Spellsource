package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.rpc.Hiddenswitch;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.util.zip.CRC32;

/**
 * This migration is run whenever the protobuf definitions change. Use it to expire caches in Redis that use those
 * types.
 */
public class R__0004_Expire_caches_on_protos_change extends BaseJavaMigration {
	@Override
	public Integer getChecksum() {
		var checksum = new CRC32();
		checksum.update(Spellsource.getDescriptor().toProto().toByteArray());
		checksum.update(Hiddenswitch.getDescriptor().toProto().toByteArray());
		return (int) checksum.getValue();
	}

	@Override
	public void migrate(Context context) throws Exception {

	}
}
