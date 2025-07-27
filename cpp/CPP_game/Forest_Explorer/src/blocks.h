#pragma once
#include "gameobject.h"
#include <sgg/graphics.h>
#include "box.h"
#include "player.h"

class Block : public GameObject, public Box {

	graphics::Brush m_block_brush_debug;

	std::vector<Box> m_blocks;

	const float m_block_size = 1.0f;
	
	void drawBlock(int i);

public: 

	Block();
	void init() override;
	void draw() override;
	void coll();
};