extends Node2D

## Age-3 Game Boy-style platformer. Tap to jump. HUD is pt-PT.
const COINS_TARGET := 5
const GROUND_Y := 520.0
const PIT_LEFT := 560.0
const PIT_RIGHT := 720.0
const SPEED := 180.0
const GRAVITY := 980.0
const JUMP_V := -420.0

var x := 80.0
var y := GROUND_Y
var vy := 0.0
var coins := 0
var jumping := false
var finished := false
var on_ground := true
var player: ColorRect
var hat: ColorRect
var hud: Label
var coin_nodes: Array[ColorRect] = []
var coin_xs: Array[float] = [220.0, 360.0, 500.0, 860.0, 1040.0]
var taken: Array[bool] = [false, false, false, false, false]
var ledges: Array[Rect2] = [Rect2(280, 400, 160, 24), Rect2(800, 360, 160, 24)]


func _ready() -> void:
	var sky := ColorRect.new()
	sky.color = Color("7EC0ED")
	sky.size = Vector2(1280, 720)
	add_child(sky)
	var band := ColorRect.new()
	band.color = Color("5BA3D9")
	band.position = Vector2(0, 320)
	band.size = Vector2(1280, 200)
	add_child(band)

	_add_brick(0, GROUND_Y, PIT_LEFT, 24, Color("3D9E2F"))
	_add_brick(PIT_RIGHT, GROUND_Y, 1280.0 - PIT_RIGHT, 24, Color("3D9E2F"))
	_add_brick(0, GROUND_Y + 24.0, PIT_LEFT, 176, Color("C75A1A"))
	_add_brick(PIT_RIGHT, GROUND_Y + 24.0, 1280.0 - PIT_RIGHT, 176, Color("C75A1A"))
	_add_brick(PIT_LEFT, GROUND_Y, PIT_RIGHT - PIT_LEFT, 200, Color("1A0A08"))
	for ledge in ledges:
		_add_brick(ledge.position.x, ledge.position.y, ledge.size.x, ledge.size.y, Color("C75A1A"))

	var fill := Host.mascot_color()
	player = ColorRect.new()
	player.color = fill
	player.size = Vector2(36, 40)
	add_child(player)
	hat = ColorRect.new()
	hat.color = fill.darkened(0.35)
	hat.size = Vector2(28, 14)
	add_child(hat)

	for cx in coin_xs:
		var coin := ColorRect.new()
		coin.color = Color("FFD54F")
		coin.size = Vector2(18, 18)
		coin.position = Vector2(cx, GROUND_Y - 48.0)
		add_child(coin)
		coin_nodes.append(coin)

	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24, 24)
	hud.add_theme_font_size_override("font_size", 28)
	layer.add_child(hud)
	_update_hud()


func _add_brick(px: float, py: float, w: float, h: float, color: Color) -> void:
	var brick := ColorRect.new()
	brick.color = color
	brick.position = Vector2(px, py)
	brick.size = Vector2(w, h)
	add_child(brick)


func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch and event.pressed:
		jumping = true
	elif event is InputEventMouseButton and event.pressed:
		jumping = true


func _process(delta: float) -> void:
	if finished:
		return
	if jumping and on_ground:
		vy = JUMP_V
		on_ground = false
	jumping = false
	vy += GRAVITY * delta
	y += vy * delta
	x += SPEED * delta
	_land()
	if y > 780.0:
		x = PIT_LEFT - 80.0
		y = GROUND_Y
		vy = 0.0
		on_ground = true
	player.position = Vector2(x, y - 40.0)
	hat.position = Vector2(x + 4.0, y - 52.0)
	_collect_coins()


func _land() -> void:
	on_ground = false
	var in_pit := x + 18.0 > PIT_LEFT and x < PIT_RIGHT
	if y >= GROUND_Y and not in_pit:
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


func _collect_coins() -> void:
	for i in coin_xs.size():
		if taken[i]:
			continue
		if absf(x - coin_xs[i]) < 28.0 and absf(y - GROUND_Y) < 80.0:
			taken[i] = true
			coin_nodes[i].visible = false
			coins = mini(coins + 1, COINS_TARGET)
			_update_hud()
			if coins >= COINS_TARGET:
				finished = true
				Host.finish(true)


func _update_hud() -> void:
	hud.text = "Moedas %d/%d" % [coins, COINS_TARGET]
