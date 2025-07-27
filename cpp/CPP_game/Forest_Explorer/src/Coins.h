#pragma once
#include "gameobject.h"
#include <sgg/graphics.h>
#include "box.h"
#include "player.h"


class Coins : public GameObject, public Box {

	graphics::Brush coins_brush;
	graphics::Brush debug_brush_coins;
	std::vector<std::string> m_sprites_coins;
	std::vector<Box*> m_coins;
	
	float size;
	bool active = true;
	int sprite = 0;
	float sum = 0.0f;

public:

	Coins();
	void init() override;
	void draw() override;

	void coins_collision();
	void draw_coins(int i);
	bool isActive() const { return active; }
};

