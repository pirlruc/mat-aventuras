extends Node2D

## Age-7 Super Off Road-style 2.5D dirt circuit. HUD is pt-PT.
const STEER_LEFT_MAX := 0.34
const STEER_RIGHT_MIN := 0.66
const LAPS_TARGET := 3
const GATES_TARGET := 8
const LENGTH := 480.0
const CRUISE := 28.0
const BOOST_SPD := 46.0
const STRIPS := 28

var distance := 2.0
var lateral := 0.0
var speed := CRUISE
var steer := 0.0
var boosting := false
var boost_timer := 0.0
var lap := 0
var gates := 0
var gate_mask := 0
var finished := false
var hud: Label
var curves: Array[float] = []
var hills: Array[float] = []
var widths: Array[float] = []
var palette := 0
var track_seed := 1
var pal_sky: Color = Color("81D4FA")
var pal_haze: Color = Color("4FC3F7")
var pal_mtn: Color = Color("2E7D32")
var pal_grass: Color = Color("43A047")
var pal_dirt: Color = Color("8D6E63")
var pal_line: Color = Color("FFF59D")
var kart_fill: Color = Color("FB8C00")


func _ready() -> void:
	track_seed = Time.get_ticks_msec()
	palette = track_seed % 4
	_roll_track()
	_cache_palette()
	kart_fill = Host.mascot_color()
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24, 24)
	hud.add_theme_font_size_override("font_size", 28)
	hud.add_theme_color_override("font_color", Color(1, 1, 1, 1))
	hud.add_theme_color_override("font_outline_color", Color(0, 0, 0, 1))
	hud.add_theme_constant_override("outline_size", 10)
	layer.add_child(hud)
	_update_hud(false)
	queue_redraw()


func _roll_track() -> void:
	var rng := RandomNumberGenerator.new()
	rng.seed = track_seed
	curves.clear()
	hills.clear()
	widths.clear()
	for i in 24:
		curves.append(rng.randf() * 1.7 - 0.85)
		hills.append(rng.randf() * 1.1 - 0.35)
		widths.append(0.82 + rng.randf() * 0.28)


func _cache_palette() -> void:
	var skies := [Color("81D4FA"), Color("FFCC80"), Color("FF8A65"), Color("1A237E")]
	var hazes := [Color("4FC3F7"), Color("FFB74D"), Color("E64A19"), Color("0D47A1")]
	var mtns := [Color("2E7D32"), Color("6D4C41"), Color("4E342E"), Color("263238")]
	var grass := [Color("43A047"), Color("D4E157"), Color("6D4C41"), Color("263238")]
	var dirt := [Color("8D6E63"), Color("BCAAA4"), Color("5D4037"), Color("4E342E")]
	var lines := [Color("FFF59D"), Color("FFFDE7"), Color("FFE082"), Color("EEEEEE")]
	pal_sky = skies[palette]
	pal_haze = hazes[palette]
	pal_mtn = mtns[palette]
	pal_grass = grass[palette]
	pal_dirt = dirt[palette]
	pal_line = lines[palette]


func _sample(values: Array[float], dist: float) -> float:
	var wrapped := fposmod(dist, LENGTH)
	var t := wrapped / LENGTH * values.size()
	var i := int(t) % values.size()
	var j := (i + 1) % values.size()
	var f := t - int(t)
	return values[i] + (values[j] - values[i]) * f


func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed:
			_steer_at(event.position, true)
		else:
			steer = 0.0
	elif event is InputEventMouseButton:
		if event.pressed:
			_steer_at(event.position, true)
		else:
			steer = 0.0
	elif event is InputEventScreenDrag:
		_steer_at(event.position, false)
	elif event is InputEventMouseMotion and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT):
		_steer_at(event.position, false)


func _steer_at(pos: Vector2, arm_boost: bool) -> void:
	var width: float = maxf(get_viewport().get_visible_rect().size.x, 1.0)
	var nx: float = pos.x / width
	if nx < STEER_LEFT_MAX:
		steer = -1.0
	elif nx > STEER_RIGHT_MIN:
		steer = 1.0
	else:
		steer = 0.0
		if arm_boost:
			boosting = true


func _process(delta: float) -> void:
	if finished:
		return
	delta = minf(delta, 0.05)
	if boosting:
		boost_timer = 1.15
	boosting = false
	boost_timer = maxf(boost_timer - delta, 0.0)
	var off := absf(lateral) > _sample(widths, distance)
	var target := BOOST_SPD if boost_timer > 0.0 else (12.0 if off else CRUISE)
	speed = move_toward(speed, target, 22.0 * delta)
	var curve := _sample(curves, distance)
	lateral = clampf(lateral + steer * 1.8 * delta + curve * speed * 0.012 * delta, -1.35, 1.35)
	distance += speed * delta
	if distance >= LENGTH:
		distance -= LENGTH
		lap += 1
		gate_mask = 0
		if lap >= LAPS_TARGET:
			_finish()
			return
	_collect_gates()
	_update_hud(boost_timer > 0.0)
	queue_redraw()


func _collect_gates() -> void:
	for i in GATES_TARGET:
		var bit := 1 << i
		if gate_mask & bit:
			continue
		var gate_at := LENGTH * float(i + 1) / float(GATES_TARGET + 1)
		if absf(distance - gate_at) <= 6.0 and absf(lateral) < 0.48:
			gate_mask |= bit
			gates = mini(gates + 1, GATES_TARGET)


func _draw() -> void:
	var size := get_viewport_rect().size
	draw_rect(Rect2(Vector2.ZERO, size), pal_sky)
	var horizon := size.y * 0.34
	var ground := size.y * 0.94
	_draw_hills(size, horizon)
	for i in range(STRIPS - 1, -1, -1):
		_draw_strip(size, horizon, ground, i)
	_draw_gates(size, horizon, ground)
	_draw_kart(size, ground)


func _draw_hills(size: Vector2, horizon: float) -> void:
	draw_rect(Rect2(0, horizon - 48.0, size.x, 52.0), pal_haze)
	for i in 4:
		var dist := distance + float(i) * 55.0
		var x := size.x * (0.08 + float(i) * 0.24) + _sample(curves, dist) * 28.0
		var peak := 26.0 + absf(_sample(hills, dist)) * 34.0
		draw_rect(Rect2(x, horizon - peak, 88.0, peak), pal_mtn)


func _draw_strip(size: Vector2, horizon: float, ground: float, index: int) -> void:
	var t := float(index) / float(STRIPS)
	var y := ground - (ground - horizon) * t
	var h := maxf((ground - horizon) / float(STRIPS), 2.0)
	var scale := 1.55 / (0.35 + t * 2.4)
	var dist := distance + 6.0 + t * 110.0
	var curve := _sample(curves, dist)
	var hill := _sample(hills, dist) * (1.0 - t) * 36.0
	var center := size.x * 0.5 - lateral * 95.0 * scale + curve * 62.0 * (1.0 - t)
	var road_w := 360.0 * scale * _sample(widths, dist)
	draw_rect(Rect2(0, y - hill, size.x, h + 1.0), pal_grass)
	draw_rect(Rect2(center - road_w * 0.5 - 10.0, y - hill, road_w + 20.0, h + 1.0), Color("E53935"))
	draw_rect(Rect2(center - road_w * 0.5, y - hill, road_w, h + 1.0), pal_dirt)
	if index % 2 == 0:
		draw_rect(Rect2(center - 4.0, y - hill, 8.0, h), pal_line)


func _draw_gates(size: Vector2, horizon: float, ground: float) -> void:
	for i in GATES_TARGET:
		if gate_mask & (1 << i):
			continue
		var gate_at := LENGTH * float(i + 1) / float(GATES_TARGET + 1)
		var ahead := gate_at - distance
		if ahead < 0.0:
			ahead += LENGTH
		if ahead > 90.0:
			continue
		var t := clampf(ahead / 90.0, 0.0, 1.0)
		var y := ground - (ground - horizon) * t - 20.0
		var scale := 1.4 / (0.4 + t * 2.0)
		var x := size.x * 0.5 - 18.0 * scale
		var bar_w := 36.0 * scale
		var bar_h := 10.0 * scale
		var post_w := 6.0 * scale
		var post_h := 26.0 * scale
		draw_rect(Rect2(x, y - post_h, post_w, post_h), Color("5D4037"))
		draw_rect(Rect2(x + bar_w - post_w, y - post_h, post_w, post_h), Color("5D4037"))
		draw_rect(Rect2(x, y, bar_w, bar_h), Color("FFD54F"))


func _draw_kart(size: Vector2, ground: float) -> void:
	var cx := size.x * 0.5 + steer * 18.0
	var y := ground - 78.0
	var fill := kart_fill
	draw_rect(Rect2(cx - 36.0, y + 40.0, 20.0, 18.0), Color("212121"))
	draw_rect(Rect2(cx + 16.0, y + 40.0, 20.0, 18.0), Color("212121"))
	draw_rect(Rect2(cx - 30.0, y + 16.0, 60.0, 30.0), fill)
	draw_rect(Rect2(cx - 8.0, y + 20.0, 16.0, 22.0), Color("FFF59D"))
	draw_rect(Rect2(cx - 18.0, y - 2.0, 36.0, 24.0), Color("ECEFF1"))
	draw_rect(Rect2(cx - 24.0, y - 12.0, 48.0, 12.0), fill)
	draw_rect(Rect2(cx - 20.0, y - 20.0, 8.0, 16.0), fill)
	draw_rect(Rect2(cx + 12.0, y - 20.0, 8.0, 16.0), fill)
	draw_rect(Rect2(cx - 26.0, y + 12.0, 10.0, 8.0), Color("FFF176"))
	draw_rect(Rect2(cx + 16.0, y + 12.0, 10.0, 8.0), Color("FFF176"))
	if boost_timer > 0.0:
		draw_rect(Rect2(cx - 10.0, y + 52.0, 20.0, 16.0), Color("FF6F00"))
		draw_rect(Rect2(cx - 28.0, y + 58.0, 12.0, 10.0), Color("FFCC80"))
		draw_rect(Rect2(cx + 16.0, y + 58.0, 12.0, 10.0), Color("FFCC80"))


func _update_hud(show_boost: bool) -> void:
	var shown := mini(lap + 1, LAPS_TARGET)
	var extra := "\nImpulso!" if show_boost else ""
	if absf(lateral) > _sample(widths, distance):
		extra = "\nVolta à pista!"
	hud.text = "Volta %d de %d\nPortões %d/%d%s" % [shown, LAPS_TARGET, gates, GATES_TARGET, extra]


func _finish() -> void:
	finished = true
	Host.finish(true)
