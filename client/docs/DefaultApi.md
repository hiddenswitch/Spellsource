# DefaultApi

All URIs are relative to *https://api-3.hiddenswitch.com*

Method | HTTP request | Description
------------- | ------------- | -------------
[**acceptInvite**](DefaultApi.md#acceptInvite) | **POST** /invites/{inviteId} | 
[**changePassword**](DefaultApi.md#changePassword) | **POST** /accounts-password | 
[**createAccount**](DefaultApi.md#createAccount) | **PUT** /accounts | 
[**decksDelete**](DefaultApi.md#decksDelete) | **DELETE** /decks/{deckId} | 
[**decksGet**](DefaultApi.md#decksGet) | **GET** /decks/{deckId} | 
[**decksGetAll**](DefaultApi.md#decksGetAll) | **GET** /decks | 
[**decksPut**](DefaultApi.md#decksPut) | **PUT** /decks | 
[**decksUpdate**](DefaultApi.md#decksUpdate) | **POST** /decks/{deckId} | 
[**deleteInvite**](DefaultApi.md#deleteInvite) | **DELETE** /invites/{inviteId} | 
[**draftsChooseCard**](DefaultApi.md#draftsChooseCard) | **PUT** /drafts/cards | 
[**draftsChooseHero**](DefaultApi.md#draftsChooseHero) | **PUT** /drafts/hero | 
[**draftsGet**](DefaultApi.md#draftsGet) | **GET** /drafts | 
[**draftsPost**](DefaultApi.md#draftsPost) | **POST** /drafts | 
[**friendDelete**](DefaultApi.md#friendDelete) | **DELETE** /friends/{friendId} | 
[**friendPut**](DefaultApi.md#friendPut) | **PUT** /friends | 
[**getAccount**](DefaultApi.md#getAccount) | **GET** /accounts/{targetUserId} | 
[**getAccounts**](DefaultApi.md#getAccounts) | **GET** /accounts | 
[**getCards**](DefaultApi.md#getCards) | **GET** /cards | 
[**getGameRecord**](DefaultApi.md#getGameRecord) | **GET** /games/{gameId} | 
[**getGameRecordIds**](DefaultApi.md#getGameRecordIds) | **GET** /games | 
[**getInvite**](DefaultApi.md#getInvite) | **GET** /invites/{inviteId} | 
[**getInvites**](DefaultApi.md#getInvites) | **GET** /invites | 
[**healthCheck**](DefaultApi.md#healthCheck) | **GET** / | 
[**login**](DefaultApi.md#login) | **POST** /accounts | 
[**matchmakingDelete**](DefaultApi.md#matchmakingDelete) | **DELETE** /matchmaking | 
[**matchmakingGet**](DefaultApi.md#matchmakingGet) | **GET** /matchmaking | 
[**postInvite**](DefaultApi.md#postInvite) | **POST** /invites | 


<a name="acceptInvite"></a>
# **acceptInvite**
> AcceptInviteResponse acceptInvite(inviteId, request)



Accepts the invite. If this is an invite to friend the user, this method will perform the friending path for you. If this is an invite to play a match and a matchmaking queue put is specified (with the deck ID), this method will enter you into the special invite matchmaking queue. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

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
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **inviteId** | **String**|  |
 **request** | [**AcceptInviteRequest**](AcceptInviteRequest.md)|  |

### Return type

[**AcceptInviteResponse**](AcceptInviteResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="changePassword"></a>
# **changePassword**
> ChangePasswordResponse changePassword(request)



Changes your password. Does not log you out after the password is changed. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
ChangePasswordRequest request = new ChangePasswordRequest(); // ChangePasswordRequest | 
try {
    ChangePasswordResponse result = apiInstance.changePassword(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#changePassword");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**ChangePasswordRequest**](ChangePasswordRequest.md)|  |

### Return type

[**ChangePasswordResponse**](ChangePasswordResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="createAccount"></a>
# **createAccount**
> CreateAccountResponse createAccount(request)



Create an account with Spellsource. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
CreateAccountRequest request = new CreateAccountRequest(); // CreateAccountRequest | 
try {
    CreateAccountResponse result = apiInstance.createAccount(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#createAccount");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**CreateAccountRequest**](CreateAccountRequest.md)|  |

### Return type

[**CreateAccountResponse**](CreateAccountResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="decksDelete"></a>
# **decksDelete**
> decksDelete(deckId)



Deletes the specified deck by ID. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String deckId = "deckId_example"; // String | The Deck ID to delete.
try {
    apiInstance.decksDelete(deckId);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#decksDelete");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deckId** | **String**| The Deck ID to delete. |

### Return type

null (empty response body)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="decksGet"></a>
# **decksGet**
> DecksGetResponse decksGet(deckId)



Gets a deck. Only viewable for the owner of the deck or players in the alliance. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String deckId = "deckId_example"; // String | The Deck ID to get.
try {
    DecksGetResponse result = apiInstance.decksGet(deckId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#decksGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deckId** | **String**| The Deck ID to get. |

### Return type

[**DecksGetResponse**](DecksGetResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="decksGetAll"></a>
# **decksGetAll**
> DecksGetAllResponse decksGetAll()



Gets all the user&#39;s decks. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    DecksGetAllResponse result = apiInstance.decksGetAll();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#decksGetAll");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**DecksGetAllResponse**](DecksGetAllResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="decksPut"></a>
# **decksPut**
> DecksPutResponse decksPut(request)



Creates a new deck with optionally specified inventory IDs, a name and a hero class. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
DecksPutRequest request = new DecksPutRequest(); // DecksPutRequest | The deck creation request. 
try {
    DecksPutResponse result = apiInstance.decksPut(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#decksPut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**DecksPutRequest**](DecksPutRequest.md)| The deck creation request.  |

### Return type

[**DecksPutResponse**](DecksPutResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="decksUpdate"></a>
# **decksUpdate**
> DecksGetResponse decksUpdate(deckId, updateCommand)



Updates the deck by adding or removing cards, changing the hero class, or renaming the deck. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String deckId = "deckId_example"; // String | The Deck ID to update.
DecksUpdateCommand updateCommand = new DecksUpdateCommand(); // DecksUpdateCommand | An update command modifying specified properties of the deck. 
try {
    DecksGetResponse result = apiInstance.decksUpdate(deckId, updateCommand);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#decksUpdate");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deckId** | **String**| The Deck ID to update. |
 **updateCommand** | [**DecksUpdateCommand**](DecksUpdateCommand.md)| An update command modifying specified properties of the deck.  |

### Return type

[**DecksGetResponse**](DecksGetResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="deleteInvite"></a>
# **deleteInvite**
> InviteResponse deleteInvite(inviteId)



When this user is the sender, cancels the invite. When this user is the recipient, rejects the specified invite. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String inviteId = "inviteId_example"; // String | 
try {
    InviteResponse result = apiInstance.deleteInvite(inviteId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#deleteInvite");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **inviteId** | **String**|  |

### Return type

[**InviteResponse**](InviteResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="draftsChooseCard"></a>
# **draftsChooseCard**
> DraftState draftsChooseCard(request)



Make a selection for the given draft index. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
DraftsChooseCardRequest request = new DraftsChooseCardRequest(); // DraftsChooseCardRequest | 
try {
    DraftState result = apiInstance.draftsChooseCard(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#draftsChooseCard");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**DraftsChooseCardRequest**](DraftsChooseCardRequest.md)|  |

### Return type

[**DraftState**](DraftState.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="draftsChooseHero"></a>
# **draftsChooseHero**
> DraftState draftsChooseHero(request)



Choose a hero from your hero selection. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
DraftsChooseHeroRequest request = new DraftsChooseHeroRequest(); // DraftsChooseHeroRequest | 
try {
    DraftState result = apiInstance.draftsChooseHero(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#draftsChooseHero");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**DraftsChooseHeroRequest**](DraftsChooseHeroRequest.md)|  |

### Return type

[**DraftState**](DraftState.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="draftsGet"></a>
# **draftsGet**
> DraftState draftsGet()



Gets your latest state of the draft. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    DraftState result = apiInstance.draftsGet();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#draftsGet");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**DraftState**](DraftState.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="draftsPost"></a>
# **draftsPost**
> DraftState draftsPost(request)



Starts a draft, or make a change to your draft, like retiring early. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
DraftsPostRequest request = new DraftsPostRequest(); // DraftsPostRequest | 
try {
    DraftState result = apiInstance.draftsPost(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#draftsPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**DraftsPostRequest**](DraftsPostRequest.md)|  |

### Return type

[**DraftState**](DraftState.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="friendDelete"></a>
# **friendDelete**
> UnfriendResponse friendDelete(friendId)



Removes the friend relationship between two users. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String friendId = "friendId_example"; // String | id of friend to unfriend.
try {
    UnfriendResponse result = apiInstance.friendDelete(friendId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#friendDelete");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **friendId** | **String**| id of friend to unfriend. |

### Return type

[**UnfriendResponse**](UnfriendResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="friendPut"></a>
# **friendPut**
> FriendPutResponse friendPut(request)



Adds a specified user to your friend list. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
FriendPutRequest request = new FriendPutRequest(); // FriendPutRequest | 
try {
    FriendPutResponse result = apiInstance.friendPut(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#friendPut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**FriendPutRequest**](FriendPutRequest.md)|  |

### Return type

[**FriendPutResponse**](FriendPutResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getAccount"></a>
# **getAccount**
> GetAccountsResponse getAccount(targetUserId)



Get a specific account. Contains more information if the userId matches the requesting user. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String targetUserId = "targetUserId_example"; // String | 
try {
    GetAccountsResponse result = apiInstance.getAccount(targetUserId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getAccount");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **targetUserId** | **String**|  |

### Return type

[**GetAccountsResponse**](GetAccountsResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getAccounts"></a>
# **getAccounts**
> GetAccountsResponse getAccounts(request)



Get a list of accounts including user profile information. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
GetAccountsRequest request = new GetAccountsRequest(); // GetAccountsRequest | 
try {
    GetAccountsResponse result = apiInstance.getAccounts(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getAccounts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**GetAccountsRequest**](GetAccountsRequest.md)|  |

### Return type

[**GetAccountsResponse**](GetAccountsResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getCards"></a>
# **getCards**
> GetCardsResponse getCards(ifNoneMatch)



Gets a complete catalogue of all the cards available in Spellsource as a list of CardRecords 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
String ifNoneMatch = "ifNoneMatch_example"; // String | The value returned in the ETag header from the server when this was last called, or empty if this is the first call to this resource. 
try {
    GetCardsResponse result = apiInstance.getCards(ifNoneMatch);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getCards");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ifNoneMatch** | **String**| The value returned in the ETag header from the server when this was last called, or empty if this is the first call to this resource.  | [optional]

### Return type

[**GetCardsResponse**](GetCardsResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getGameRecord"></a>
# **getGameRecord**
> GetGameRecordResponse getGameRecord(gameId)



Retrieves a record of a game this player played. Games against bots retrieve a complete game record, while games against other players only receive this player&#39;s point of view. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String gameId = "gameId_example"; // String | 
try {
    GetGameRecordResponse result = apiInstance.getGameRecord(gameId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getGameRecord");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **gameId** | **String**|  |

### Return type

[**GetGameRecordResponse**](GetGameRecordResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getGameRecordIds"></a>
# **getGameRecordIds**
> GetGameRecordIdsResponse getGameRecordIds()



Retrieves a list of game IDs corresponding to all the games this player played. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    GetGameRecordIdsResponse result = apiInstance.getGameRecordIds();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getGameRecordIds");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**GetGameRecordIdsResponse**](GetGameRecordIdsResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getInvite"></a>
# **getInvite**
> InviteResponse getInvite(inviteId)



Retrieves information about a specific invite, as long as this user is either the sender or recipient. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
String inviteId = "inviteId_example"; // String | 
try {
    InviteResponse result = apiInstance.getInvite(inviteId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getInvite");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **inviteId** | **String**|  |

### Return type

[**InviteResponse**](InviteResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getInvites"></a>
# **getInvites**
> InviteGetResponse getInvites()



Retrieve all invites where this user is either the sender or recipient. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    InviteGetResponse result = apiInstance.getInvites();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getInvites");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**InviteGetResponse**](InviteGetResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="healthCheck"></a>
# **healthCheck**
> healthCheck()



Returns an empty body if the server is available. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
try {
    apiInstance.healthCheck();
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#healthCheck");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="login"></a>
# **login**
> LoginResponse login(request)



Login with a username and password, receiving an authentication token to use for future sessions. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
LoginRequest request = new LoginRequest(); // LoginRequest | 
try {
    LoginResponse result = apiInstance.login(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#login");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**LoginRequest**](LoginRequest.md)|  |

### Return type

[**LoginResponse**](LoginResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="matchmakingDelete"></a>
# **matchmakingDelete**
> MatchCancelResponse matchmakingDelete()



Removes your client from the matchmaking queue, regardless of which queue it is in.

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    MatchCancelResponse result = apiInstance.matchmakingDelete();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#matchmakingDelete");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**MatchCancelResponse**](MatchCancelResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="matchmakingGet"></a>
# **matchmakingGet**
> MatchmakingQueuesResponse matchmakingGet()



Gets a list of queues available for matchmaking. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    MatchmakingQueuesResponse result = apiInstance.matchmakingGet();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#matchmakingGet");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**MatchmakingQueuesResponse**](MatchmakingQueuesResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="postInvite"></a>
# **postInvite**
> InviteResponse postInvite(request)



Send an invite. 

### Example
```java
// Import classes:
//import com.hiddenswitch.spellsource.client.ApiClient;
//import com.hiddenswitch.spellsource.client.ApiException;
//import com.hiddenswitch.spellsource.client.Configuration;
//import com.hiddenswitch.spellsource.client.auth.*;
//import com.hiddenswitch.spellsource.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
InvitePostRequest request = new InvitePostRequest(); // InvitePostRequest | 
try {
    InviteResponse result = apiInstance.postInvite(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#postInvite");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**InvitePostRequest**](InvitePostRequest.md)|  |

### Return type

[**InviteResponse**](InviteResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

