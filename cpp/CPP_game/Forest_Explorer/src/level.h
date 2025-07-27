#pragma once

#include "gameobject.h"
#include <vector>
#include <list>
#include <string>
#include <sgg/graphics.h>
#include "player.h"
#include "enemy.h"
#include "Coins.h"
#include "blocks.h"
#include "gamestate.h"
#include "UI.h"


class Level : public GameObject
{
	graphics::Brush m_brush_background;
	
	graphics::Brush start;
	graphics::Brush op;
	graphics::Brush cre;
	graphics::Brush brush_exit;

	graphics::Brush brush_portal;

	graphics::Brush text_br;

	std::vector<Enemy*> enemies;
	
	int number_of_enemies = 4;
		
	UI* mouse = new UI();

	Block* blocks = nullptr;
	
	Enemy* enemy = nullptr;
	
	Coins* coin = nullptr;
	
	void drawStartScreen();
	void updateStartingScreen(float dt);

	void drawCredits();
	void drawOptions();

	void drawPlayingScreen();
	void updatePlayingScreen(float dt);
	
	void drawFinalScreen();
	
	void spawn_blocks();
	void spawn_enemies();
	void spawn_coins();

	void checkCollisions();
	void checkEnemy();
	void checkPlayer();

public:

	Level(const std::string& name = "Level0");
	~Level() override;

	void update(float dt) override;
	void draw() override;
	void init() override;
};