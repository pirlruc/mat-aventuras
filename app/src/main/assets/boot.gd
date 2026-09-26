extends ColorRect

## Waits until the Android SurfaceView has a real size, then loads the reward.
## Frame counting stays in _process. A deferred coroutine stops at the
## first suspension, which left this blue rect on screen.
var _frames := 0
var _sized_frames := 0
var _started := false


func _ready() -> void:
	color = Color(0.117647, 0.533333, 0.898039, 1)
	RenderingServer.set_default_clear_color(color)


func _process(_delta: float) -> void:
	if _started:
		return
	_frames += 1
	var sz := get_viewport().get_visible_rect().size
	var win := DisplayServer.window_get_size()
	if sz.x > 32.0 and sz.y > 32.0 and win.x > 32 and win.y > 32:
		_sized_frames += 1
	if _sized_frames < 8 and _frames < 56:
		return
	_started = true
	set_process(false)
	_load_reward()


func _load_reward() -> void:
	var path := "res://kart.tscn"
	if Engine.has_singleton("MatAventuras"):
		var next := str(Engine.get_singleton("MatAventuras").rewardScene())
		if not next.is_empty():
			path = next
	var packed := load(path) as PackedScene
	if packed == null:
		push_error("Reward scene missing: %s" % path)
		Host.finish(false, false)
		return
	# A script that fails to parse still instantiates. Without this check the
	# blue clear color stays up and the prize never draws.
	var preview := packed.instantiate()
	var has_script := preview.get_script() != null
	preview.free()
	if not has_script:
		push_error("Reward script failed: %s" % path)
		Host.finish(false, false)
		return
	var err := get_tree().change_scene_to_packed(packed)
	if err != OK:
		push_error("Failed to load reward scene: %s (%s)" % [path, err])
		Host.finish(false, false)
