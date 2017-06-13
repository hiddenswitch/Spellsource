# DefaultApi

All URIs are relative to *http://api-us-east-2.hiddenswitch.com:8080/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createAccount**](DefaultApi.md#createAccount) | **PUT** /accounts | 
[**decksDelete**](DefaultApi.md#decksDelete) | **DELETE** /decks/{deckId} | 
[**decksGet**](DefaultApi.md#decksGet) | **GET** /decks/{deckId} | 
[**decksGetAll**](DefaultApi.md#decksGetAll) | **GET** /decks | 
[**decksPut**](DefaultApi.md#decksPut) | **PUT** /decks | 
[**decksUpdate**](DefaultApi.md#decksUpdate) | **POST** /decks/{deckId} | 
[**friendDelete**](DefaultApi.md#friendDelete) | **DELETE** /friends/{friendId} | 
[**friendPut**](DefaultApi.md#friendPut) | **PUT** /friends | 
[**getAccount**](DefaultApi.md#getAccount) | **GET** /accounts/{targetUserId} | 
[**getAccounts**](DefaultApi.md#getAccounts) | **GET** /accounts | 
[**login**](DefaultApi.md#login) | **POST** /accounts | 
[**matchmakingConstructedDelete**](DefaultApi.md#matchmakingConstructedDelete) | **DELETE** /matchmaking/constructed | 
[**matchmakingConstructedGet**](DefaultApi.md#matchmakingConstructedGet) | **GET** /matchmaking/constructed | 
[**matchmakingConstructedQueueDelete**](DefaultApi.md#matchmakingConstructedQueueDelete) | **DELETE** /matchmaking/constructed/queue | 
[**matchmakingConstructedQueuePut**](DefaultApi.md#matchmakingConstructedQueuePut) | **PUT** /matchmaking/constructed/queue | 


<a name="createAccount"></a>
# **createAccount**
> CreateAccountResponse createAccount(request)



Create an account with Minionate. 

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;


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
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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

<a name="friendDelete"></a>
# **friendDelete**
> UnfriendResponse friendDelete(friendId)



unfriend a user 

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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



connect with a friend 

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
FriendPutRequest request = new FriendPutRequest(); // FriendPutRequest | Friend put request 
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
 **request** | [**FriendPutRequest**](FriendPutRequest.md)| Friend put request  |

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
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

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

<a name="login"></a>
# **login**
> LoginResponse login(request)



Login with a username and password, receiving an authentication token to use for future sessions. 

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;


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

<a name="matchmakingConstructedDelete"></a>
# **matchmakingConstructedDelete**
> MatchConcedeResponse matchmakingConstructedDelete()



Concedes the player&#39;s current constructed game. 

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    MatchConcedeResponse result = apiInstance.matchmakingConstructedDelete();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#matchmakingConstructedDelete");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**MatchConcedeResponse**](MatchConcedeResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="matchmakingConstructedGet"></a>
# **matchmakingConstructedGet**
> GameState matchmakingConstructedGet()



Gets a renderable gamestate representing this player&#39;s current game. 

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    GameState result = apiInstance.matchmakingConstructedGet();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#matchmakingConstructedGet");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**GameState**](GameState.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="matchmakingConstructedQueueDelete"></a>
# **matchmakingConstructedQueueDelete**
> MatchCancelResponse matchmakingConstructedQueueDelete()



Removes your client from the matchmaking queue.

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
try {
    MatchCancelResponse result = apiInstance.matchmakingConstructedQueueDelete();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#matchmakingConstructedQueueDelete");
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

<a name="matchmakingConstructedQueuePut"></a>
# **matchmakingConstructedQueuePut**
> MatchmakingQueuePutResponse matchmakingConstructedQueuePut(request)



Enters your client into a matchmaking queue for constructed deck matchmaking. Clients have to keep their matchmaking queue entry alive by regularly retrying when they have not yet been matched. Retry within 5 seconds. 

### Example
```java
// Import classes:
//import com.hiddenswitch.proto3.net.client.ApiClient;
//import com.hiddenswitch.proto3.net.client.ApiException;
//import com.hiddenswitch.proto3.net.client.Configuration;
//import com.hiddenswitch.proto3.net.client.auth.*;
//import com.hiddenswitch.proto3.net.client.api.DefaultApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: TokenSecurity
ApiKeyAuth TokenSecurity = (ApiKeyAuth) defaultClient.getAuthentication("TokenSecurity");
TokenSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//TokenSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
MatchmakingQueuePutRequest request = new MatchmakingQueuePutRequest(); // MatchmakingQueuePutRequest | The matchmaking queue entry. Contains the deck. 
try {
    MatchmakingQueuePutResponse result = apiInstance.matchmakingConstructedQueuePut(request);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#matchmakingConstructedQueuePut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **request** | [**MatchmakingQueuePutRequest**](MatchmakingQueuePutRequest.md)| The matchmaking queue entry. Contains the deck.  |

### Return type

[**MatchmakingQueuePutResponse**](MatchmakingQueuePutResponse.md)

### Authorization

[TokenSecurity](../README.md#TokenSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

