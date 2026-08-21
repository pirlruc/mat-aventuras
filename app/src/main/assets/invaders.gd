extends Node2D

## Letter-ship invaders. Drag to move, tap to fire. HUD is pt-PT.
var ship_x := 0.5
var shot_x := -1.0
var shot_y := -1.0
var aliens := (1 << 15) - 1
var origin := 0.12
var dir := 1.0
var bomb_x := -1.0
var bomb_y := -1.0
var hits := 0
var finished := false
var hud: Label
var fire := false


func _ready() -> void:
	RenderingServer.set_default_clear_color(Color("0D47A1"))
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
	return clampf(pos.x / maxf(get_viewport().get_visible_rect().size.x, 1.0), 0.08, 0.92)


func _process(delta: float) -> void:
	if finished:
		return
	delta = minf(delta, 0.05)
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
	if bomb_y < 0.0 and aliens != 0:
		bomb_x = origin + 0.12
		bomb_y = 0.22
	if bomb_y >= 0.0:
		bomb_y += 0.45 * delta
		if bomb_y > 0.95:
			bomb_y = -1.0
		elif absf(bomb_x - ship_x) < 0.07 and bomb_y > 0.84:
			Host.finish(false)
			finished = true
			return
	if hits >= 8 or aliens == 0:
		finished = true
		Host.finish(true)
		return
	_update_hud()
	queue_redraw()


func _try_hit() -> void:
	for i in 15:
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
	var size := get_viewport_rect().size
	if size.x < 32.0 or size.y < 32.0:
		return
	draw_rect(Rect2(Vector2.ZERO, size), Color("0D47A1"))
	for i in 18:
		var sx := fmod(float(i) * 127.0, size.x)
		var sy := fmod(float(i) * 53.0, size.y * 0.7)
		draw_rect(Rect2(sx, sy, 3.0, 3.0), Color("E3F2FD"))
	draw_rect(Rect2(0, size.y * 0.88, size.x, size.y * 0.12), Color("1B5E20"))
	for i in 15:
		if aliens & (1 << i) == 0:
			continue
		var col := i % 5
		var row := i / 5
		var ax := (origin + float(col) * 0.12) * size.x
		var ay := (0.12 + float(row) * 0.12) * size.y
		draw_rect(Rect2(ax - 18.0, ay - 14.0, 36.0, 28.0), Color("66BB6A"))
		draw_rect(Rect2(ax - 10.0, ay - 6.0, 8.0, 8.0), Color("212121"))
		draw_rect(Rect2(ax + 2.0, ay - 6.0, 8.0, 8.0), Color("212121"))
		draw_rect(Rect2(ax - 8.0, ay + 8.0, 16.0, 6.0), Color("FFF59D"))
	if shot_y >= 0.0:
		draw_rect(Rect2(shot_x * size.x - 3.0, shot_y * size.y, 6.0, 18.0), Color("FFF176"))
	if bomb_y >= 0.0:
		draw_rect(Rect2(bomb_x * size.x - 5.0, bomb_y * size.y, 10.0, 14.0), Color("FF8A65"))
	var sx := ship_x * size.x
	var sy := size.y * 0.88
	draw_rect(Rect2(sx - 32.0, sy - 20.0, 64.0, 20.0), Host.mascot_color())
	draw_rect(Rect2(sx - 10.0, sy - 32.0, 20.0, 14.0), Color("ECEFF1"))
	draw_rect(Rect2(sx - 6.0, sy - 8.0, 12.0, 8.0), Color("FFF176"))


func _update_hud() -> void:
	hud.text = "Desliza para mover · toca para disparar\nLetras %d/8" % mini(hits, 8)
