

# Cursor


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **String** | Unique identifier for the cursor |  |
|**name** | **String** | Human-readable name for the cursor |  |
|**type** | [**TypeEnum**](#TypeEnum) | Type of cursor |  |
|**position** | [**Position**](Position.md) |  |  |
|**active** | **Boolean** | Whether the cursor is currently active |  |
|**createdAt** | **OffsetDateTime** | Timestamp when the cursor was created |  |
|**updatedAt** | **OffsetDateTime** | Timestamp when the cursor was last updated |  |



## Enum: TypeEnum

| Name | Value |
|---- | -----|
| POINTER | &quot;pointer&quot; |
| TEXT | &quot;text&quot; |
| CROSSHAIR | &quot;crosshair&quot; |
| HAND | &quot;hand&quot; |



