package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Accounts;
import io.vertx.await.Async;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.keycloak.representations.userprofile.config.UPConfig;

import java.util.HashMap;

import static io.vertx.await.Async.await;

public class V9_1__Change_Keycloak_registration extends BaseJavaMigration {
	public static final String USER_PROFILE_ATTRIBUTES_JSON = """
			{
			  "attributes": [
			    {
			      "name": "email",
			      "displayName": "${email}",
			      "required": {
			        "roles": [
			          "user"
			        ]
			      },
			      "permissions": {
			        "view": [
			          "admin",
			          "user"
			        ],
			        "edit": [
			          "admin",
			          "user"
			        ]
			      },
			      "validations": {
			        "email": {},
			        "length": {
			          "max": 255
			        }
			      }
			    },
			    {
			      "name": "username",
			      "displayName": "${username}",
			      "permissions": {
			        "view": [
			          "admin",
			          "user"
			        ],
			        "edit": [
			          "admin",
			          "user"
			        ]
			      },
			      "validations": {
			        "length": {
			          "min": 3,
			          "max": 255
			        },
			        "username-prohibited-characters": {},
			        "up-username-not-idn-homograph": {}
			      }
			    },
			    {
			      "name": "firstName",
			      "displayName": "${firstName}",
			      "permissions": {
			        "view": [
			          "admin",
			          "user"
			        ],
			        "edit": [
			          "admin",
			          "user"
			        ]
			      },
			      "validations": {
			        "length": {
			          "max": 255
			        },
			        "person-name-prohibited-characters": {}
			      }
			    },
			    {
			      "name": "lastName",
			      "displayName": "${lastName}",
			      "permissions": {
			        "view": [
			          "admin",
			          "user"
			        ],
			        "edit": [
			          "admin",
			          "user"
			        ]
			      },
			      "validations": {
			        "length": {
			          "max": 255
			        },
			        "person-name-prohibited-characters": {}
			      }
			    },
			    {
			      "name": "showPremadeDecks",
			      "displayName": "Starting Collection",
			      "selector": {
			        "scopes": [
			          "profile",
			          "roles"
			        ]
			      },
			      "permissions": {
			        "edit": [
			          "user",
			          "admin"
			        ],
			        "view": [
			          "admin",
			          "user"
			        ]
			      },
			      "annotations": {
			        "inputType": "select"
			      },
			      "validations": {
			        "options": {
			          "options": [
			            "With Premade Decks",
			            "Defaults"
			          ]
			        }
			      }
			    }
			  ]
			}
			""";

	public static UPConfig getUserProfileConfig() {
		try {
			var upConfig = DatabindCodec.mapper().readValue(USER_PROFILE_ATTRIBUTES_JSON, UPConfig.class);
			upConfig.setUnmanagedAttributePolicy(UPConfig.UnmanagedAttributePolicy.ENABLED);
			return upConfig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void migrate(Context context) throws Exception {
		var vertx = Vertx.vertx();
		try {
			Async.vt(vertx, () -> {
				var realm = await(Accounts.realm());
				var realmRepr = realm.toRepresentation();
				var attributes = realmRepr.getAttributes() != null
						? new HashMap<>(realmRepr.getAttributes())
						: new HashMap<String, String>();
				attributes.put("userProfileEnabled", "true");
				realmRepr.setAttributes(attributes);
				realm.update(realmRepr);
				realm.users().userProfile().update(getUserProfileConfig());
				return null;
			}).toCompletionStage().toCompletableFuture().join();
		} finally {
			vertx.close();
		}
	}
}
