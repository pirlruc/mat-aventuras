extends Node2D

## Climb letter floors, jump barrels, mushroom grows the hero. HUD is pt-PT.
var x := 0.12
var y := 0.12
var vy := 0.0
var on_floor := true
var mask := 0
var barrel_x := 0.9
var barrel_floor := 3
var form := 0
var finished := false
var hud: Label
var move_x := 0.0
var jumping := false
var flick_x := 0.0
var flick_y := 0.0
var floors := PackedFloat32Array([0.12, 0.34, 0.56, 0.78])
var letters := [Vector2(0.28, 0.12), Vector2(0.72, 0.34), Vector2(0.28, 0.56), Vector2(0.72, 0.56), Vector2(0.50, 0.78)]


func _ready() -> void:
	RenderingServer.set_default_clear_color(Color("6D4C41"))
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24, 24)
	hud.add_theme_font_size_override("font_size", 28)
	hud.add_theme_color_override("font_color", Color.WHITE)
	hud.add_theme_color_override("font_outline_color", Color.BLACK)
	hud.add_theme_constant_override("outline_size", 10)
	layer.add_child(hud)
	_update_hud()


func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed:
			move_x = _run(_nx(event.position))
			flick_x = 0.0
			flick_y = 0.0
		else:
			move_x = 0.0
	elif event is InputEventMouseButton:
		if event.pressed:
			move_x = _run(_nx(event.position))
			flick_x = 0.0
			flick_y = 0.0
		else:
			move_x = 0.0
	elif event is InputEventScreenDrag:
		move_x = _run(_nx(event.position))
		flick_x += event.relative.x
		flick_y += event.relative.y
		if flick_y < -56.0 and absf(flick_y) >= absf(flick_x) * 1.2:
			jumping = true
	elif event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		move_x = _run(_nx(event.position))
		flick_x += event.relative.x
		flick_y += event.relative.y
		if flick_y < -56.0 and absf(flick_y) >= absf(flick_x) * 1.2:
			jumping = true


func _nx(pos: Vector2) -> float:
	return pos.x / maxf(get_viewport().get_visible_rect().size.x, 1.0)


func _run(nx: float) -> float:
	var delta := nx - 0.5
	if absf(delta) <= 0.14:
		return 0.0
	return -1.0 if delta < 0.0 else 1.0


func _process(delta: float) -> void:
	if finished:
		return
	delta = minf(delta, 0.05)
	if jumping and on_floor:
		vy = 0.85
		on_floor = false
	jumping = false
	vy += -2.4 * delta
	x = clampf(x + move_x * 0.55 * delta, 0.06, 0.94)
	y += vy * delta
	_land()
	_collect()
	if absf(x - 0.5) < 0.08 and absf(y - 0.34) < 0.08:
		form = 1
	barrel_x += 0.35 * delta
	if barrel_x > 0.94:
		barrel_x = 0.08
		barrel_floor = 3 if barrel_floor <= 0 else barrel_floor - 1
	var floor_y := floors[clampi(barrel_floor, 0, floors.size() - 1)]
	if absf(y - floor_y) < 0.05 and absf(x - barrel_x) < 0.07:
		if form == 1:
			form = 0
		else:
			finished = true
			Host.finish(false)
			return
	if _letter_count() >= 5:
		finished = true
		Host.finish(true)
		return
	_update_hud()
	queue_redraw()


func _land() -> void:
	on_floor = false
	if vy > 0.0:
		return
	for floor_y in floors:
		if y <= floor_y and y >= floor_y - 0.08:
			y = floor_y
			vy = 0.0
			on_floor = true
			return
	if y <= floors[0]:
		y = floors[0]
		vy = 0.0
		on_floor = true


func _collect() -> void:
	for i in letters.size():
		var bit := 1 << i
		if mask & bit:
			continue
		var spot: Vector2 = letters[i]
		if absf(x - spot.x) < 0.08 and absf(y - spot.y) < 0.08:
			mask |= bit


func _draw() -> void:
	var size := get_viewport_rect().size
	draw_rect(Rect2(Vector2.ZERO, size), Color("6D4C41"))
	draw_rect(Rect2(0, 0, size.x, size.y * 0.18), Color("81D4FA"))
	for floor_y in floors:
		var py := size.y * (1.0 - floor_y)
		draw_rect(Rect2(size.x * 0.06, py, size.x * 0.88, 18.0), Color("5D4037"))
		draw_rect(Rect2(size.x * 0.06, py - 8.0, size.x * 0.88, 8.0), Color("43A047"))
	for i in letters.size():
		if mask & (1 << i):
			continue
		var spot: Vector2 = letters[i]
		var lx := spot.x * size.x
		var ly := size.y * (1.0 - spot.y) - 28.0
		draw_rect(Rect2(lx - 12.0, ly, 24.0, 24.0), Color("FFD54F"))
	var mx := 0.5 * size.x
	var my := size.y * (1.0 - 0.34) - 22.0
	draw_rect(Rect2(mx - 10.0, my, 20.0, 20.0), Color("E53935"))
	var bx := barrel_x * size.x
	var by := size.y * (1.0 - floors[clampi(barrel_floor, 0, floors.size() - 1)]) - 20.0
	draw_rect(Rect2(bx - 14.0, by, 28.0, 20.0), Color("8D6E63"))
	var px := x * size.x
	var py := size.y * (1.0 - y) - (40.0 if form == 1 else 32.0)
	var fill := Color("FFF176") if form == 1 else Host.mascot_color()
	draw_rect(Rect2(px - 14.0, py, 28.0, 36.0 if form == 1 else 28.0), fill)


func _update_hud() -> void:
	var extra := "\nCresceste!" if form == 1 else ""
	hud.text = "Anda e desliza para cima para saltar\nLetras %d/5%s" % [_letter_count(), extra]


func _letter_count() -> int:
	var n := 0
	var v := mask
	while v != 0:
		n += v & 1
		v >>= 1
	return n
