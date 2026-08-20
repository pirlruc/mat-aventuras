extends Node3D

## Age-7 oval kart. Touch bands match EngineInputMap (0.34 / 0.66). HUD is pt-PT.
const STEER_LEFT_MAX := 0.34
const STEER_RIGHT_MIN := 0.66
const LAPS_TARGET := 3
const RINGS_TARGET := 8

var lap := 0
var rings := 0
var angle := 0.0
var steer := 0.0
var boosting := false
var kart: MeshInstance3D
var cam: Camera3D
var hud: Label
var finished := false


func _ready() -> void:
	_build_world()
	_update_hud(false)


func _build_world() -> void:
	var light := DirectionalLight3D.new()
	light.rotation_degrees = Vector3(-50, 35, 0)
	add_child(light)

	cam = Camera3D.new()
	add_child(cam)

	var track := MeshInstance3D.new()
	var torus := TorusMesh.new()
	torus.inner_radius = 6.0
	torus.outer_radius = 8.0
	track.mesh = torus
	track.rotation_degrees = Vector3(90, 0, 0)
	add_child(track)

	kart = MeshInstance3D.new()
	var box := BoxMesh.new()
	box.size = Vector3(0.8, 0.4, 1.2)
	kart.mesh = box
	add_child(kart)

	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24, 24)
	hud.add_theme_font_size_override("font_size", 28)
	layer.add_child(hud)


func _input(event: InputEvent) -> void:
	var pos := Vector2.ZERO
	var pressed := false
	if event is InputEventScreenTouch:
		pos = event.position
		pressed = event.pressed
		if not pressed:
			steer = 0.0
			return
	elif event is InputEventMouseButton:
		pos = event.position
		pressed = event.pressed
		if not pressed:
			steer = 0.0
			return
	else:
		return
	if not pressed:
		return
	var width: float = maxf(get_viewport().get_visible_rect().size.x, 1.0)
	var nx: float = pos.x / width
	if nx < STEER_LEFT_MAX:
		steer = -1.0
	elif nx > STEER_RIGHT_MIN:
		steer = 1.0
	else:
		boosting = true


func _process(delta: float) -> void:
	if finished:
		return
	delta = minf(delta, 0.05)
	var speed := 1.1 + (1.4 if boosting else 0.0)
	var show_boost := boosting
	boosting = false
	angle += speed * delta * (1.0 + steer * 0.15)
	var radius := 7.0
	kart.position = Vector3(cos(angle) * radius, 0.4, sin(angle) * radius)
	kart.rotation.y = -angle + PI * 0.5
	cam.position = kart.position + Vector3(0, 5, 8)
	cam.look_at(kart.position)
	if angle >= TAU:
		angle -= TAU
		lap += 1
		rings = mini(rings + 3, RINGS_TARGET)
		if lap >= LAPS_TARGET:
			_finish()
			return
	_update_hud(show_boost)


func _update_hud(show_boost: bool = false) -> void:
	var shown := mini(lap + 1, LAPS_TARGET)
	var boost_txt := "\nImpulso!" if show_boost else ""
	hud.text = "Volta %d de %d\nAnéis %d/%d%s" % [shown, LAPS_TARGET, rings, RINGS_TARGET, boost_txt]


func _finish() -> void:
	finished = true
	Host.finish(true)
