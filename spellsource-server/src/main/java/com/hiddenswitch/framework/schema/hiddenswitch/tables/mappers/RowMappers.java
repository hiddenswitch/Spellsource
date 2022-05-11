package com.hiddenswitch.framework.schema.hiddenswitch.tables.mappers;

import io.vertx.sqlclient.Row;
import java.util.function.Function;

public class RowMappers {

        private RowMappers(){}

        public static Function<Row,com.hiddenswitch.framework.schema.hiddenswitch.tables.pojos.FlywaySchemaHistory> getFlywaySchemaHistoryMapper() {
                return row -> {
                        com.hiddenswitch.framework.schema.hiddenswitch.tables.pojos.FlywaySchemaHistory pojo = new com.hiddenswitch.framework.schema.hiddenswitch.tables.pojos.FlywaySchemaHistory();
                        pojo.setInstalledRank(row.getInteger("installed_rank"));
                        pojo.setVersion(row.getString("version"));
                        pojo.setDescription(row.getString("description"));
                        pojo.setType(row.getString("type"));
                        pojo.setScript(row.getString("script"));
                        pojo.setChecksum(row.getInteger("checksum"));
                        pojo.setInstalledBy(row.getString("installed_by"));
                        pojo.setInstalledOn(row.getLocalDateTime("installed_on"));
                        pojo.setExecutionTime(row.getInteger("execution_time"));
                        pojo.setSuccess(row.getBoolean("success"));
                        return pojo;
                };
        }

}
