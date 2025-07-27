#include "blocks.h"
#include "util.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <stdexcept> 


void Block::drawBlock(int i)
{
	Box& box = m_blocks[i];

	float x = box.m_pos_x;
	float y = box.m_pos_y;

	if (m_state->m_debugging) 
		graphics::drawRect(x, y, box.m_width, box.m_height, m_block_brush_debug);
}

void Block::init()
{
	try {
		std::ifstream positions(m_state->getFullAssetPath("sprite_positions.txt")); 
		if (!positions) {
			throw std::runtime_error("Error opening file with positions."); 
		}
	
		std::string line;
		while (std::getline(positions, line)) { 
			std::istringstream iss(line); 


			std::vector<float> values;

			std::string token;
			while (std::getline(iss, token, ',')) { 
				try {
					float value = std::stof(token); 
					values.push_back(value);
					
				}
				catch (const std::invalid_argument& e) {
					std::cerr << "Invalid float value: " << token << std::endl;
				}
			}

	
			m_blocks.push_back(Box(values[0], values[1], values[2], values[3]));
		}
		positions.close();
	} 
	catch (const std::runtime_error& e) { 
		std::cerr << e.what() << std::endl; 
	}
		
	m_block_brush_debug.fill_opacity = 0.1f;
	SETCOLOR(m_block_brush_debug.fill_color, 0.1f, 1.0f, 0.1f);
	SETCOLOR(m_block_brush_debug.outline_color, 0.3f, 1.0f, 0.2f);


}

void Block::draw(){

	for (int i = 0; i < m_blocks.size(); i++)
		drawBlock(i);
}

void Block::coll()
{
	for (auto& block : m_blocks){
		float offset = 0.0f;
		if (offset = m_state->getPlayer()->intersectDown(block)){
			m_state->getPlayer()->m_pos_y += offset;
			m_state->getPlayer()->m_vy = 0.0f;
			break;
		}
	}

	for (auto& block : m_blocks){
		float offset = 0.0f;
		if (offset = m_state->getPlayer()->intersectSideways(block)){
			m_state->getPlayer()->m_pos_y += offset;
			m_state->getPlayer()->m_vy = 0.0f;
			m_state->getPlayer()->m_pos_x += offset;
			m_state->getPlayer()->m_vx = 0.0f;
			break;
		}

	}

	for (auto& block : m_blocks){
		float offset = 0.0f;
		if (offset =block.intersectDown(*(m_state->getPlayer()))){
			m_state->getPlayer()->m_pos_y -= offset;
			m_state->getPlayer()->m_vy = 0.0f;
			break;
		}
	}

}

Block::Block()
{
	init();
}