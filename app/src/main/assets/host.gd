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


func finish(ok: bool) -> void:
	if plugin:
		plugin.completeReward(ok)
	else:
		get_tree().quit()


func view_size(node: Node) -> Vector2:
	var vis := Vector2.ZERO
	var vp := node.get_viewport()
	if vp:
		vis = vp.get_visible_rect().size
	var win := DisplayServer.window_get_size()
	var out := Vector2(maxf(vis.x, float(win.x)), maxf(vis.y, float(win.y)))
	if out.x < 32.0 or out.y < 32.0:
		return Vector2(1920, 1080)
	return out


func fit_viewport(node: Node) -> Vector2:
	var size := view_size(node)
	var tree := node.get_tree()
	if tree:
		tree.root.content_scale_mode = Window.CONTENT_SCALE_MODE_DISABLED
	return size


func unit(node: Node) -> float:
	var size := view_size(node)
	return minf(size.x, size.y)


func skin_hud(hud: Label, node: Node) -> void:
	var u := unit(node)
	var px := maxi(28, int(u * 0.036))
	hud.position = Vector2(u * 0.028, u * 0.024)
	hud.add_theme_font_size_override("font_size", px)
	hud.add_theme_color_override("font_color", Color.WHITE)
	hud.add_theme_color_override("font_outline_color", Color.BLACK)
	hud.add_theme_constant_override("outline_size", maxi(8, int(float(px) * 0.28)))
