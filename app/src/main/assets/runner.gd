extends Node2D

## Age-3 side-scroller. Hold right/left to run; swipe up to jump. HUD is pt-PT.
const COINS_TARGET := 9
const GROUND_Y := 520.0
const SPEED := 220.0
const GRAVITY := 980.0
const JUMP_V := -520.0
const DEADZONE := 0.14
const JUMP_FLICK := 56.0

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
var pal_sky: Color = Color("7EC0ED")
var pal_grass: Color = Color("3D9E2F")
var pal_brick: Color = Color("C75A1A")
var enemies: Array[Vector3] = [] # min_x, max_x, speed
var enemy_nodes: Array[ColorRect] = []
var stomped: Array[bool] = []
var power_xs: Array[float] = []
var power_grow: Array[bool] = []
var power_nodes: Array[ColorRect] = []
var power_taken: Array[bool] = []
var form := 0
var star_timer := 0.0
var elapsed := 0.0
var finger_down := false
var finger_x := 0.5
var flick_x := 0.0
var flick_y := 0.0


func _ready() -> void:
	theme = Time.get_ticks_msec() % 4
	_cache_palette()
	_layout()
	_draw_world()
	_make_player()
	_make_coins()
	_make_enemies()
	_make_powers()
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	layer.add_child(hud)
	Host.style_hud(hud, self)
	_update_hud()
	RenderingServer.set_default_clear_color(pal_sky)


func _cache_palette() -> void:
	var skies := [Color("7EC0ED"), Color("FFB74D"), Color("4FC3F7"), Color("5C6BC0")]
	var grass := [Color("3D9E2F"), Color("C0CA33"), Color("2E7D32"), Color("8D6E63")]
	var bricks := [Color("C75A1A"), Color("6D4C41"), Color("EF6C00"), Color("5D4037")]
	pal_sky = skies[theme]
	pal_grass = grass[theme]
	pal_brick = bricks[theme]


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
	enemies = [Vector3(480.0, 640.0, 70.0), Vector3(900.0, 1100.0, 55.0), Vector3(1600.0, 1780.0, 65.0)]
	stomped.resize(enemies.size())
	stomped.fill(false)
	power_xs = [250.0, 1320.0]
	power_grow = [true, false]
	power_taken.resize(2)
	power_taken.fill(false)


func _draw_world() -> void:
	var sky := ColorRect.new()
	sky.color = pal_sky
	sky.size = Vector2(2200, 720)
	add_child(sky)
	var band := ColorRect.new()
	band.color = pal_sky.darkened(0.18)
	band.position = Vector2(0, 300)
	band.size = Vector2(2200, 220)
	add_child(band)
	for i in 6:
		var cloud := ColorRect.new()
		cloud.color = Color(1, 1, 1, 0.55)
		cloud.position = Vector2(80.0 + i * 320.0, 40.0 + float(i % 3) * 18.0)
		cloud.size = Vector2(110, 36)
		add_child(cloud)
	_add_brick(0, GROUND_Y, 2200, 24, pal_grass)
	_add_brick(0, GROUND_Y + 24.0, 2200, 176, pal_brick)
	for pit in pits:
		_add_brick(pit.x, GROUND_Y, pit.y - pit.x, 200, Color("1A0A08"))
	for ledge in ledges:
		_add_brick(ledge.position.x, ledge.position.y, ledge.size.x, ledge.size.y, pal_brick)
		_add_brick(ledge.position.x, ledge.position.y - 8.0, ledge.size.x, 8.0, pal_grass)


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
		coin.size = Vector2(18, 18)
		coin.position = Vector2(cx, GROUND_Y - 56.0)
		add_child(coin)
		coin_nodes.append(coin)


func _make_enemies() -> void:
	for _e in enemies:
		var body := ColorRect.new()
		body.color = Color("6D4C41")
		body.size = Vector2(28, 22)
		add_child(body)
		enemy_nodes.append(body)


func _make_powers() -> void:
	for i in power_xs.size():
		var node := ColorRect.new()
		node.color = Color("E53935") if power_grow[i] else Color("FFF176")
		node.size = Vector2(20, 20)
		node.position = Vector2(power_xs[i], GROUND_Y - 58.0)
		add_child(node)
		power_nodes.append(node)


func _add_brick(px: float, py: float, w: float, h: float, color: Color) -> void:
	var brick := ColorRect.new()
	brick.color = color
	brick.position = Vector2(px, py)
	brick.size = Vector2(w, h)
	add_child(brick)


func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		finger_down = event.pressed
		if event.pressed:
			finger_x = _nx(event.position)
			move_x = _run_from(finger_x)
			flick_x = 0.0
			flick_y = 0.0
		else:
			move_x = 0.0
	elif event is InputEventMouseButton:
		finger_down = event.pressed
		if event.pressed:
			finger_x = _nx(event.position)
			move_x = _run_from(finger_x)
			flick_x = 0.0
			flick_y = 0.0
		else:
			move_x = 0.0
	elif event is InputEventScreenDrag:
		finger_x = _nx(event.position)
		move_x = _run_from(finger_x)
		flick_x += event.relative.x
		flick_y += event.relative.y
		if flick_y < -JUMP_FLICK and absf(flick_y) >= absf(flick_x) * 1.2:
			jumping = true
	elif event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		finger_x = _nx(event.position)
		move_x = _run_from(finger_x)
		flick_x += event.relative.x
		flick_y += event.relative.y
		if flick_y < -JUMP_FLICK and absf(flick_y) >= absf(flick_x) * 1.2:
			jumping = true


func _nx(pos: Vector2) -> float:
	return pos.x / maxf(Host.screen_size(self).x, 1.0)


func _run_from(nx: float) -> float:
	var delta := nx - 0.5
	if absf(delta) <= DEADZONE:
		return 0.0
	return -1.0 if delta < 0.0 else 1.0


func _process(delta: float) -> void:
	delta = minf(delta, 0.05)
	_fit_screen()
	if not finished:
		elapsed += delta
		star_timer = maxf(star_timer - delta, 0.0)
		if star_timer <= 0.0 and form == 2:
			form = 1
		if jumping and on_ground:
			vy = JUMP_V
			on_ground = false
		jumping = false
		vy += GRAVITY * delta
		y += vy * delta
		if not in_pit_fall:
			x += move_x * SPEED * delta
		x = clampf(x, 80.0, 2000.0)
		_land()
		if y > 780.0:
			x = _safe_x()
			y = GROUND_Y
			vy = 0.0
			on_ground = true
			in_pit_fall = false
		_place_player()
		_place_enemies()
		_collect_coins()
		_collect_powers()
		_bump_enemies()
	Host.style_hud(hud, self)
	_update_hud()


func _fit_screen() -> void:
	var sz := Host.screen_size(self)
	if sz.y < 32.0:
		return
	var s := sz.y / 720.0
	scale = Vector2(s, s)
	camera_x = x - 240.0
	position = Vector2(-camera_x * s, 0.0)


func _place_player() -> void:
	var grow := 1.25 if form >= 1 else 1.0
	if form == 2:
		player_parts[0].color = Color("FFF176")
	else:
		player_parts[0].color = Host.mascot_color()
	var px := x
	var py := y - 52.0 * grow
	player_parts[0].position = Vector2(px + 10.0, py + 20.0 * grow)
	player_parts[1].position = Vector2(px + 8.0, py + 4.0 * grow)
	player_parts[2].position = Vector2(px + 6.0, py)
	player_parts[3].position = Vector2(px + 12.0, py + 10.0 * grow)
	player_parts[4].position = Vector2(px + 20.0, py + 10.0 * grow)
	player_parts[5].position = Vector2(px + 10.0, py + 40.0 * grow)
	player_parts[6].position = Vector2(px + 20.0, py + 40.0 * grow)
	player_parts[7].position = Vector2(px + 8.0, py + 52.0 * grow)
	player_parts[8].position = Vector2(px + 20.0, py + 52.0 * grow)


func _enemy_x(enemy: Vector3) -> float:
	var span := enemy.y - enemy.x
	var cycle := span * 2.0
	var phase := fposmod(elapsed * enemy.z, cycle)
	if phase <= span:
		return enemy.x + phase
	return enemy.y - (phase - span)


func _place_enemies() -> void:
	for i in enemies.size():
		if stomped[i]:
			enemy_nodes[i].visible = false
			continue
		var ex := _enemy_x(enemies[i])
		enemy_nodes[i].position = Vector2(ex, GROUND_Y - 24.0)


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


func _collect_powers() -> void:
	for i in power_xs.size():
		if power_taken[i]:
			continue
		if absf(x - power_xs[i]) < 28.0 and absf(y - GROUND_Y) < 90.0:
			power_taken[i] = true
			power_nodes[i].visible = false
			if power_grow[i]:
				form = maxi(form, 1)
			else:
				form = 2
				star_timer = 3.2
			_update_hud()


func _bump_enemies() -> void:
	for i in enemies.size():
		if stomped[i]:
			continue
		var ex := _enemy_x(enemies[i])
		if absf(x - ex) > 26.0:
			continue
		if not on_ground or form == 2:
			stomped[i] = true
			enemy_nodes[i].visible = false
		elif form >= 1:
			form = 0
		else:
			x = _safe_x()
			y = GROUND_Y
			vy = 0.0
			on_ground = true


func _update_hud() -> void:
	if finished:
		hud.text = "Ganhaste!\nMoedas %d/%d" % [coins, COINS_TARGET]
		return
	var extra := ""
	if form == 2:
		extra = "\nEstrela!"
	elif form == 1:
		extra = "\nCresceste!"
	hud.text = "Segura à direita para avançar\nDesliza para cima: saltar\nMoedas %d/%d%s" % [coins, COINS_TARGET, extra]
