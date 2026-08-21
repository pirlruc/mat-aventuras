extends Node2D

## Letter maze: eat pellets, golden corners transform the hero. Three lives.
const SIZE := 5
const LIVES_MAX := 3
var px := 1
var py := 3
var gx := 3
var gy := 1
var g2x := 1
var g2y := 1
var pellets := 0
var power := 0.0
var form := 0
var lives := LIVES_MAX
var invuln := 0.8
var finished := false
var won := false
var hud: Label
var acc := 0.0
var dir := Vector2i.ZERO
var layout := PackedStringArray(["#####", "#...#", "#.#.#", "#...#", "#####"])


func _ready() -> void:
	pellets = _mask()
	RenderingServer.set_default_clear_color(Color("1565C0"))
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	layer.add_child(hud)
	Host.style_hud(hud, self)
	_update_hud()


func _mask() -> int:
	var bits := 0
	for y in SIZE:
		for x in SIZE:
			if _open(x, y):
				bits |= 1 << (y * SIZE + x)
	return bits


func _open(x: int, y: int) -> bool:
	if x < 0 or y < 0 or x >= SIZE or y >= SIZE:
		return false
	return layout[y][x] == "."


func _input(event: InputEvent) -> void:
	if finished:
		return
	var pos := Vector2.ZERO
	var pressed := false
	if event is InputEventScreenTouch and event.pressed:
		pos = event.position
		pressed = true
	elif event is InputEventScreenDrag:
		pos = event.position
		pressed = true
	elif event is InputEventMouseButton and event.pressed:
		pos = event.position
		pressed = true
	elif event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		pos = event.position
		pressed = true
	if not pressed:
		return
	var size := Host.screen_size(self)
	var nx := pos.x / maxf(size.x, 1.0) - 0.5
	var ny := pos.y / maxf(size.y, 1.0) - 0.5
	if absf(nx) > absf(ny):
		dir = Vector2i(1 if nx > 0.0 else -1, 0)
	else:
		dir = Vector2i(0, 1 if ny > 0.0 else -1)


func _process(delta: float) -> void:
	delta = minf(delta, 0.08)
	if not finished:
		acc += delta
		invuln = maxf(invuln - delta, 0.0)
		power = maxf(power - delta, 0.0)
		form = 1 if power > 0.0 else 0
		if acc >= 0.16:
			acc = 0.0
			_step_hero()
			_step_ghosts()
		if pellets == 0:
			_end_game(true)
		elif form == 0 and invuln <= 0.0 and _ghost_hit():
			lives -= 1
			invuln = 1.6
			px = 1
			py = 3
			if lives <= 0:
				lives = 0
				_end_game(false)
	Host.style_hud(hud, self)
	_update_hud()
	queue_redraw()


func _ghost_hit() -> bool:
	return (px == gx and py == gy) or (px == g2x and py == g2y)


func _end_game(ok: bool) -> void:
	if finished:
		return
	finished = true
	won = ok
	Host.finish(ok)


func _step_hero() -> void:
	var nx := px + dir.x
	var ny := py + dir.y
	if _open(nx, ny):
		px = nx
		py = ny
	var bit := 1 << (py * SIZE + px)
	if pellets & bit:
		pellets &= ~bit
		if (px == 1 or px == 3) and (py == 1 or py == 3):
			power = 2.4


func _step_ghosts() -> void:
	var s := -1 if form == 1 else 1
	var g1 := _ghost(gx, gy, px, py, s)
	gx = g1.x
	gy = g1.y
	var g2 := _ghost(g2x, g2y, px, py, s)
	g2x = g2.x
	g2y = g2.y


func _ghost(x: int, y: int, tx: int, ty: int, s: int) -> Vector2i:
	var sx := x + s * signi(tx - x)
	var sy := y + s * signi(ty - y)
	if _open(sx, y) and sx != x:
		return Vector2i(sx, y)
	if _open(x, sy) and sy != y:
		return Vector2i(x, sy)
	return Vector2i(x, y)


func _draw() -> void:
	var size := Host.screen_size(self)
	if size.x < 32.0 or size.y < 32.0:
		return
	draw_rect(Rect2(Vector2.ZERO, size), Color("0D47A1"))
	var cell := minf(size.x, size.y) * 0.16
	var ox := (size.x - cell * SIZE) * 0.5
	var oy := (size.y - cell * SIZE) * 0.52
	for y in SIZE:
		for x in SIZE:
			var r := Rect2(ox + x * cell, oy + y * cell, cell - cell * 0.06, cell - cell * 0.06)
			if not _open(x, y):
				draw_rect(r, Color("0A3A7A"))
				continue
			draw_rect(r, Color("1976D2"))
			var bit := 1 << (y * SIZE + x)
			if pellets & bit:
				var power_cell := (x == 1 or x == 3) and (y == 1 or y == 3)
				var rad := cell * (0.18 if power_cell else 0.08)
				draw_rect(Rect2(r.position + r.size * 0.5 - Vector2(rad, rad), Vector2(rad * 2.0, rad * 2.0)), Color("FFF59D"))
	var blink := invuln > 0.0 and int(invuln * 8.0) % 2 == 0
	if not blink:
		_dot(ox + px * cell, oy + py * cell, cell, Host.mascot_color() if form == 0 else Color("FFF176"))
	_dot(ox + gx * cell, oy + gy * cell, cell, Color("E53935") if form == 0 else Color("81D4FA"))
	_dot(ox + g2x * cell, oy + g2y * cell, cell, Color("8E24AA") if form == 0 else Color("81D4FA"))
	if finished:
		draw_rect(Rect2(Vector2.ZERO, size), Color(0, 0, 0, 0.42))


func _dot(x: float, y: float, cell: float, color: Color) -> void:
	var pad := cell * 0.18
	draw_rect(Rect2(x + pad, y + pad, cell - pad * 2.2, cell - pad * 2.2), color)


func _left() -> int:
	var left := 0
	var v := pellets
	while v != 0:
		left += v & 1
		v >>= 1
	return left


func _update_hud() -> void:
	if finished:
		hud.text = ("Ganhaste!" if won else "Perdeste!") + "\nVidas %d" % lives
		return
	var extra := "\nTransformado!" if form == 1 else ""
	hud.text = "Desliza para o lado das bolinhas\nFaltam %d  ·  Vidas %d/%d%s" % [_left(), lives, LIVES_MAX, extra]
