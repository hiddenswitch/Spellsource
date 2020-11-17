// <auto-generated>
//     Generated by the protocol buffer compiler.  DO NOT EDIT!
//     source: hiddenswitch.proto
// </auto-generated>
#pragma warning disable 0414, 1591
#region Designer generated code

using grpc = global::Grpc.Core;

namespace HiddenSwitch.Proto {
  public static partial class Unauthenticated
  {
    static readonly string __ServiceName = "hiddenswitch.Unauthenticated";

    static void __Helper_SerializeMessage(global::Google.Protobuf.IMessage message, grpc::SerializationContext context)
    {
      #if !GRPC_DISABLE_PROTOBUF_BUFFER_SERIALIZATION
      if (message is global::Google.Protobuf.IBufferMessage)
      {
        context.SetPayloadLength(message.CalculateSize());
        global::Google.Protobuf.MessageExtensions.WriteTo(message, context.GetBufferWriter());
        context.Complete();
        return;
      }
      #endif
      context.Complete(global::Google.Protobuf.MessageExtensions.ToByteArray(message));
    }

    static class __Helper_MessageCache<T>
    {
      public static readonly bool IsBufferMessage = global::System.Reflection.IntrospectionExtensions.GetTypeInfo(typeof(global::Google.Protobuf.IBufferMessage)).IsAssignableFrom(typeof(T));
    }

    static T __Helper_DeserializeMessage<T>(grpc::DeserializationContext context, global::Google.Protobuf.MessageParser<T> parser) where T : global::Google.Protobuf.IMessage<T>
    {
      #if !GRPC_DISABLE_PROTOBUF_BUFFER_SERIALIZATION
      if (__Helper_MessageCache<T>.IsBufferMessage)
      {
        return parser.ParseFrom(context.PayloadAsReadOnlySequence());
      }
      #endif
      return parser.ParseFrom(context.PayloadAsNewBuffer());
    }

    static readonly grpc::Marshaller<global::HiddenSwitch.Proto.CreateAccountRequest> __Marshaller_hiddenswitch_CreateAccountRequest = grpc::Marshallers.Create(__Helper_SerializeMessage, context => __Helper_DeserializeMessage(context, global::HiddenSwitch.Proto.CreateAccountRequest.Parser));
    static readonly grpc::Marshaller<global::HiddenSwitch.Proto.LoginOrCreateReply> __Marshaller_hiddenswitch_LoginOrCreateReply = grpc::Marshallers.Create(__Helper_SerializeMessage, context => __Helper_DeserializeMessage(context, global::HiddenSwitch.Proto.LoginOrCreateReply.Parser));
    static readonly grpc::Marshaller<global::HiddenSwitch.Proto.LoginRequest> __Marshaller_hiddenswitch_LoginRequest = grpc::Marshallers.Create(__Helper_SerializeMessage, context => __Helper_DeserializeMessage(context, global::HiddenSwitch.Proto.LoginRequest.Parser));

    static readonly grpc::Method<global::HiddenSwitch.Proto.CreateAccountRequest, global::HiddenSwitch.Proto.LoginOrCreateReply> __Method_CreateAccount = new grpc::Method<global::HiddenSwitch.Proto.CreateAccountRequest, global::HiddenSwitch.Proto.LoginOrCreateReply>(
        grpc::MethodType.Unary,
        __ServiceName,
        "CreateAccount",
        __Marshaller_hiddenswitch_CreateAccountRequest,
        __Marshaller_hiddenswitch_LoginOrCreateReply);

    static readonly grpc::Method<global::HiddenSwitch.Proto.LoginRequest, global::HiddenSwitch.Proto.LoginOrCreateReply> __Method_Login = new grpc::Method<global::HiddenSwitch.Proto.LoginRequest, global::HiddenSwitch.Proto.LoginOrCreateReply>(
        grpc::MethodType.Unary,
        __ServiceName,
        "Login",
        __Marshaller_hiddenswitch_LoginRequest,
        __Marshaller_hiddenswitch_LoginOrCreateReply);

    /// <summary>Service descriptor</summary>
    public static global::Google.Protobuf.Reflection.ServiceDescriptor Descriptor
    {
      get { return global::HiddenSwitch.Proto.HiddenswitchReflection.Descriptor.Services[0]; }
    }

    /// <summary>Base class for server-side implementations of Unauthenticated</summary>
    [grpc::BindServiceMethod(typeof(Unauthenticated), "BindService")]
    public abstract partial class UnauthenticatedBase
    {
      public virtual global::System.Threading.Tasks.Task<global::HiddenSwitch.Proto.LoginOrCreateReply> CreateAccount(global::HiddenSwitch.Proto.CreateAccountRequest request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      public virtual global::System.Threading.Tasks.Task<global::HiddenSwitch.Proto.LoginOrCreateReply> Login(global::HiddenSwitch.Proto.LoginRequest request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

    }

    /// <summary>Client for Unauthenticated</summary>
    public partial class UnauthenticatedClient : grpc::ClientBase<UnauthenticatedClient>
    {
      /// <summary>Creates a new client for Unauthenticated</summary>
      /// <param name="channel">The channel to use to make remote calls.</param>
      public UnauthenticatedClient(grpc::ChannelBase channel) : base(channel)
      {
      }
      /// <summary>Creates a new client for Unauthenticated that uses a custom <c>CallInvoker</c>.</summary>
      /// <param name="callInvoker">The callInvoker to use to make remote calls.</param>
      public UnauthenticatedClient(grpc::CallInvoker callInvoker) : base(callInvoker)
      {
      }
      /// <summary>Protected parameterless constructor to allow creation of test doubles.</summary>
      protected UnauthenticatedClient() : base()
      {
      }
      /// <summary>Protected constructor to allow creation of configured clients.</summary>
      /// <param name="configuration">The client configuration.</param>
      protected UnauthenticatedClient(ClientBaseConfiguration configuration) : base(configuration)
      {
      }

      public virtual global::HiddenSwitch.Proto.LoginOrCreateReply CreateAccount(global::HiddenSwitch.Proto.CreateAccountRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return CreateAccount(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual global::HiddenSwitch.Proto.LoginOrCreateReply CreateAccount(global::HiddenSwitch.Proto.CreateAccountRequest request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_CreateAccount, null, options, request);
      }
      public virtual grpc::AsyncUnaryCall<global::HiddenSwitch.Proto.LoginOrCreateReply> CreateAccountAsync(global::HiddenSwitch.Proto.CreateAccountRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return CreateAccountAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual grpc::AsyncUnaryCall<global::HiddenSwitch.Proto.LoginOrCreateReply> CreateAccountAsync(global::HiddenSwitch.Proto.CreateAccountRequest request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_CreateAccount, null, options, request);
      }
      public virtual global::HiddenSwitch.Proto.LoginOrCreateReply Login(global::HiddenSwitch.Proto.LoginRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return Login(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual global::HiddenSwitch.Proto.LoginOrCreateReply Login(global::HiddenSwitch.Proto.LoginRequest request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_Login, null, options, request);
      }
      public virtual grpc::AsyncUnaryCall<global::HiddenSwitch.Proto.LoginOrCreateReply> LoginAsync(global::HiddenSwitch.Proto.LoginRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return LoginAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual grpc::AsyncUnaryCall<global::HiddenSwitch.Proto.LoginOrCreateReply> LoginAsync(global::HiddenSwitch.Proto.LoginRequest request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_Login, null, options, request);
      }
      /// <summary>Creates a new instance of client from given <c>ClientBaseConfiguration</c>.</summary>
      protected override UnauthenticatedClient NewInstance(ClientBaseConfiguration configuration)
      {
        return new UnauthenticatedClient(configuration);
      }
    }

    /// <summary>Creates service definition that can be registered with a server</summary>
    /// <param name="serviceImpl">An object implementing the server-side handling logic.</param>
    public static grpc::ServerServiceDefinition BindService(UnauthenticatedBase serviceImpl)
    {
      return grpc::ServerServiceDefinition.CreateBuilder()
          .AddMethod(__Method_CreateAccount, serviceImpl.CreateAccount)
          .AddMethod(__Method_Login, serviceImpl.Login).Build();
    }

    /// <summary>Register service method with a service binder with or without implementation. Useful when customizing the  service binding logic.
    /// Note: this method is part of an experimental API that can change or be removed without any prior notice.</summary>
    /// <param name="serviceBinder">Service methods will be bound by calling <c>AddMethod</c> on this object.</param>
    /// <param name="serviceImpl">An object implementing the server-side handling logic.</param>
    public static void BindService(grpc::ServiceBinderBase serviceBinder, UnauthenticatedBase serviceImpl)
    {
      serviceBinder.AddMethod(__Method_CreateAccount, serviceImpl == null ? null : new grpc::UnaryServerMethod<global::HiddenSwitch.Proto.CreateAccountRequest, global::HiddenSwitch.Proto.LoginOrCreateReply>(serviceImpl.CreateAccount));
      serviceBinder.AddMethod(__Method_Login, serviceImpl == null ? null : new grpc::UnaryServerMethod<global::HiddenSwitch.Proto.LoginRequest, global::HiddenSwitch.Proto.LoginOrCreateReply>(serviceImpl.Login));
    }

  }
  public static partial class Accounts
  {
    static readonly string __ServiceName = "hiddenswitch.Accounts";

    static void __Helper_SerializeMessage(global::Google.Protobuf.IMessage message, grpc::SerializationContext context)
    {
      #if !GRPC_DISABLE_PROTOBUF_BUFFER_SERIALIZATION
      if (message is global::Google.Protobuf.IBufferMessage)
      {
        context.SetPayloadLength(message.CalculateSize());
        global::Google.Protobuf.MessageExtensions.WriteTo(message, context.GetBufferWriter());
        context.Complete();
        return;
      }
      #endif
      context.Complete(global::Google.Protobuf.MessageExtensions.ToByteArray(message));
    }

    static class __Helper_MessageCache<T>
    {
      public static readonly bool IsBufferMessage = global::System.Reflection.IntrospectionExtensions.GetTypeInfo(typeof(global::Google.Protobuf.IBufferMessage)).IsAssignableFrom(typeof(T));
    }

    static T __Helper_DeserializeMessage<T>(grpc::DeserializationContext context, global::Google.Protobuf.MessageParser<T> parser) where T : global::Google.Protobuf.IMessage<T>
    {
      #if !GRPC_DISABLE_PROTOBUF_BUFFER_SERIALIZATION
      if (__Helper_MessageCache<T>.IsBufferMessage)
      {
        return parser.ParseFrom(context.PayloadAsReadOnlySequence());
      }
      #endif
      return parser.ParseFrom(context.PayloadAsNewBuffer());
    }

    static readonly grpc::Marshaller<global::HiddenSwitch.Proto.GetAccountsRequest> __Marshaller_hiddenswitch_GetAccountsRequest = grpc::Marshallers.Create(__Helper_SerializeMessage, context => __Helper_DeserializeMessage(context, global::HiddenSwitch.Proto.GetAccountsRequest.Parser));
    static readonly grpc::Marshaller<global::HiddenSwitch.Proto.GetAccountsReply> __Marshaller_hiddenswitch_GetAccountsReply = grpc::Marshallers.Create(__Helper_SerializeMessage, context => __Helper_DeserializeMessage(context, global::HiddenSwitch.Proto.GetAccountsReply.Parser));

    static readonly grpc::Method<global::HiddenSwitch.Proto.GetAccountsRequest, global::HiddenSwitch.Proto.GetAccountsReply> __Method_GetAccounts = new grpc::Method<global::HiddenSwitch.Proto.GetAccountsRequest, global::HiddenSwitch.Proto.GetAccountsReply>(
        grpc::MethodType.Unary,
        __ServiceName,
        "GetAccounts",
        __Marshaller_hiddenswitch_GetAccountsRequest,
        __Marshaller_hiddenswitch_GetAccountsReply);

    /// <summary>Service descriptor</summary>
    public static global::Google.Protobuf.Reflection.ServiceDescriptor Descriptor
    {
      get { return global::HiddenSwitch.Proto.HiddenswitchReflection.Descriptor.Services[1]; }
    }

    /// <summary>Base class for server-side implementations of Accounts</summary>
    [grpc::BindServiceMethod(typeof(Accounts), "BindService")]
    public abstract partial class AccountsBase
    {
      public virtual global::System.Threading.Tasks.Task<global::HiddenSwitch.Proto.GetAccountsReply> GetAccounts(global::HiddenSwitch.Proto.GetAccountsRequest request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

    }

    /// <summary>Client for Accounts</summary>
    public partial class AccountsClient : grpc::ClientBase<AccountsClient>
    {
      /// <summary>Creates a new client for Accounts</summary>
      /// <param name="channel">The channel to use to make remote calls.</param>
      public AccountsClient(grpc::ChannelBase channel) : base(channel)
      {
      }
      /// <summary>Creates a new client for Accounts that uses a custom <c>CallInvoker</c>.</summary>
      /// <param name="callInvoker">The callInvoker to use to make remote calls.</param>
      public AccountsClient(grpc::CallInvoker callInvoker) : base(callInvoker)
      {
      }
      /// <summary>Protected parameterless constructor to allow creation of test doubles.</summary>
      protected AccountsClient() : base()
      {
      }
      /// <summary>Protected constructor to allow creation of configured clients.</summary>
      /// <param name="configuration">The client configuration.</param>
      protected AccountsClient(ClientBaseConfiguration configuration) : base(configuration)
      {
      }

      public virtual global::HiddenSwitch.Proto.GetAccountsReply GetAccounts(global::HiddenSwitch.Proto.GetAccountsRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return GetAccounts(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual global::HiddenSwitch.Proto.GetAccountsReply GetAccounts(global::HiddenSwitch.Proto.GetAccountsRequest request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_GetAccounts, null, options, request);
      }
      public virtual grpc::AsyncUnaryCall<global::HiddenSwitch.Proto.GetAccountsReply> GetAccountsAsync(global::HiddenSwitch.Proto.GetAccountsRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return GetAccountsAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual grpc::AsyncUnaryCall<global::HiddenSwitch.Proto.GetAccountsReply> GetAccountsAsync(global::HiddenSwitch.Proto.GetAccountsRequest request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_GetAccounts, null, options, request);
      }
      /// <summary>Creates a new instance of client from given <c>ClientBaseConfiguration</c>.</summary>
      protected override AccountsClient NewInstance(ClientBaseConfiguration configuration)
      {
        return new AccountsClient(configuration);
      }
    }

    /// <summary>Creates service definition that can be registered with a server</summary>
    /// <param name="serviceImpl">An object implementing the server-side handling logic.</param>
    public static grpc::ServerServiceDefinition BindService(AccountsBase serviceImpl)
    {
      return grpc::ServerServiceDefinition.CreateBuilder()
          .AddMethod(__Method_GetAccounts, serviceImpl.GetAccounts).Build();
    }

    /// <summary>Register service method with a service binder with or without implementation. Useful when customizing the  service binding logic.
    /// Note: this method is part of an experimental API that can change or be removed without any prior notice.</summary>
    /// <param name="serviceBinder">Service methods will be bound by calling <c>AddMethod</c> on this object.</param>
    /// <param name="serviceImpl">An object implementing the server-side handling logic.</param>
    public static void BindService(grpc::ServiceBinderBase serviceBinder, AccountsBase serviceImpl)
    {
      serviceBinder.AddMethod(__Method_GetAccounts, serviceImpl == null ? null : new grpc::UnaryServerMethod<global::HiddenSwitch.Proto.GetAccountsRequest, global::HiddenSwitch.Proto.GetAccountsReply>(serviceImpl.GetAccounts));
    }

  }
}
#endregion