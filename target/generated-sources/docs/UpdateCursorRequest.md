

# UpdateCursorRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**name** | **String** | Human-readable name for the cursor |  [optional] |
|**type** | [**TypeEnum**](#TypeEnum) | Type of cursor |  [optional] |
|**position** | [**Position**](Position.md) |  |  [optional] |
|**active** | **Boolean** | Whether the cursor is currently active |  [optional] |



## Enum: TypeEnum

| Name | Value |
|---- | -----|
| POINTER | &quot;pointer&quot; |
| TEXT | &quot;text&quot; |
| CROSSHAIR | &quot;crosshair&quot; |
| HAND | &quot;hand&quot; |



