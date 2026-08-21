extends Node2D

## Letter-ship invaders. Drag to move, tap to fire. Ends at 0 lives or empty fleet.
const LIVES_MAX := 5
const FLEET := 15
const GRACE := 1.8
const HIT_INVULN := 1.4

var ship_x := 0.5
var shot_x := -1.0
var shot_y := -1.0
var aliens := (1 << FLEET) - 1
var origin := 0.12
var dir := 1.0
var bomb_x := -1.0
var bomb_y := -1.0
var hits := 0
var lives := LIVES_MAX
var invuln := 0.0
var grace := GRACE
var finished := false
var hud: Label
var fire := false


func _ready() -> void:
	RenderingServer.set_default_clear_color(Color("0D1B3A"))
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
			ship_x = _nx(event.position)
			fire = true
		else:
			fire = false
	elif event is InputEventScreenDrag:
		ship_x = _nx(event.position)
	elif event is InputEventMouseButton:
		if event.pressed:
			ship_x = _nx(event.position)
			fire = true
		else:
			fire = false
	elif event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		ship_x = _nx(event.position)


func _nx(pos: Vector2) -> float:
	return clampf(pos.x / maxf(Host.view_size(self).x, 1.0), 0.08, 0.92)


func _process(delta: float) -> void:
	if finished:
		return
	Host.fit_viewport(self)
	delta = minf(delta, 0.05)
	invuln = maxf(invuln - delta, 0.0)
	grace = maxf(grace - delta, 0.0)
	if shot_y < 0.0 and fire:
		shot_x = ship_x
		shot_y = 0.82
		fire = false
	if shot_y >= 0.0:
		shot_y -= 1.1 * delta
		if shot_y < 0.06:
			shot_y = -1.0
		else:
			_try_hit()
	origin += dir * 0.12 * delta
	if origin < 0.04 or origin > 0.42:
		dir = -dir
		origin = clampf(origin, 0.04, 0.42)
	_step_bomb(delta)
	if aliens == 0:
		finished = true
		Host.finish(true)
		return
	if lives <= 0:
		finished = true
		Host.finish(false)
		return
	_update_hud()
	queue_redraw()


func _step_bomb(delta: float) -> void:
	if grace > 0.0 or aliens == 0:
		return
	if bomb_y < 0.0:
		var spawn := _bomber()
		bomb_x = spawn.x
		bomb_y = spawn.y
	bomb_y += 0.45 * delta
	if bomb_y > 0.95:
		bomb_y = -1.0
	elif absf(bomb_x - ship_x) < 0.07 and bomb_y > 0.84 and invuln <= 0.0:
		lives -= 1
		invuln = HIT_INVULN
		bomb_y = -1.0


func _bomber() -> Vector2:
	for i in FLEET:
		if aliens & (1 << i) == 0:
			continue
		var col := i % 5
		var row := i / 5
		return Vector2(origin + float(col) * 0.12, 0.12 + float(row) * 0.12)
	return Vector2(-1.0, -1.0)


func _try_hit() -> void:
	for i in FLEET:
		if aliens & (1 << i) == 0:
			continue
		var col := i % 5
		var row := i / 5
		var ax := origin + float(col) * 0.12
		var ay := 0.12 + float(row) * 0.12
		if absf(ax - shot_x) < 0.05 and absf(ay - shot_y) < 0.06:
			aliens &= ~(1 << i)
			hits += 1
			shot_y = -1.0
			return


func _draw() -> void:
	var size := Host.view_size(self)
	var u := minf(size.x, size.y)
	draw_rect(Rect2(Vector2.ZERO, size), Color("0D1B3A"))
	draw_rect(Rect2(0, 0, size.x, size.y * 0.42), Color("102A54"))
	for i in 28:
		var sx := fmod(float(i) * 127.0, size.x)
		var sy := fmod(float(i) * 53.0, size.y * 0.62)
		var star := u * 0.006
		draw_rect(Rect2(sx, sy, star, star), Color("E3F2FD"))
	var ground_h := size.y * 0.14
	draw_rect(Rect2(0, size.y - ground_h, size.x, ground_h), Color("1B5E20"))
	draw_rect(Rect2(0, size.y - ground_h, size.x, u * 0.012), Color("43A047"))
	var aw := u * 0.07
	var ah := u * 0.055
	for i in FLEET:
		if aliens & (1 << i) == 0:
			continue
		var col := i % 5
		var row := i / 5
		var ax := (origin + float(col) * 0.12) * size.x
		var ay := (0.12 + float(row) * 0.12) * size.y
		draw_rect(Rect2(ax - aw * 0.5, ay - ah * 0.5, aw, ah), Color("66BB6A"))
		draw_rect(Rect2(ax - aw * 0.28, ay - ah * 0.18, aw * 0.18, ah * 0.28), Color("212121"))
		draw_rect(Rect2(ax + aw * 0.08, ay - ah * 0.18, aw * 0.18, ah * 0.28), Color("212121"))
		draw_rect(Rect2(ax - aw * 0.22, ay + ah * 0.22, aw * 0.44, ah * 0.18), Color("FFF59D"))
	if shot_y >= 0.0:
		var sw := u * 0.012
		draw_rect(Rect2(shot_x * size.x - sw * 0.5, shot_y * size.y, sw, u * 0.045), Color("FFF176"))
	if bomb_y >= 0.0:
		var bw := u * 0.018
		draw_rect(Rect2(bomb_x * size.x - bw * 0.5, bomb_y * size.y, bw, u * 0.032), Color("FF8A65"))
	var blink := invuln > 0.0 and fmod(invuln, 0.16) < 0.08
	if not blink:
		var sx := ship_x * size.x
		var sy := size.y * 0.86
		var ship_w := u * 0.11
		draw_rect(Rect2(sx - ship_w * 0.5, sy - u * 0.028, ship_w, u * 0.032), Host.mascot_color())
		draw_rect(Rect2(sx - u * 0.018, sy - u * 0.055, u * 0.036, u * 0.028), Color("ECEFF1"))
		draw_rect(Rect2(sx - u * 0.012, sy - u * 0.012, u * 0.024, u * 0.016), Color("FFF176"))
	for v in lives:
		var lx := size.x - u * 0.04 - float(v) * u * 0.045
		draw_rect(Rect2(lx, u * 0.03, u * 0.032, u * 0.018), Host.mascot_color())


func _update_hud() -> void:
	Host.skin_hud(hud, self)
	var left := 0
	var v := aliens
	while v != 0:
		left += v & 1
		v >>= 1
	hud.text = "Desliza para mover · toca para disparar\nNaves %d/%d\nVidas %d/%d" % [
		hits, FLEET, lives, LIVES_MAX
	]
	if left == 0:
		hud.text = "Ganhaste!\nNaves %d/%d" % [FLEET, FLEET]
