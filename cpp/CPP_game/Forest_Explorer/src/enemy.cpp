#include "enemy.h"
#include "util.h"
#include <sgg/graphics.h>
#include <random>
#include <math.h>
#include <algorithm>




void Enemy::update(float dt)
{
	float delta_time = dt / 1000.0f;
	
	m_pos_x += speed * delta_time;

	if (m_pos_x > m_state->getCanvasWidth())
		active = false;
}

void Enemy::init()
{
	speed = 1.0f;
	m_pos_x = (m_state->getCanvasWidth()/15.0f + 1.0f * size)  ;
	m_pos_y = 5.0f * rand() / (float)RAND_MAX;
	size = 100.0f+50.0f * rand() / (float)RAND_MAX;
	enemy_brush.outline_opacity = 0.0f;
	enemy_brush.fill_opacity = 1.0f;
	m_sprites.push_back(m_state->getFullAssetPath("raven1.png"));
	m_sprites.push_back(m_state->getFullAssetPath("raven2.png"));
	m_height = 0.5f;
	m_width = 0.5f;
}

void Enemy::draw()
{
	enemy_brush.texture = m_sprites[sprite];
	sum += graphics::getDeltaTime();
	if (sum > 500.0f) {
		sprite++;
		sum = 0;
	}
	if (sprite == 2)
		sprite = 0;
	
	graphics::drawRect(m_pos_x, m_pos_y, 1.3f, m_height+0.5f, enemy_brush);

	if (m_state->m_debugging)
		debugDraw();
}

void Enemy::debugDraw()
{

	graphics::Brush debug_brush_enemy;
	SETCOLOR(debug_brush_enemy.fill_color, 1.0f, 0.3f, 0.0f);
	SETCOLOR(debug_brush_enemy.outline_color, 1.0f, 0.1f, 0.0f);
	debug_brush_enemy.fill_opacity = 0.1f;
	debug_brush_enemy.outline_opacity = 1.0f;
	graphics::drawRect(m_pos_x , m_pos_y , m_width, m_height, debug_brush_enemy);

	char s[20];
	sprintf_s(s, "(%5.2f,%5.2f)", m_pos_x, m_pos_y);
	SETCOLOR(debug_brush_enemy.fill_color, 1.0f, 0.0f, 0.0f);
	debug_brush_enemy.fill_opacity = 1.0f;
	graphics::drawText(m_pos_x-0.4f, m_pos_y-0.6f, 0.15f , s, debug_brush_enemy);
}

Enemy::Enemy() {
	init();
}
