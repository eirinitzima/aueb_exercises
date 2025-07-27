#pragma once
#include "gameobject.h"
#include <sgg/graphics.h>
#include "box.h"
#include "player.h"


class Enemy : public GameObject ,public Box
{
	graphics::Brush enemy_brush;
	std::vector<std::string> m_sprites;
	
	float speed;
	float size;
	bool active = true;
	int sprite = 0;
	float sum = 0.0f;

public:
	
	Enemy();
	void update(float dt) override;
	void init() override;
	void draw() override;
	
	float getPositionX() const { return m_pos_x; }
	float getPositionY() const { return m_pos_y; }
	float getWidth() const { return m_width; }
	float getHeight() const { return m_height; }
	bool isActive() const { return active; }
	void setActive(bool a) { active = a; }

protected:
	void debugDraw();
};
