#pragma once
#include "gameobject.h"
#include <sgg/graphics.h>
#include "box.h"



class UI : public Box, public GameObject
{
	graphics::Brush br;
	graphics::MouseState ms;

	float mouse_pos_x=0.0f;
	float mouse_pos_y=0.0f;
	const float mouse_width=0.1f;
	const float mouse_height=0.1f;

public:

	UI();
	void update(float dt) override;
	void drawButton();
	bool checkCollision (Box& botton);
	bool clickButton() const { return ms.button_left_pressed;}

	float getPosX() const { return mouse_pos_x; }
	float getPoxY() const { return mouse_pos_y; }
	float getWidthMouse() const { return mouse_width; }
	float getHeightMouse() const { return mouse_height; }
};
