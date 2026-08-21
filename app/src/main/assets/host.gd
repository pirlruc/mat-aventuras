extends Node

## Bridge to IsolatedEngineActivity plus screen-pixel helpers for prize games.
var plugin: Object
var settling := false


func _ready() -> void:
	if Engine.has_singleton("MatAventuras"):
		plugin = Engine.get_singleton("MatAventuras")


func mascot_code() -> String:
	if plugin:
		return str(plugin.mascotCode())
	return ""


func mascot_color() -> Color:
	match mascot_code():
		"speedy_hedgehog":
			return Color("1E88E5")
		"hero_pup":
			return Color("FFB300")
		"pink_piglet":
			return Color("EC407A")
		"brave_plumber":
			return Color("43A047")
		"mischievous_alien":
			return Color("7E57C2")
		_:
			return Color("FB8C00")


func child_name() -> String:
	if plugin:
		return str(plugin.childName())
	return ""


func screen_size(node: Node) -> Vector2:
	var vis := Vector2(1280, 720)
	if node and node.get_viewport():
		vis = node.get_viewport().get_visible_rect().size
	var win := DisplayServer.window_get_size()
	return Vector2(maxf(vis.x, float(win.x)), maxf(vis.y, float(win.y)))


func font_size(node: Node) -> int:
	return maxi(int(screen_size(node).y * 0.042), 22)


func style_hud(hud: Label, node: Node) -> void:
	var size := screen_size(node)
	hud.position = Vector2(size.x * 0.02, size.y * 0.018)
	hud.size = Vector2(size.x * 0.72, size.y * 0.28)
	hud.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	hud.add_theme_font_size_override("font_size", font_size(node))
	hud.add_theme_color_override("font_color", Color.WHITE)
	hud.add_theme_color_override("font_outline_color", Color.BLACK)
	hud.add_theme_constant_override("outline_size", maxi(int(size.y * 0.012), 8))


func finish(ok: bool) -> void:
	if settling:
		return
	settling = true
	await get_tree().create_timer(1.8).timeout
	if plugin:
		plugin.completeReward(ok)
	else:
		get_tree().quit()
