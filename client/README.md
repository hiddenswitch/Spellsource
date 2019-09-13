# spellsource-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>com.hiddenswitch</groupId>
    <artifactId>spellsource-client</artifactId>
    <version>0.8.7</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "com.hiddenswitch:spellsource-client:0.8.7"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/spellsource-client-0.8.7.jar
* target/lib/*.jar

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java

import com.hiddenswitch.spellsource.client.*;
import com.hiddenswitch.spellsource.client.auth.*;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.api.DefaultApi;

import java.io.File;
import java.util.*;

public class DefaultApiExample {

    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        
        // Configure API key authorization: TokenSecurity
        ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
        TokenSecurity.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //TokenSecurity.setApiKeyPrefix("Token");

        DefaultApi apiInstance = new DefaultApi();
        String inviteId = "inviteId_example"; // String | 
        AcceptInviteRequest request = new AcceptInviteRequest(); // AcceptInviteRequest | 
        try {
            AcceptInviteResponse result = apiInstance.acceptInvite(inviteId, request);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#acceptInvite");
            e.printStackTrace();
        }
    }
}

```

## Documentation for API Endpoints

All URIs are relative to *https://api.hiddenswitch.com/api/v3*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*DefaultApi* | [**acceptInvite**](docs/DefaultApi.md#acceptInvite) | **POST** /invites/{inviteId} | 
*DefaultApi* | [**changePassword**](docs/DefaultApi.md#changePassword) | **POST** /accounts-password | 
*DefaultApi* | [**createAccount**](docs/DefaultApi.md#createAccount) | **PUT** /accounts | 
*DefaultApi* | [**decksDelete**](docs/DefaultApi.md#decksDelete) | **DELETE** /decks/{deckId} | 
*DefaultApi* | [**decksGet**](docs/DefaultApi.md#decksGet) | **GET** /decks/{deckId} | 
*DefaultApi* | [**decksGetAll**](docs/DefaultApi.md#decksGetAll) | **GET** /decks | 
*DefaultApi* | [**decksPut**](docs/DefaultApi.md#decksPut) | **PUT** /decks | 
*DefaultApi* | [**decksUpdate**](docs/DefaultApi.md#decksUpdate) | **POST** /decks/{deckId} | 
*DefaultApi* | [**deleteInvite**](docs/DefaultApi.md#deleteInvite) | **DELETE** /invites/{inviteId} | 
*DefaultApi* | [**draftsChooseCard**](docs/DefaultApi.md#draftsChooseCard) | **PUT** /drafts/cards | 
*DefaultApi* | [**draftsChooseHero**](docs/DefaultApi.md#draftsChooseHero) | **PUT** /drafts/hero | 
*DefaultApi* | [**draftsGet**](docs/DefaultApi.md#draftsGet) | **GET** /drafts | 
*DefaultApi* | [**draftsPost**](docs/DefaultApi.md#draftsPost) | **POST** /drafts | 
*DefaultApi* | [**friendDelete**](docs/DefaultApi.md#friendDelete) | **DELETE** /friends/{friendId} | 
*DefaultApi* | [**friendPut**](docs/DefaultApi.md#friendPut) | **PUT** /friends | 
*DefaultApi* | [**getAccount**](docs/DefaultApi.md#getAccount) | **GET** /accounts/{targetUserId} | 
*DefaultApi* | [**getAccounts**](docs/DefaultApi.md#getAccounts) | **GET** /accounts | 
*DefaultApi* | [**getCards**](docs/DefaultApi.md#getCards) | **GET** /cards | 
*DefaultApi* | [**getGameRecord**](docs/DefaultApi.md#getGameRecord) | **GET** /games/{gameId} | 
*DefaultApi* | [**getGameRecordIds**](docs/DefaultApi.md#getGameRecordIds) | **GET** /games | 
*DefaultApi* | [**getInvite**](docs/DefaultApi.md#getInvite) | **GET** /invites/{inviteId} | 
*DefaultApi* | [**getInvites**](docs/DefaultApi.md#getInvites) | **GET** /invites | 
*DefaultApi* | [**getVersion**](docs/DefaultApi.md#getVersion) | **GET** /version | 
*DefaultApi* | [**healthCheck**](docs/DefaultApi.md#healthCheck) | **GET** / | 
*DefaultApi* | [**login**](docs/DefaultApi.md#login) | **POST** /accounts | 
*DefaultApi* | [**matchmakingDelete**](docs/DefaultApi.md#matchmakingDelete) | **DELETE** /matchmaking | 
*DefaultApi* | [**matchmakingGet**](docs/DefaultApi.md#matchmakingGet) | **GET** /matchmaking | 
*DefaultApi* | [**postInvite**](docs/DefaultApi.md#postInvite) | **POST** /invites | 


## Documentation for Models

 - [AcceptInviteRequest](docs/AcceptInviteRequest.md)
 - [AcceptInviteResponse](docs/AcceptInviteResponse.md)
 - [Account](docs/Account.md)
 - [CardEvent](docs/CardEvent.md)
 - [CardRecord](docs/CardRecord.md)
 - [ChangePasswordRequest](docs/ChangePasswordRequest.md)
 - [ChangePasswordResponse](docs/ChangePasswordResponse.md)
 - [ChatMessage](docs/ChatMessage.md)
 - [ChooseOneOptions](docs/ChooseOneOptions.md)
 - [ClientToServerMessage](docs/ClientToServerMessage.md)
 - [ClientToServerMessageFirstMessage](docs/ClientToServerMessageFirstMessage.md)
 - [CreateAccountRequest](docs/CreateAccountRequest.md)
 - [CreateAccountResponse](docs/CreateAccountResponse.md)
 - [DamageTypeEnum](docs/DamageTypeEnum.md)
 - [DecksGetAllResponse](docs/DecksGetAllResponse.md)
 - [DecksGetResponse](docs/DecksGetResponse.md)
 - [DecksPutRequest](docs/DecksPutRequest.md)
 - [DecksPutResponse](docs/DecksPutResponse.md)
 - [DecksUpdateCommand](docs/DecksUpdateCommand.md)
 - [DecksUpdateCommandPushCardIds](docs/DecksUpdateCommandPushCardIds.md)
 - [DecksUpdateCommandPushInventoryIds](docs/DecksUpdateCommandPushInventoryIds.md)
 - [DefaultMethodResponse](docs/DefaultMethodResponse.md)
 - [DraftState](docs/DraftState.md)
 - [DraftsChooseCardRequest](docs/DraftsChooseCardRequest.md)
 - [DraftsChooseHeroRequest](docs/DraftsChooseHeroRequest.md)
 - [DraftsPostRequest](docs/DraftsPostRequest.md)
 - [Emote](docs/Emote.md)
 - [Enchantment](docs/Enchantment.md)
 - [Entity](docs/Entity.md)
 - [EntityChangeSet](docs/EntityChangeSet.md)
 - [EntityLocation](docs/EntityLocation.md)
 - [Envelope](docs/Envelope.md)
 - [EnvelopeAdded](docs/EnvelopeAdded.md)
 - [EnvelopeChanged](docs/EnvelopeChanged.md)
 - [EnvelopeGame](docs/EnvelopeGame.md)
 - [EnvelopeMethod](docs/EnvelopeMethod.md)
 - [EnvelopeMethodDequeue](docs/EnvelopeMethodDequeue.md)
 - [EnvelopeMethodSendMessage](docs/EnvelopeMethodSendMessage.md)
 - [EnvelopeRemoved](docs/EnvelopeRemoved.md)
 - [EnvelopeRequest](docs/EnvelopeRequest.md)
 - [EnvelopeResponse](docs/EnvelopeResponse.md)
 - [EnvelopeResult](docs/EnvelopeResult.md)
 - [EnvelopeResultSendMessage](docs/EnvelopeResultSendMessage.md)
 - [EnvelopeSub](docs/EnvelopeSub.md)
 - [EnvelopeSubConversation](docs/EnvelopeSubConversation.md)
 - [Friend](docs/Friend.md)
 - [FriendPutRequest](docs/FriendPutRequest.md)
 - [FriendPutResponse](docs/FriendPutResponse.md)
 - [GameActions](docs/GameActions.md)
 - [GameActionsDiscoveries](docs/GameActionsDiscoveries.md)
 - [GameActionsPhysicalAttacks](docs/GameActionsPhysicalAttacks.md)
 - [GameEvent](docs/GameEvent.md)
 - [GameEventAfterSpellCasted](docs/GameEventAfterSpellCasted.md)
 - [GameEventArmorGained](docs/GameEventArmorGained.md)
 - [GameEventBeforeSummon](docs/GameEventBeforeSummon.md)
 - [GameEventDamage](docs/GameEventDamage.md)
 - [GameEventFatigue](docs/GameEventFatigue.md)
 - [GameEventHeal](docs/GameEventHeal.md)
 - [GameEventHeroPowerUsed](docs/GameEventHeroPowerUsed.md)
 - [GameEventJoust](docs/GameEventJoust.md)
 - [GameEventKill](docs/GameEventKill.md)
 - [GameEventOverload](docs/GameEventOverload.md)
 - [GameEventPerformedGameAction](docs/GameEventPerformedGameAction.md)
 - [GameEventPreDamage](docs/GameEventPreDamage.md)
 - [GameEventQuestSuccessful](docs/GameEventQuestSuccessful.md)
 - [GameEventSecretPlayed](docs/GameEventSecretPlayed.md)
 - [GameEventSecretRevealed](docs/GameEventSecretRevealed.md)
 - [GameEventSilence](docs/GameEventSilence.md)
 - [GameEventTargetAcquisition](docs/GameEventTargetAcquisition.md)
 - [GameEventTriggerFired](docs/GameEventTriggerFired.md)
 - [GameEventWeaponDestroyed](docs/GameEventWeaponDestroyed.md)
 - [GameOver](docs/GameOver.md)
 - [GameState](docs/GameState.md)
 - [GetAccountsRequest](docs/GetAccountsRequest.md)
 - [GetAccountsResponse](docs/GetAccountsResponse.md)
 - [GetCardsResponse](docs/GetCardsResponse.md)
 - [GetGameRecordIdsResponse](docs/GetGameRecordIdsResponse.md)
 - [GetGameRecordResponse](docs/GetGameRecordResponse.md)
 - [InventoryCollection](docs/InventoryCollection.md)
 - [Invite](docs/Invite.md)
 - [InviteGetResponse](docs/InviteGetResponse.md)
 - [InvitePostRequest](docs/InvitePostRequest.md)
 - [InviteResponse](docs/InviteResponse.md)
 - [LoginRequest](docs/LoginRequest.md)
 - [LoginResponse](docs/LoginResponse.md)
 - [Match](docs/Match.md)
 - [MatchCancelResponse](docs/MatchCancelResponse.md)
 - [MatchConcedeResponse](docs/MatchConcedeResponse.md)
 - [MatchmakingQueueItem](docs/MatchmakingQueueItem.md)
 - [MatchmakingQueueItemRequires](docs/MatchmakingQueueItemRequires.md)
 - [MatchmakingQueuePutRequest](docs/MatchmakingQueuePutRequest.md)
 - [MatchmakingQueuePutResponse](docs/MatchmakingQueuePutResponse.md)
 - [MatchmakingQueuePutResponseUnityConnection](docs/MatchmakingQueuePutResponseUnityConnection.md)
 - [MatchmakingQueuesResponse](docs/MatchmakingQueuesResponse.md)
 - [MessageType](docs/MessageType.md)
 - [PhysicalAttackEvent](docs/PhysicalAttackEvent.md)
 - [PresenceEnum](docs/PresenceEnum.md)
 - [Replay](docs/Replay.md)
 - [ReplayDeltas](docs/ReplayDeltas.md)
 - [ReplayGameStates](docs/ReplayGameStates.md)
 - [ServerToClientMessage](docs/ServerToClientMessage.md)
 - [SpanContext](docs/SpanContext.md)
 - [SpellAction](docs/SpellAction.md)
 - [SpellsourceException](docs/SpellsourceException.md)
 - [SummonAction](docs/SummonAction.md)
 - [SummonActionIndexToActions](docs/SummonActionIndexToActions.md)
 - [TargetActionPair](docs/TargetActionPair.md)
 - [Timers](docs/Timers.md)
 - [UnfriendResponse](docs/UnfriendResponse.md)


## Documentation for Authorization

Authentication schemes defined for the API:
### TokenSecurity

- **Type**: API key
- **API key parameter name**: X-Auth-Token
- **Location**: HTTP header


## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

## Author

ben@hiddenswitch.com

