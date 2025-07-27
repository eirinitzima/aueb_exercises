#include "gamestate.h"
#include "level.h"
#include "player.h"
#include <thread>
#include <chrono>
#include "enemy.h"
#include "UI.h"
#include <math.h>
#include <algorithm>
#include "util.h"

using namespace std::chrono_literals;

GameState::GameState()
{
}


GameState::~GameState()
{
	if (m_current_level) {
		m_current_level->~Level();
		delete m_current_level;
		m_current_level = nullptr;
	}
	if (m_player){
		delete m_player;
		m_player=nullptr;
	}
	delete play;
	play = nullptr;
	delete options;
	options = nullptr;
	delete credits;
	credits = nullptr;
	delete exit;
	exit = nullptr;
	delete portal;
	portal = nullptr;
}

GameState* GameState::getInstance()
{
	if (!m_unique_instance)
	{
		m_unique_instance = new GameState();
	}
	return m_unique_instance;
}

bool GameState::init()
{
	m_current_level = new Level("lvl");
	m_current_level->init();
	
	m_player = new Player("Player");
	m_player->init();

	graphics::preloadBitmaps(getAssetDir());
	graphics::setFont(m_asset_path + "Pemanka.ttf");

	return true;
}

void GameState::draw()
{
	if (!m_current_level)
		return;

	m_current_level->draw();
}

void GameState::update(float dt)
{
	if (dt > 500) 
		return;
	
	float sleep_time = std::max(17.0f - dt, 0.0f);

	if (sleep_time > 0.0f)
		std::this_thread::sleep_for(std::chrono::duration<float, std::milli>(sleep_time));

	if (!m_current_level)
		return;

	m_current_level->update(dt);

	m_debugging = graphics::getKeyState(graphics::SCANCODE_0);

	if ( m_player->isActive()==false && status==STATUS_PLAYING) 
		status = FINAL_SCREEN;
	

	if (getChoice()==1) {
		status = STATUS_PLAYING;
		choice = 14;
	}
	if (getChoice() == 2) 
		status = STATUS_OPTIONS;
	
	if (getChoice() == 3) 
		status = STATUS_CREDITS;
	
	if (getChoice() == 4) 
		status = STATUS_START;
}

GameState* GameState::m_unique_instance = nullptr;