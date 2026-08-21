extends Node2D

## Climb letter floors, jump barrels, mushroom grows the hero. HUD is pt-PT.
const LIVES_MAX := 3
const HIT_INVULN := 1.2
var x := 0.12
var y := 0.12
var vy := 0.0
var on_floor := true
var mask := 0
var barrel_x := 0.9
var barrel_floor := 3
var form := 0
var lives := LIVES_MAX
var invuln := 0.0
var finished := false
var hud: Label
var move_x := 0.0
var jumping := false
var flick_x := 0.0
var flick_y := 0.0
var floors := PackedFloat32Array([0.12, 0.34, 0.56, 0.78])
var letters := [Vector2(0.28, 0.12), Vector2(0.72, 0.34), Vector2(0.28, 0.56), Vector2(0.72, 0.56), Vector2(0.50, 0.78)]


func _ready() -> void:
	RenderingServer.set_default_clear_color(Color("4E342E"))
	Host.fit_viewport(self)
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	Host.skin_hud(hud, self)
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
	return pos.x / maxf(Host.view_size(self).x, 1.0)


func _run(nx: float) -> float:
	var delta := nx - 0.5
	if absf(delta) <= 0.14:
		return 0.0
	return -1.0 if delta < 0.0 else 1.0


func _process(delta: float) -> void:
	if finished:
		return
	Host.fit_viewport(self)
	delta = minf(delta, 0.05)
	invuln = maxf(invuln - delta, 0.0)
	if jumping and on_floor:
		vy = 0.85
		on_floor = false
	jumping = false
	vy += -2.4 * delta
	x = clampf(x + move_x * 0.55 * delta, 0.06, 0.94)
	y += vy * delta
	_land()
	_collect()
	if _letter_count() >= 5:
		finished = true
		Host.finish(true)
		return
	if absf(x - 0.5) < 0.08 and absf(y - 0.34) < 0.08:
		form = 1
	barrel_x += 0.35 * delta
	if barrel_x > 0.94:
		barrel_x = 0.08
		barrel_floor = 3 if barrel_floor <= 0 else barrel_floor - 1
	var floor_y := floors[clampi(barrel_floor, 0, floors.size() - 1)]
	if invuln <= 0.0 and absf(y - floor_y) < 0.05 and absf(x - barrel_x) < 0.07:
		if form == 1:
			form = 0
			invuln = HIT_INVULN
		else:
			lives -= 1
			invuln = HIT_INVULN
	if lives <= 0:
		finished = true
		Host.finish(false)
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
	var size := Host.view_size(self)
	var u := minf(size.x, size.y)
	draw_rect(Rect2(Vector2.ZERO, size), Color("4E342E"))
	draw_rect(Rect2(0, 0, size.x, size.y * 0.22), Color("81D4FA"))
	var plank_h := u * 0.028
	for floor_y in floors:
		var py := size.y * (1.0 - floor_y)
		draw_rect(Rect2(size.x * 0.05, py, size.x * 0.90, plank_h), Color("5D4037"))
		draw_rect(Rect2(size.x * 0.05, py - plank_h * 0.45, size.x * 0.90, plank_h * 0.45), Color("43A047"))
	for i in letters.size():
		if mask & (1 << i):
			continue
		var spot: Vector2 = letters[i]
		var lx := spot.x * size.x
		var ly := size.y * (1.0 - spot.y) - u * 0.05
		draw_rect(Rect2(lx - u * 0.022, ly, u * 0.044, u * 0.044), Color("FFD54F"))
	var mx := 0.5 * size.x
	var my := size.y * (1.0 - 0.34) - u * 0.04
	draw_rect(Rect2(mx - u * 0.018, my, u * 0.036, u * 0.036), Color("E53935"))
	var bx := barrel_x * size.x
	var by := size.y * (1.0 - floors[clampi(barrel_floor, 0, floors.size() - 1)]) - u * 0.036
	draw_rect(Rect2(bx - u * 0.026, by, u * 0.052, u * 0.036), Color("8D6E63"))
	var blink := invuln > 0.0 and fmod(invuln, 0.16) < 0.08
	if not blink:
		var px := x * size.x
		var body_h := u * 0.07 if form == 1 else u * 0.05
		var py := size.y * (1.0 - y) - body_h
		var fill := Color("FFF176") if form == 1 else Host.mascot_color()
		draw_rect(Rect2(px - u * 0.024, py, u * 0.048, body_h), fill)
	for v in lives:
		var lx := size.x - u * 0.05 - float(v) * u * 0.05
		draw_rect(Rect2(lx, u * 0.03, u * 0.032, u * 0.032), Host.mascot_color())


func _update_hud() -> void:
	Host.skin_hud(hud, self)
	var extra := "\nCresceste!" if form == 1 else ""
	hud.text = "Anda e desliza para cima para saltar\nLetras %d/5 · Vidas %d/%d%s" % [
		_letter_count(), lives, LIVES_MAX, extra
	]


func _letter_count() -> int:
	var n := 0
	var v := mask
	while v != 0:
		n += v & 1
		v >>= 1
	return n
