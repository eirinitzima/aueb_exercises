#include "level.h"
#include <sgg/graphics.h>
#include "player.h"
#include "util.h"
#include <iostream>
#include "enemy.h"
#include "box.h"
#include "gamestate.h"
#include "gameobject.h"
#include <thread>
#include <chrono>




void Level::updatePlayingScreen(float dt)
{
	checkEnemy();
	checkPlayer();

	spawn_coins();
	spawn_blocks();

	for (int i = 0; i < number_of_enemies; i++) 
		spawn_enemies();
		
	if (m_state->getPlayer()->isActive())
			m_state->getPlayer()->update(dt);

	for (int i = 0; i < number_of_enemies; i++) {
		if (enemies[i])
			enemies[i]->update(dt);
	}

	checkCollisions();

	if (m_state->getPlayer()->intersectSideways(*(m_state->portal))) {
		graphics::playSound(m_state->getFullAssetPath("clapping.wav"), 1.0f);
		m_state->getPlayer()->setActive(false);
	}

	GameObject::update(dt);
}

void Level::updateStartingScreen(float dt)
{	
	if (mouse->checkCollision(*(m_state->play)) && mouse->clickButton())
			m_state->setChoise(1);

	if (mouse->checkCollision(*(m_state->options)) && mouse->clickButton())
			m_state->setChoise(2);
	
	if (mouse->checkCollision(*(m_state->credits)) && mouse->clickButton())
			m_state->setChoise(3);	
}


void Level::drawFinalScreen()
{
	graphics::Brush br;
	br.outline_opacity = 0.0f;
	br.texture = m_state->getFullAssetPath("final.png");
	graphics::drawRect(5.0f, 3.0f, m_state->getCanvasWidth(), m_state->getCanvasHeight(), br);
	float glow = 0.5f + 0.5f * sinf(graphics::getGlobalTime() / 100.0f);
	br.fill_color[0] = 0.3f;
	br.fill_color[1] = 0.3f;
	br.fill_color[2] = 0.5f + glow * 0.5f;
	char info[40];
	std::string player_score = "Final Score: " + std::to_string(static_cast<int>(m_state->getScrore()));
	sprintf_s(info, "%s", player_score.c_str());
	graphics::drawText( 3.5f, 2.5f, 0.8f, info, br);
	
	graphics::Brush img_br;
	img_br.outline_opacity = 0.0f;
	
	if (m_state->getScrore() == 0.0f) {
		img_br.texture = m_state->getFullAssetPath("star_0.png");
		graphics::drawRect(m_state->getCanvasWidth() / 2.0f, 4.0f, 2.5f, 2.0f, img_br);
	}															   
	else if (m_state->getScrore() <= 200.0f) {					   
		img_br.texture = m_state->getFullAssetPath("star_1.png");  
		graphics::drawRect(m_state->getCanvasWidth() / 2.0f, 4.0f, 2.5f, 2.0f, img_br);
	}															   
	else if (m_state->getScrore() <= 500.0f) {					   
		img_br.texture = m_state->getFullAssetPath("star_2.png");  
		graphics::drawRect(m_state->getCanvasWidth() / 2.0f, 4.0f, 2.5f, 2.0f, img_br);
	}															   
	else {														   
		img_br.texture = m_state->getFullAssetPath("star_3.png");  
		graphics::drawRect(m_state->getCanvasWidth() / 2.0f, 4.0f, 2.5f, 2.0f, img_br);
	}

}

void Level::drawStartScreen()
{
	graphics::drawRect(5.0f, 3.0f, m_state->getCanvasWidth(), m_state->getCanvasHeight(), start);

	graphics::Brush gr;
	float glow = 0.5f + 0.5f * sinf(graphics::getGlobalTime() / 100.0f);
	gr.fill_color[0] = 0.3f;
	gr.fill_color[1] = 0.3f;
	gr.fill_color[2] = 0.5f + glow * 0.5f;
	
	char info[40];
	sprintf_s(info, "FOREST");
	graphics::drawText(0.7f,4.5f, 0.8f, info, gr);
	sprintf_s(info, "EXPLORER");
	graphics::drawText(0.5f, 5.3f, 0.8f, info, gr);
	
	if (m_state->m_debugging)
		mouse->drawButton();

	gr.fill_opacity = 0.0f;
	gr.outline_opacity = 0.0f;

	sprintf_s(info, "PLAY");
	graphics::drawText(m_state->play->m_pos_x-0.4f, m_state->play->m_pos_y, 0.5f, info, text_br);
	
	sprintf_s(info, "OPTIONS");
	graphics::drawText(m_state->options->m_pos_x-0.6f, m_state->options->m_pos_y+0.2f, 0.5f, info, text_br);
	
	sprintf_s(info, "CREDITS");
	graphics::drawText(m_state->credits->m_pos_x-0.6f, m_state->credits->m_pos_y+0.2f, 0.5f, info, text_br);
}

void Level::drawCredits()
{
	if (m_state->m_debugging)
		mouse->drawButton();
	
	if (mouse->checkCollision(*(m_state->exit)) && mouse->clickButton()) 
		m_state->setChoise(4);
	
	graphics::drawRect(5.0f, 3.0f, m_state->getCanvasWidth(), m_state->getCanvasHeight(), cre);

	graphics::drawRect(m_state->exit->m_pos_x, m_state->exit->m_pos_y, m_state->exit->m_width*2.5f, m_state->exit->m_height*2.5f, brush_exit);

	char info[40];
	sprintf_s(info, "EIRINI TZIMA");
	graphics::drawText(1.75f, 2.9f, 0.3f, info, text_br);
	sprintf_s(info, "3220201");
	graphics::drawText(1.8f, 3.2f, 0.4f, info, text_br);
	sprintf_s(info, "FWKIWN KONTALEXIS");
	graphics::drawText(6.5f, 2.9f, 0.3f, info, text_br);
	sprintf_s(info, "3220083");
	graphics::drawText(6.9f, 3.2f, 0.4f, info, text_br);

}

void Level::drawOptions()
{
	if (m_state->m_debugging)
		mouse->drawButton();

	if (mouse->checkCollision(*(m_state->exit)) && mouse->clickButton()) 
		m_state->setChoise(4);
	
	graphics::drawRect(5, 3, m_state->getCanvasWidth(), m_state->getCanvasHeight(), op);
	graphics::drawRect(m_state->exit->m_pos_x, m_state->exit->m_pos_y, m_state->exit->m_width*2.5f, m_state->exit->m_height*2.5f, brush_exit);
	char info[40];
	sprintf_s(info, "MOVE PLAYER");
	graphics::drawText(1.2f, 3.50f, 0.6f, info, text_br);
}



void Level::drawPlayingScreen()
{	
	if (m_state->m_debugging)
		mouse->drawButton();

	//draw background
	graphics::drawRect(5.0f, 3.0f, m_state->getCanvasWidth(), m_state->getCanvasHeight(), m_brush_background);

	//draw portal
	brush_portal.outline_opacity = 0.0f;
	graphics::drawRect(9.5f, 1.0f, m_state->portal->m_width, m_state->portal->m_height, brush_portal);
	brush_portal.texture=m_state->getFullAssetPath("portal.png");
	
	//draw score
	graphics::Brush br;
	br.outline_opacity = 0.0f;
	br.texture= m_state->getFullAssetPath("score.png");
	graphics::drawRect(0.3f, 0.3f, 0.7f, 0.7f, br);
	char info[40];
	std::string player_score = std::to_string(static_cast<int>(m_state->getScrore()));
	sprintf_s(info, "%s", player_score.c_str());
	graphics::drawText(0.6f, 0.4f, 0.3f, info, br);

	// draw player
	if (m_state->getPlayer()->isActive())
		m_state->getPlayer()->draw();

	// draw blocks
	if (blocks)
		blocks->draw();

	//draw coins
	if (coin) 
		coin->draw();

	//draw enemies
	for (int i = 0; i < number_of_enemies; i++) {
		if (enemies[i])
			enemies[i]->draw();
	}
	
	//draw lifebar
	float player_life = m_state->getPlayer() ? m_state->getPlayer()->getRemainingLife() : 0.0f;

	br.outline_opacity = 0.0f;
	br.fill_color[0] = 0.0f;  
	br.fill_color[1] = 0.5f * (1.0f - player_life) + player_life * 0.2f;
	br.fill_color[2] = 0.5f * (1.0f - player_life) + player_life * 0.2f;
	br.texture = "";
	br.fill_secondary_color[0] = 0.0f;
	br.fill_secondary_color[1] = 0.5f * (1.0f - player_life) + player_life * 0.5f;
	br.fill_secondary_color[2] = 0.5f * (1.0f - player_life) + player_life * 0.5f;
	br.gradient = true;
	br.gradient_dir_u = 1.0f;
	br.gradient_dir_v = 0.0f;
	graphics::drawRect(9.2f-((1.0f-player_life)*1.4f/2), 0.2f, player_life * 1.4f, 0.2f, br);

	br.outline_opacity = 1.0f; 
	br.gradient = false;
	br.fill_opacity = 0.0f;
	graphics::drawRect(9.2f, 0.2f, 1.4f, 0.2f, br);
}

void Level::spawn_blocks()
{
	if (!blocks) 
		blocks = new Block();
}

void Level::spawn_enemies()
{
	for (int i = 0; i < number_of_enemies; i++) {
		if (!enemies[i])
			enemies[i] = new Enemy();
	}
}

void Level::spawn_coins()
{
	if (!coin) 
		coin = new Coins();
}



void Level::checkCollisions()
{
	blocks->coll();
	coin->coins_collision();

	//Player collisions with enemies
	for (int i = 0; i < number_of_enemies; i++) {
		
		if (m_state->getPlayer()->intersectSideways(*enemies[i])){
			graphics::playSound(m_state->getFullAssetPath("ouch.wav"), 1.0f);
			m_state->getPlayer()->drainLife(0.01f);
			break;
		}
		if (m_state->getPlayer()->intersectDown(*enemies[i])) {
			graphics::playSound(m_state->getFullAssetPath("ouch.wav"), 1.0f);
			m_state->getPlayer()->drainLife(0.01f);
			break;
		}
		if (enemies[i]->intersectSideways(*(m_state->getPlayer()))) {
			graphics::playSound(m_state->getFullAssetPath("ouch.wav"), 1.0f);
			m_state->getPlayer()->drainLife(0.01f);
			break;
		}
		if (enemies[i]->intersectDown(*(m_state->getPlayer()))) {
			graphics::playSound(m_state->getFullAssetPath("crow.wav"), 1.0f);
			m_state->addScore(100.0f);
			delete enemies[i]; 
			enemies[i] = nullptr;
			number_of_enemies -= 1;
			break;
		}
	}
}


void Level::checkEnemy()
{
	for (int i = 0; i < number_of_enemies; i++) {
		if (enemies[i] && !enemies[i]->isActive()){	
			delete enemies[i];
			enemies[i] = nullptr;
		}
	}	
}

void Level::checkPlayer()
{
	if (m_state->getPlayer() && m_state->getPlayer()->isActive() && m_state->getPlayer()->getRemainingLife() == 0.0f) 
		m_state->getPlayer()->setActive(false);
}

void Level::update(float dt)
{
	if (m_state->getStatus() == GameState::STATUS_PLAYING) {
		updatePlayingScreen(dt);
	}
	if (m_state->getStatus() == GameState::STATUS_START) {
		updateStartingScreen(dt);
	}
	mouse->update(dt);
}



void Level::draw()
{

	if (m_state->getStatus() == GameState::STATUS_START) {
		drawStartScreen();
	}
	if (m_state->getStatus() == GameState::STATUS_PLAYING) {
		drawPlayingScreen();
	}
	if (m_state->getStatus() == GameState::STATUS_CREDITS) {
		drawCredits();
	} 
	if (m_state->getStatus() == GameState::STATUS_OPTIONS) {
		drawOptions();
	} 
	if (m_state->getStatus() == GameState::FINAL_SCREEN) {
		drawFinalScreen();
	}
}

void Level::init()
{
	graphics::playSound(m_state->getFullAssetPath("forest_sound.wav"), 0.8f);
	
	for (int i = 0; i < number_of_enemies; i++) 
		enemies.push_back(nullptr);
	
	text_br.fill_color[0] = 0.0f;
	text_br.fill_color[1] = 0.0f;
	text_br.fill_color[2] = 0.0f;
}

Level::Level(const std::string & name) : GameObject(name)
{
	m_brush_background.outline_opacity = 0.0f;
	m_brush_background.texture = m_state->getFullAssetPath("bkg.png");

	start.outline_opacity = 0.0f;
	start.texture = m_state->getFullAssetPath("start.png");

	
	op.outline_opacity = 0.0f;
	op.texture = m_state->getFullAssetPath("options.png");

	
	cre.outline_opacity = 0.0f;
	cre.texture = m_state->getFullAssetPath("credits.png");

	brush_exit.outline_opacity = 0.0f;
	brush_exit.texture= m_state->getFullAssetPath("exit.png");
}

Level::~Level()
{
	delete blocks;
	blocks = nullptr;
	delete coin;
	coin = nullptr;
	delete enemy;
	enemy = nullptr;
	delete mouse;
	mouse = nullptr;
}
