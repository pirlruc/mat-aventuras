extends Node2D

## Letter-ship invaders. Ends only after 5 lives or every ship is gone.
const COLS := 5
const ROWS := 3
const SHIP_COUNT := 15
const LIVES_MAX := 5
const OPENING_SAFE := 1.2

var ship_x := 0.5
var shot_x := -1.0
var shot_y := -1.0
var aliens := (1 << 15) - 1
var origin := 0.12
var dir := 1.0
var bomb_x := -1.0
var bomb_y := -1.0
var hits := 0
var lives := LIVES_MAX
var invuln := OPENING_SAFE
var finished := false
var won := false
var hud: Label
var fire := false


func _ready() -> void:
	RenderingServer.set_default_clear_color(Color("0D47A1"))
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	layer.add_child(hud)
	Host.style_hud(hud, self)
	_update_hud()


func _input(event: InputEvent) -> void:
	if finished:
		return
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
	return clampf(pos.x / maxf(Host.screen_size(self).x, 1.0), 0.08, 0.92)


func _process(delta: float) -> void:
	delta = minf(delta, 0.05)
	if not finished:
		invuln = maxf(invuln - delta, 0.0)
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
			_end_game(true)
		elif lives <= 0:
			_end_game(false)
	Host.style_hud(hud, self)
	_update_hud()
	queue_redraw()


func _step_bomb(delta: float) -> void:
	if bomb_y < 0.0 and aliens != 0 and invuln <= 0.0:
		bomb_x = origin + 0.12
		bomb_y = 0.22
	if bomb_y < 0.0:
		return
	bomb_y += 0.45 * delta
	if bomb_y > 0.95:
		bomb_y = -1.0
	elif invuln <= 0.0 and absf(bomb_x - ship_x) < 0.07 and bomb_y > 0.84:
		lives -= 1
		bomb_y = -1.0
		invuln = 1.4
		if lives <= 0:
			lives = 0


func _try_hit() -> void:
	for i in SHIP_COUNT:
		if (aliens & (1 << i)) == 0:
			continue
		var col: int = i % COLS
		var row: int = i / COLS
		var ax := origin + float(col) * 0.12
		var ay := 0.12 + float(row) * 0.12
		if absf(ax - shot_x) < 0.05 and absf(ay - shot_y) < 0.06:
			aliens &= ~(1 << i)
			hits += 1
			shot_y = -1.0
			return


func _end_game(ok: bool) -> void:
	if finished:
		return
	finished = true
	won = ok
	Host.finish(ok)


func _draw() -> void:
	var size := Host.screen_size(self)
	if size.x < 32.0 or size.y < 32.0:
		return
	var u := minf(size.x, size.y)
	draw_rect(Rect2(Vector2.ZERO, size), Color("0D47A1"))
	for i in 36:
		var sx := fmod(float(i) * 137.0, size.x)
		var sy := fmod(float(i) * 71.0, size.y * 0.72)
		draw_rect(Rect2(sx, sy, maxf(u * 0.006, 3.0), maxf(u * 0.006, 3.0)), Color("E3F2FD"))
	draw_rect(Rect2(0, size.y * 0.86, size.x, size.y * 0.14), Color("1B5E20"))
	draw_rect(Rect2(0, size.y * 0.86, size.x, u * 0.012), Color("43A047"))
	var ship_w := u * 0.11
	var ship_h := u * 0.055
	for i in SHIP_COUNT:
		if (aliens & (1 << i)) == 0:
			continue
		var col: int = i % COLS
		var row: int = i / COLS
		var ax := (origin + float(col) * 0.12) * size.x
		var ay := (0.12 + float(row) * 0.12) * size.y
		_draw_letter_ship(ax, ay, ship_w, ship_h)
	if shot_y >= 0.0:
		var bolt_w := u * 0.012
		draw_rect(Rect2(shot_x * size.x - bolt_w * 0.5, shot_y * size.y, bolt_w, u * 0.04), Color("FFF176"))
	if bomb_y >= 0.0:
		var bw := u * 0.018
		draw_rect(Rect2(bomb_x * size.x - bw * 0.5, bomb_y * size.y, bw, u * 0.03), Color("FF8A65"))
	var sx := ship_x * size.x
	var sy := size.y * 0.88
	var blink := invuln > 0.0 and int(invuln * 8.0) % 2 == 0
	if not blink:
		draw_rect(Rect2(sx - ship_w * 0.5, sy - ship_h * 0.4, ship_w, ship_h * 0.55), Host.mascot_color())
		draw_rect(Rect2(sx - ship_w * 0.18, sy - ship_h * 0.85, ship_w * 0.36, ship_h * 0.5), Color("ECEFF1"))
		draw_rect(Rect2(sx - ship_w * 0.1, sy - ship_h * 0.2, ship_w * 0.2, ship_h * 0.22), Color("FFF176"))
	_draw_lives(size, u)
	if finished:
		_draw_banner(size, "")


func _draw_letter_ship(ax: float, ay: float, w: float, h: float) -> void:
	draw_rect(Rect2(ax - w * 0.5, ay - h * 0.45, w, h), Color("66BB6A"))
	draw_rect(Rect2(ax - w * 0.28, ay - h * 0.18, w * 0.18, h * 0.28), Color("212121"))
	draw_rect(Rect2(ax + w * 0.1, ay - h * 0.18, w * 0.18, h * 0.28), Color("212121"))
	draw_rect(Rect2(ax - w * 0.22, ay + h * 0.22, w * 0.44, h * 0.18), Color("FFF59D"))


func _draw_lives(size: Vector2, u: float) -> void:
	var icon := u * 0.035
	var y := size.y * 0.03
	for i in LIVES_MAX:
		var x := size.x - (float(LIVES_MAX - i) * (icon + u * 0.012)) - size.x * 0.02
		var fill := Host.mascot_color() if i < lives else Color(1, 1, 1, 0.2)
		draw_rect(Rect2(x, y, icon, icon * 0.6), fill)


func _draw_banner(size: Vector2, _text: String) -> void:
	draw_rect(Rect2(Vector2.ZERO, size), Color(0, 0, 0, 0.42))
	var pad := size.y * 0.08
	draw_rect(Rect2(size.x * 0.12, size.y * 0.38, size.x * 0.76, size.y * 0.22), Color("1565C0"))
	draw_rect(Rect2(size.x * 0.12, size.y * 0.38, size.x * 0.76, pad * 0.12), Color("FFF176"))


func _ships_left() -> int:
	var n := 0
	var v := aliens
	while v != 0:
		n += v & 1
		v >>= 1
	return n


func _update_hud() -> void:
	if finished:
		hud.text = ("Ganhaste!" if won else "Perdeste!") + "\nNaves %d  ·  Vidas %d" % [_ships_left(), lives]
		return
	hud.text = "Desliza para mover · toca para disparar\nDestrói todas as naves\nNaves %d  ·  Vidas %d/%d" % [
		_ships_left(), lives, LIVES_MAX
	]
