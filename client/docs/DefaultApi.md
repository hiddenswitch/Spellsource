# DefaultApi

All URIs are relative to *http://localhost:8080/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**matchmakingConstructedQueueDelete**](DefaultApi.md#matchmakingConstructedQueueDelete) | **DELETE** /matchmaking/constructed/queue | 
[**matchmakingConstructedQueuePut**](DefaultApi.md#matchmakingConstructedQueuePut) | **PUT** /matchmaking/constructed/queue | 


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

// Configure HTTP basic authorization: DefaultSecurity
HttpBasicAuth DefaultSecurity = (HttpBasicAuth) defaultClient.getAuthentication("DefaultSecurity");
DefaultSecurity.setUsername("YOUR USERNAME");
DefaultSecurity.setPassword("YOUR PASSWORD");

// Configure API key authorization: DisabledSecurity
ApiKeyAuth DisabledSecurity = (ApiKeyAuth) defaultClient.getAuthentication("DisabledSecurity");
DisabledSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//DisabledSecurity.setApiKeyPrefix("Token");

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

[DefaultSecurity](../README.md#DefaultSecurity), [DisabledSecurity](../README.md#DisabledSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="matchmakingConstructedQueuePut"></a>
# **matchmakingConstructedQueuePut**
> MatchmakingQueuePutResponse matchmakingConstructedQueuePut(unused)



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

// Configure HTTP basic authorization: DefaultSecurity
HttpBasicAuth DefaultSecurity = (HttpBasicAuth) defaultClient.getAuthentication("DefaultSecurity");
DefaultSecurity.setUsername("YOUR USERNAME");
DefaultSecurity.setPassword("YOUR PASSWORD");

// Configure API key authorization: DisabledSecurity
ApiKeyAuth DisabledSecurity = (ApiKeyAuth) defaultClient.getAuthentication("DisabledSecurity");
DisabledSecurity.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//DisabledSecurity.setApiKeyPrefix("Token");

DefaultApi apiInstance = new DefaultApi();
MatchmakingQueuePutRequest unused = new MatchmakingQueuePutRequest(); // MatchmakingQueuePutRequest | The matchmaking queue entry. Contains the deck. 
try {
    MatchmakingQueuePutResponse result = apiInstance.matchmakingConstructedQueuePut(unused);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#matchmakingConstructedQueuePut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **unused** | [**MatchmakingQueuePutRequest**](MatchmakingQueuePutRequest.md)| The matchmaking queue entry. Contains the deck.  |

### Return type

[**MatchmakingQueuePutResponse**](MatchmakingQueuePutResponse.md)

### Authorization

[DefaultSecurity](../README.md#DefaultSecurity), [DisabledSecurity](../README.md#DisabledSecurity)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

