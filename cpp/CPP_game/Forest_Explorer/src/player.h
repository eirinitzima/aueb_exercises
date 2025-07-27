#pragma once

#include "gameobject.h"
#include <sgg/graphics.h>
#include "box.h"

class Player : public Box, public GameObject
{
	std::vector<std::string> m_sprites;

	graphics::Brush m_brush_player;

	const float m_accel_horizontal = 20.0f;
	const float m_accel_vertical = 300.1f;
	const float m_max_velocity = 5.0f;
	const float m_gravity = 10.0f;
	float life=1.0f;

public:
	
	float m_vx = 0.0f;
	float m_vy = 0.0f;


public:

	Player(std::string name) : GameObject(name) {}
	~Player();

	void update(float dt) override;
	void draw() override;
	void init() override;

	float getPositionX() const { return m_pos_x; }
	float getPositionY() const { return m_pos_y; }
	float getRemainingLife() const { return life; }
	void drainLife(float amount) { life = std::max<float>(0.0f, life - amount); }
	
protected:

	void debugDraw();
	void movePlayer(float dt);
};
