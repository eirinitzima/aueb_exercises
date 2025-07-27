#include "Coins.h"
#include "util.h"
#include <sgg/graphics.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <stdexcept>
#include <cmath>


void Coins::coins_collision()
{
	for (auto block : m_coins){
		if (m_state->getPlayer()->intersectSideways(*block)){
			graphics::playSound(m_state->getFullAssetPath("coins.wav"), 1.0f);
			m_state->addScore(50.0f);
			delete block;
		}
	}
}

void Coins::draw_coins(int i)
{
	Box coins = *m_coins[i];
	float x = coins.m_pos_x;
	float y = coins.m_pos_y;
	float w = coins.m_width / 2.0f;
	float h = coins.m_height / 2.0f;
	
	sum+= graphics::getDeltaTime();

	if (sum > 1000.0f) {
		sprite++;
		sum = 0;
	}
	if (sprite == 6)
		sprite = 0;

	coins_brush.texture = m_sprites_coins[sprite];
	
	graphics::drawRect(x, y, w, h, coins_brush);

	if (m_state->m_debugging)
		graphics::drawRect(x, y, w, h, debug_brush_coins);
}



void Coins::init()
{
	try {
		std::ifstream positions(m_state->getFullAssetPath("coins_pos.txt"));
		
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

			Box* new_box = new Box(values[0], values[1], values[2], values[3]);
			m_coins.push_back(new_box);
		}

		positions.close();
	}
	catch (const std::runtime_error& e) { 
		std::cerr << e.what() << std::endl;
	}
	
	coins_brush.outline_opacity = 0.0f;
	coins_brush.fill_opacity = 1.0f;


	SETCOLOR(debug_brush_coins.fill_color, 1.0f, 0.3f, 0.0f);
	SETCOLOR(debug_brush_coins.outline_color, 1.0f, 0.1f, 0.0f);
	debug_brush_coins.fill_opacity = 0.1f;
	debug_brush_coins.outline_opacity = 1.0f;
	

	m_sprites_coins.push_back(m_state->getFullAssetPath("coin1.png"));
	m_sprites_coins.push_back(m_state->getFullAssetPath("coin2.png"));
	m_sprites_coins.push_back(m_state->getFullAssetPath("coin3.png"));
	m_sprites_coins.push_back(m_state->getFullAssetPath("coin4.png"));
	m_sprites_coins.push_back(m_state->getFullAssetPath("coin5.png"));
	m_sprites_coins.push_back(m_state->getFullAssetPath("coin6.png"));
	
}

void Coins::draw()
{
	for (int i = 0; i < m_coins.size(); i++)
		draw_coins(i);
}

Coins::Coins()
{
	init();
}