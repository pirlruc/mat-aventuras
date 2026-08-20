extends Node

## Bridge to the Android IsolatedEngineActivity via Engine.get_singleton("MatAventuras").
var plugin: Object


func _ready() -> void:
	if Engine.has_singleton("MatAventuras"):
		plugin = Engine.get_singleton("MatAventuras")


func mascot_code() -> String:
	if plugin:
		return str(plugin.mascotCode())
	return ""


func child_name() -> String:
	if plugin:
		return str(plugin.childName())
	return ""


func finish(ok: bool) -> void:
	if plugin:
		plugin.completeReward(ok)
	else:
		get_tree().quit()
