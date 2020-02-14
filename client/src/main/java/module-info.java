// TODO: Use dynamically generated module names
module spellsource.client {
    requires jersey.client;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.jaxrs.json;
    requires jersey.multipart;
    requires swagger.annotations;
    requires java.ws.rs;
    exports com.hiddenswitch.spellsource.client;
    exports com.hiddenswitch.spellsource.client.api;
    exports com.hiddenswitch.spellsource.client.models;
    exports com.hiddenswitch.spellsource.client.auth;
    opens com.hiddenswitch.spellsource.client.models to com.fasterxml.jackson.databind;
}