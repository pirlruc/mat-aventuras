extends Node2D

## Letter maze: eat pellets, golden corners transform the hero. HUD is pt-PT.
const SIZE := 5
var px := 1
var py := 3
var gx := 3
var gy := 1
var g2x := 1
var g2y := 1
var pellets := 0
var power := 0.0
var form := 0
var finished := false
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
	hud.position = Vector2(24, 24)
	hud.add_theme_font_size_override("font_size", 28)
	hud.add_theme_color_override("font_color", Color.WHITE)
	hud.add_theme_color_override("font_outline_color", Color.BLACK)
	hud.add_theme_constant_override("outline_size", 10)
	layer.add_child(hud)
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
	var size := get_viewport().get_visible_rect().size
	var nx := pos.x / maxf(size.x, 1.0) - 0.5
	var ny := pos.y / maxf(size.y, 1.0) - 0.5
	if absf(nx) > absf(ny):
		dir = Vector2i(1 if nx > 0.0 else -1, 0)
	else:
		dir = Vector2i(0, 1 if ny > 0.0 else -1)


func _process(delta: float) -> void:
	if finished:
		return
	delta = minf(delta, 0.08)
	acc += delta
	power = maxf(power - delta, 0.0)
	form = 1 if power > 0.0 else 0
	if acc >= 0.16:
		acc = 0.0
		_step_hero()
		_step_ghosts()
	if pellets == 0:
		finished = true
		Host.finish(true)
		return
	if form == 0 and ((px == gx and py == gy) or (px == g2x and py == g2y)):
		finished = true
		Host.finish(false)
		return
	_update_hud()
	queue_redraw()


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
	gx = _ghost(gx, gy, px, py, s).x
	gy = _ghost(gx, gy, px, py, s).y
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
	var size := get_viewport_rect().size
	draw_rect(Rect2(Vector2.ZERO, size), Color("1565C0"))
	var cell := minf(size.x, size.y) / 7.0
	var ox := (size.x - cell * SIZE) * 0.5
	var oy := (size.y - cell * SIZE) * 0.5
	for y in SIZE:
		for x in SIZE:
			var r := Rect2(ox + x * cell, oy + y * cell, cell - 4.0, cell - 4.0)
			if not _open(x, y):
				draw_rect(r, Color("0D47A1"))
				continue
			draw_rect(r, Color("1976D2"))
			var bit := 1 << (y * SIZE + x)
			if pellets & bit:
				var power_cell := (x == 1 or x == 3) and (y == 1 or y == 3)
				var rad := 10.0 if power_cell else 5.0
				draw_rect(Rect2(r.position + r.size * 0.5 - Vector2(rad, rad), Vector2(rad * 2.0, rad * 2.0)), Color("FFF59D"))
	_dot(ox + px * cell, oy + py * cell, cell, Host.mascot_color() if form == 0 else Color("FFF176"))
	_dot(ox + gx * cell, oy + gy * cell, cell, Color("E53935") if form == 0 else Color("81D4FA"))
	_dot(ox + g2x * cell, oy + g2y * cell, cell, Color("8E24AA") if form == 0 else Color("81D4FA"))


func _dot(x: float, y: float, cell: float, color: Color) -> void:
	draw_rect(Rect2(x + 8.0, y + 8.0, cell - 20.0, cell - 20.0), color)


func _update_hud() -> void:
	var extra := "\nTransformado!" if form == 1 else ""
	var left := 0
	var v := pellets
	while v != 0:
		left += v & 1
		v >>= 1
	hud.text = "Desliza para o lado das bolinhas\nFaltam %d%s" % [left, extra]
