extends ColorRect

## Waits for a real Android SurfaceView, then loads the prize scene.
## Never force_draw a 0-size window — that is what left EGL_BAD_SURFACE / black.
const BOOT_BLUE := Color(0.117647, 0.533333, 0.898039, 1)
const MIN_PX := 32


func _ready() -> void:
	color = BOOT_BLUE
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	RenderingServer.set_default_clear_color(BOOT_BLUE)
	call_deferred("_load_reward")


func _process(_delta: float) -> void:
	_fit_pixels()
	queue_redraw()


func _fit_pixels() -> bool:
	var win := DisplayServer.window_get_size()
	if win.x <= MIN_PX or win.y <= MIN_PX:
		return false
	var root := get_tree().root
	root.content_scale_mode = Window.CONTENT_SCALE_MODE_DISABLED
	root.content_scale_aspect = Window.CONTENT_SCALE_ASPECT_EXPAND
	if root.size != win:
		root.size = win
	var sz := get_viewport().get_visible_rect().size
	return sz.x > float(MIN_PX) and sz.y > float(MIN_PX)


func _load_reward() -> void:
	if not await _wait_for_surface():
		push_error("Godot window never gained a real size")
		Host.finish(false)
		return
	var path := _reward_path()
	if not await _wait_for_resource(path):
		push_error("Reward scene missing: %s" % path)
		Host.finish(false)
		return
	var err := get_tree().change_scene_to_file(path)
	if err != OK:
		await get_tree().create_timer(0.25).timeout
		_fit_pixels()
		err = get_tree().change_scene_to_file(path)
	if err != OK:
		push_error("Failed to load reward scene: %s (%s)" % [path, err])
		Host.finish(false)


func _wait_for_surface() -> bool:
	for _i in 180:
		await get_tree().process_frame
		if _fit_pixels():
			for _j in 4:
				await get_tree().process_frame
			return _fit_pixels()
	return _fit_pixels()


func _wait_for_resource(path: String) -> bool:
	for _i in 90:
		if ResourceLoader.exists(path):
			return true
		await get_tree().process_frame
	return ResourceLoader.exists(path)


func _reward_path() -> String:
	var path := "res://kart.tscn"
	if Engine.has_singleton("MatAventuras"):
		var next := str(Engine.get_singleton("MatAventuras").rewardScene())
		if not next.is_empty():
			path = next
	return path
