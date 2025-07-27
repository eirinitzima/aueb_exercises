#include "player.h"
#include "util.h"
#include <cmath>



void Player::update(float dt)
{
	float delta_time = dt / 1000.0f;

	movePlayer(dt);
	
	GameObject::update(dt);

}

void Player::draw()
{	
	int sprite = 0.0f;

	if (graphics::getKeyState(graphics::SCANCODE_RIGHT))
		sprite = (int)fmod(100.0f - m_pos_x * 8.0f, 8.0f);

	if (graphics::getKeyState(graphics::SCANCODE_LEFT))
		sprite = (int)fmod(100.0f - m_pos_x * 8.0f, 8.0f) + 8.0f;
	

	m_brush_player.texture = m_sprites[sprite];
	graphics::drawRect(m_pos_x, m_pos_y , 1.0f, 1.0f, m_brush_player);
	
	if (m_state->m_debugging)
		debugDraw();
}

void Player::init()
{
	m_pos_x = 3.0f;
	m_pos_y = 5.0f;
	
	m_brush_player.fill_opacity = 1.0f;
	m_brush_player.outline_opacity = 0.0f;

	m_brush_player.texture = m_state->getFullAssetPath("pump_start.png");

	m_sprites.push_back(m_state->getFullAssetPath("pump_right_0.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_right_1.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_right_2.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_right_3.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_right_4.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_right_5.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_right_6.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_right_7.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_0.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_1.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_2.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_3.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_4.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_5.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_6.png"));
	m_sprites.push_back(m_state->getFullAssetPath("pump_left_7.png"));
	
	m_width = 0.5f;
}

void Player::debugDraw()
{
	graphics::Brush debug_brush;
	SETCOLOR(debug_brush.fill_color, 1.0f, 0.3f, 0.0f);
	SETCOLOR(debug_brush.outline_color, 1.0f, 0.1f, 0.0f);
	debug_brush.fill_opacity = 0.1f;
	debug_brush.outline_opacity = 1.0f;
	graphics::drawRect(m_pos_x, m_pos_y, m_width, m_height, debug_brush);
	
	char s[20];
	sprintf_s(s,"(%5.2f, %5.2f)", m_pos_x, m_pos_y);
	SETCOLOR(debug_brush.fill_color, 1.0f, 0.0f, 0.0f);
	debug_brush.fill_opacity = 1.0f;
	graphics::drawText(m_pos_x - 0.4f, m_pos_y - 0.6f, 0.15f, s, debug_brush);
}

void Player::movePlayer(float dt)
{
	float delta_time = dt / 1000.0f;
	
	float move = 0.0f;
	
	if (graphics::getKeyState(graphics::SCANCODE_LEFT)) 
		move -= 1.0f;
	
	if (graphics::getKeyState(graphics::SCANCODE_RIGHT)) 
		move = 1.0f;
	
	m_vx = std::min<float>(m_max_velocity, m_vx + delta_time * move * m_accel_horizontal);
	m_vx = std::max<float>(-m_max_velocity, m_vx);

	m_vx -= 0.2f * m_vx / (0.1f + fabs(m_vx));

	if (fabs(m_vx) < 0.01f)
		m_vx = 0.0f;

	if (m_vy == 0.0f ) 
		m_vy -= (graphics::getKeyState(graphics::SCANCODE_UP) ? m_accel_vertical : 0.0f) * 0.02f;
		

	m_pos_x += m_vx * delta_time;
		
	m_vy += delta_time * m_gravity;

	m_pos_y += m_vy * delta_time;
	
	if (m_pos_x > m_state->getCanvasWidth()) {
		m_pos_x = m_state->getCanvasWidth();
		m_vx = 0.0f;
	}

	if (m_pos_x <0.0f) {
		m_pos_x = 0.0f;
		m_vx = 0.0f;
	}

	if (m_pos_y > m_state->getCanvasHeight()) {
		m_pos_y = m_state->getCanvasHeight();
		m_vy = 0.0f;
	}

	if (m_pos_y < 0.0f) {
		m_pos_y = 0.0f;
		m_vy = 0.0f;
	}
}

Player::~Player()
{
}

