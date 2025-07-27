#pragma once
#include "box.h"
#include <string>
#define WINDOW_WIDTH 1450
#define WINDOW_HEIGHT 700

class GameState
{	
	static GameState* m_unique_instance;

	const std::string m_asset_path = "assets\\";

	const float m_canvas_width  = 10.0f;
	const float m_canvas_height = 6.0f;
	
	class Level * m_current_level = 0;
	class Player* m_player = 0;
	
	float score = 0.0f;

	GameState();

public:
	Box* play = new Box(8.45f, 2.0f, 2.4f, 0.9f);
	Box* options = new Box(8.45f, 3.38f, 2.4f, 0.9f);
	Box* credits = new Box(8.45f, 5.0f, 2.4f, 0.9f);
	Box* exit = new Box(9.46f, 0.5f, 0.3f, 0.3f);
	Box* portal = new Box(9.46f, 0.5f, 1.0f, 1.0f);

	typedef enum { STATUS_START, STATUS_PLAYING, FINAL_SCREEN,STATUS_OPTIONS,STATUS_CREDITS} status_t;
	status_t status = STATUS_START;
	float m_global_offset_x = 0.0f;
	float m_global_offset_y = 0.0f;
	
	bool m_debugging = false;
	int choice = 0;

public:

	~GameState();
	static GameState* getInstance();


	int  getChoice() const { return choice; }
	void  setChoise(int a) { choice = a; }

	status_t getStatus() const { return status; }
	bool init();
	void draw();
	void update(float dt);

	std::string getFullAssetPath(const std::string& asset) const { return m_asset_path + asset; }
	std::string getAssetDir() const { return m_asset_path;}

	float getCanvasWidth() const { return m_canvas_width; }
	float getCanvasHeight() const { return m_canvas_height; }

	float getWindowWidth() const { return WINDOW_WIDTH; }
	float getWindowHeight() const { return WINDOW_HEIGHT; }

	float getScrore() const { return score; }
	void addScore(float amount) { score += amount; }

	class Player* getPlayer() const { return m_player; }
};
