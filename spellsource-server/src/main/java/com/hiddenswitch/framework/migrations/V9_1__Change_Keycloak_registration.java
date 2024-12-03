package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.schema.keycloak.Keycloak;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import io.vertx.await.Async;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

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

	@Override
	public void migrate(Context context) throws Exception {
		var vertx = Vertx.vertx();
		var latch = new CountDownLatch(1);
		Async.vt(vertx, () -> {
			var realm = await(Accounts.realm());
			var realmRepr = realm.toRepresentation();
			var attributes = new HashMap<>(realmRepr.getAttributes());
			attributes.put("userProfileEnabled", "true");
			realmRepr.setAttributes(attributes);
			realm.update(realmRepr);
			realm.users().userProfile().update(USER_PROFILE_ATTRIBUTES_JSON);
			latch.countDown();
			return null;
		});
		latch.await();
		vertx.close();
	}
}
