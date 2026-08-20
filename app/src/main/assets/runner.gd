extends Node2D

## Age-3 ring runner. Any tap jumps. HUD is pt-PT.
const RINGS_TARGET := 5
const GROUND_Y := 520.0

var x := 80.0
var y := GROUND_Y
var vy := 0.0
var rings := 0
var jumping := false
var finished := false
var player: ColorRect
var hud: Label


func _ready() -> void:
	var sky := ColorRect.new()
	sky.color = Color("81D4FA")
	sky.size = Vector2(1280, 720)
	add_child(sky)

	var ground := ColorRect.new()
	ground.color = Color("66BB6A")
	ground.position = Vector2(0, GROUND_Y)
	ground.size = Vector2(1280, 200)
	add_child(ground)

	player = ColorRect.new()
	player.color = Color("FB8C00")
	player.size = Vector2(48, 48)
	add_child(player)

	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24, 24)
	hud.add_theme_font_size_override("font_size", 28)
	layer.add_child(hud)
	_update_hud()


func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch and event.pressed:
		jumping = true
	elif event is InputEventMouseButton and event.pressed:
		jumping = true


func _process(delta: float) -> void:
	if finished:
		return
	if jumping and y >= GROUND_Y - 1.0:
		vy = -420.0
	jumping = false
	vy += 980.0 * delta
	y = minf(y + vy * delta, GROUND_Y)
	if y >= GROUND_Y:
		vy = 0.0
		y = GROUND_Y
	x += 180.0 * delta
	player.position = Vector2(fmod(x, 1200.0) + 40.0, y - 48.0)
	if x > 80.0 + rings * 220.0:
		rings = mini(rings + 1, RINGS_TARGET)
		_update_hud()
		if rings >= RINGS_TARGET:
			finished = true
			Host.finish(true)


func _update_hud() -> void:
	hud.text = "Anéis %d/%d" % [rings, RINGS_TARGET]
