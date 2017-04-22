
# EntityChangeSetInner

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**op** | [**OpEnum**](#OpEnum) | Describes a change to an entity. &#39;A&#39; corresponds to added/inserted, &#39;C&#39; corresponds to changed, &#39;R&#39; corresponds to removed. p0 is the previous value, if it exists. p1 is the current value. Only the entity&#39;s locatino is currently populated in this changeset.  |  [optional]
**id** | **Integer** | The ID of the entity in this change set.  |  [optional]
**p0** | [**EntityState**](EntityState.md) |  |  [optional]
**p1** | [**EntityState**](EntityState.md) |  |  [optional]


<a name="OpEnum"></a>
## Enum: OpEnum
Name | Value
---- | -----
A | &quot;A&quot;
C | &quot;C&quot;
R | &quot;R&quot;



