extends Node2D

## Age-3 side-scroller. Drag forward to run and leap; drag back to reverse. HUD is pt-PT.
const COINS_TARGET := 9
const GROUND_Y := 520.0
const SPEED := 220.0
const GRAVITY := 980.0
const JUMP_V := -520.0

var x := 80.0
var y := GROUND_Y
var vy := 0.0
var coins := 0
var move_x := 0.0
var jumping := false
var finished := false
var on_ground := true
var in_pit_fall := false
var theme: int = 0
var hud: Label
var coin_nodes: Array[ColorRect] = []
var taken: Array[bool] = []
var pits: Array[Vector2] = []
var ledges: Array[Rect2] = []
var coin_xs: Array[float] = []
var player_parts: Array[ColorRect] = []
var camera_x := 0.0


func _ready() -> void:
	theme = Time.get_ticks_msec() % 4
	_layout()
	_draw_world()
	_make_player()
	_make_coins()
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24, 24)
	hud.add_theme_font_size_override("font_size", 28)
	layer.add_child(hud)
	_update_hud()


func _layout() -> void:
	var shift := float(theme * 8)
	pits = [
		Vector2(360.0 + shift, 430.0 + shift),
		Vector2(720.0, 800.0),
		Vector2(1080.0 + shift, 1155.0 + shift),
		Vector2(1500.0, 1570.0),
	]
	ledges = [
		Rect2(240, 400, 140, 22),
		Rect2(500, 340, 130, 22),
		Rect2(860, 380, 150, 22),
		Rect2(1220, 320, 140, 22),
		Rect2(1640, 390, 160, 22),
	]
	coin_xs = [200.0, 320.0, 540.0, 680.0, 920.0, 1040.0, 1280.0, 1680.0, 1840.0]
	taken.resize(coin_xs.size())
	taken.fill(false)


func _sky() -> Color:
	var skies := [Color("7EC0ED"), Color("FFB74D"), Color("4FC3F7"), Color("5C6BC0")]
	return skies[theme]


func _grass() -> Color:
	var grass := [Color("3D9E2F"), Color("C0CA33"), Color("2E7D32"), Color("8D6E63")]
	return grass[theme]


func _brick() -> Color:
	var bricks := [Color("C75A1A"), Color("6D4C41"), Color("EF6C00"), Color("5D4037")]
	return bricks[theme]


func _draw_world() -> void:
	var sky := ColorRect.new()
	sky.color = _sky()
	sky.size = Vector2(2200, 720)
	add_child(sky)
	var band := ColorRect.new()
	band.color = _sky().darkened(0.18)
	band.position = Vector2(0, 300)
	band.size = Vector2(2200, 220)
	add_child(band)
	_add_brick(0, GROUND_Y, 2200, 24, _grass())
	_add_brick(0, GROUND_Y + 24.0, 2200, 176, _brick())
	for pit in pits:
		_add_brick(pit.x, GROUND_Y, pit.y - pit.x, 200, Color("1A0A08"))
	for ledge in ledges:
		_add_brick(ledge.position.x, ledge.position.y, ledge.size.x, ledge.size.y, _brick())


func _make_player() -> void:
	var fill := Host.mascot_color()
	var shade := fill.darkened(0.35)
	_add_part(fill, Vector2(16, 22))
	_add_part(fill, Vector2(20, 18))
	_add_part(shade, Vector2(24, 10))
	_add_part(Color("212121"), Vector2(4, 4))
	_add_part(Color("212121"), Vector2(4, 4))
	_add_part(shade, Vector2(6, 14))
	_add_part(shade, Vector2(6, 14))
	_add_part(Color("4E342E"), Vector2(10, 6))
	_add_part(Color("4E342E"), Vector2(10, 6))


func _add_part(color: Color, size: Vector2) -> void:
	var part := ColorRect.new()
	part.color = color
	part.size = size
	add_child(part)
	player_parts.append(part)


func _make_coins() -> void:
	for cx in coin_xs:
		var coin := ColorRect.new()
		coin.color = Color("FFD54F")
		coin.size = Vector2(16, 16)
		coin.position = Vector2(cx, GROUND_Y - 52.0)
		add_child(coin)
		coin_nodes.append(coin)


func _add_brick(px: float, py: float, w: float, h: float, color: Color) -> void:
	var brick := ColorRect.new()
	brick.color = color
	brick.position = Vector2(px, py)
	brick.size = Vector2(w, h)
	add_child(brick)


func _input(event: InputEvent) -> void:
	if event is InputEventScreenDrag:
		move_x = clampf(event.relative.x / 12.0, -1.0, 1.0)
		if event.relative.x > 14.0:
			jumping = true
	elif event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		move_x = clampf(event.relative.x / 12.0, -1.0, 1.0)
		if event.relative.x > 14.0:
			jumping = true
	elif event is InputEventScreenTouch and not event.pressed:
		move_x = 0.0
	elif event is InputEventMouseButton and not event.pressed:
		move_x = 0.0


func _process(delta: float) -> void:
	if finished:
		return
	delta = minf(delta, 0.05)
	if jumping and on_ground and move_x > 0.2:
		vy = JUMP_V
		on_ground = false
	jumping = false
	vy += GRAVITY * delta
	y += vy * delta
	if in_pit_fall:
		x += 0.0
	else:
		x += move_x * SPEED * delta
	_land()
	if y > 780.0:
		x = _safe_x()
		y = GROUND_Y
		vy = 0.0
		on_ground = true
		in_pit_fall = false
	camera_x = x - 240.0
	position = Vector2(-camera_x, 0)
	_place_player()
	_collect_coins()


func _place_player() -> void:
	var px := x
	var py := y - 52.0
	player_parts[0].position = Vector2(px + 10.0, py + 20.0)
	player_parts[1].position = Vector2(px + 8.0, py + 4.0)
	player_parts[2].position = Vector2(px + 6.0, py)
	player_parts[3].position = Vector2(px + 12.0, py + 10.0)
	player_parts[4].position = Vector2(px + 20.0, py + 10.0)
	player_parts[5].position = Vector2(px + 10.0, py + 40.0)
	player_parts[6].position = Vector2(px + 20.0, py + 40.0)
	player_parts[7].position = Vector2(px + 8.0, py + 52.0)
	player_parts[8].position = Vector2(px + 20.0, py + 52.0)


func _land() -> void:
	on_ground = false
	var over_pit := _in_pit(x)
	if over_pit and y >= GROUND_Y:
		in_pit_fall = true
	if in_pit_fall:
		return
	if y >= GROUND_Y and not over_pit:
		y = GROUND_Y
		vy = 0.0
		on_ground = true
		return
	for ledge in ledges:
		var on_x := x + 18.0 > ledge.position.x and x < ledge.position.x + ledge.size.x
		if on_x and vy >= 0.0 and y >= ledge.position.y and y <= ledge.position.y + 28.0:
			y = ledge.position.y
			vy = 0.0
			on_ground = true
			return


func _in_pit(px: float) -> bool:
	for pit in pits:
		if px + 18.0 > pit.x and px < pit.y:
			return true
	return false


func _safe_x() -> float:
	var best := 80.0
	for pit in pits:
		if pit.x < x:
			best = pit.x - 70.0
	return maxf(best, 80.0)


func _collect_coins() -> void:
	for i in coin_xs.size():
		if taken[i]:
			continue
		if absf(x - coin_xs[i]) < 28.0 and absf(y - GROUND_Y) < 90.0:
			taken[i] = true
			coin_nodes[i].visible = false
			coins = mini(coins + 1, COINS_TARGET)
			_update_hud()
			if coins >= COINS_TARGET:
				finished = true
				Host.finish(true)


func _update_hud() -> void:
	hud.text = "Moedas %d/%d" % [coins, COINS_TARGET]
