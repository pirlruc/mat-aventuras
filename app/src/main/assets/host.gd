extends Node

## Bridge to IsolatedEngineActivity plus screen-pixel helpers for prize games.
const DEADZONE := 0.14
const JUMP_FLICK := 56.0

var plugin: Object
var settling := false
var fitted := Vector2.ZERO
var _end_timer: SceneTreeTimer


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


func view_size(node: Node) -> Vector2:
	var vis := Vector2.ZERO
	var vp := node.get_viewport()
	if vp:
		vis = vp.get_visible_rect().size
	var win := DisplayServer.window_get_size()
	var out := Vector2(maxf(vis.x, float(win.x)), maxf(vis.y, float(win.y)))
	if out.x < 32.0 or out.y < 32.0:
		return Vector2(1280, 720)
	return out


func fit_viewport(node: Node) -> Vector2:
	var size := view_size(node)
	if size == fitted:
		return size
	fitted = size
	var tree := node.get_tree()
	if tree:
		tree.root.content_scale_mode = Window.CONTENT_SCALE_MODE_DISABLED
	return size


func unit(node: Node) -> float:
	var size := view_size(node)
	return minf(size.x, size.y)


## CanvasLayer plus a skinned pt-PT label. Prize scenes share this instead of copying it.
func make_hud(node: Node) -> Label:
	var layer := CanvasLayer.new()
	node.add_child(layer)
	var label := Label.new()
	skin_hud(label, node)
	layer.add_child(label)
	return label


## Pointer sample shared by prize scenes.
## Empty when [event] is not a touch or mouse sample.
## Keys: pos (Vector2), down (bool), up (bool), move (bool), rel (Vector2).
func read_pointer(event: InputEvent) -> Dictionary:
	if event is InputEventScreenTouch:
		return {"pos": event.position, "down": event.pressed, "up": not event.pressed, "move": false, "rel": Vector2.ZERO}
	if event is InputEventMouseButton:
		return {"pos": event.position, "down": event.pressed, "up": not event.pressed, "move": false, "rel": Vector2.ZERO}
	if event is InputEventScreenDrag:
		return {"pos": event.position, "down": false, "up": false, "move": true, "rel": event.relative}
	if event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		return {"pos": event.position, "down": false, "up": false, "move": true, "rel": event.relative}
	return {}


func normalized_x(node: Node, pos: Vector2) -> float:
	return pos.x / maxf(view_size(node).x, 1.0)


## Full left/right in [-1, 1]. Centre dead-zone is 0 so a boost tap does not steer.
func axis_from_normalized_x(nx: float) -> float:
	var delta := nx - 0.5
	if absf(delta) <= DEADZONE:
		return 0.0
	return -1.0 if delta < 0.0 else 1.0


func is_jump_flick(dx: float, dy: float) -> bool:
	return dy <= -JUMP_FLICK and absf(dy) >= absf(dx) * 1.2


func skin_hud(hud: Label, node: Node) -> void:
	var u := unit(node)
	var px := maxi(28, int(u * 0.036))
	hud.position = Vector2(u * 0.028, u * 0.024)
	hud.size = Vector2(view_size(node).x * 0.72, view_size(node).y * 0.28)
	hud.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	hud.add_theme_font_size_override("font_size", px)
	hud.add_theme_color_override("font_color", Color.WHITE)
	hud.add_theme_color_override("font_outline_color", Color.BLACK)
	hud.add_theme_constant_override("outline_size", maxi(8, int(float(px) * 0.28)))


func finish(ok: bool, linger: bool = true) -> void:
	if settling:
		return
	settling = true
	if linger:
		var tree := get_tree()
		if tree:
			# SceneTree holds the timer. Callers do not wait on finish(), so a
			# coroutine would be dropped and the reward would never complete.
			_end_timer = tree.create_timer(1.6)
			_end_timer.timeout.connect(_complete.bind(ok))
			return
	_complete(ok)


func _complete(ok: bool) -> void:
	if plugin:
		plugin.completeReward(ok)
		return
	var later := get_tree()
	if later:
		later.quit()
