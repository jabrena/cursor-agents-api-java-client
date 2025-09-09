

# CreateCursorRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Human-readable name for the cursor |  |
|**type** | [**TypeEnum**](#TypeEnum) | Type of cursor |  |
|**position** | [**Position**](Position.md) |  |  |
|**active** | **Boolean** | Whether the cursor should be active initially |  [optional] |



## Enum: TypeEnum

| Name | Value |
|---- | -----|
| POINTER | &quot;pointer&quot; |
| TEXT | &quot;text&quot; |
| CROSSHAIR | &quot;crosshair&quot; |
| HAND | &quot;hand&quot; |



