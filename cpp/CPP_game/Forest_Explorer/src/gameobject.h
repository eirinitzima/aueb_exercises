#pragma once
#include <string>
#include "gamestate.h"

class GameObject 
{
	static int m_next_id;
	
protected:
	
	std::string m_name;
	int m_id = 0;
	bool m_active = true;
	int choice = 0;

public:

	class GameState* m_state;
	GameObject(const std::string& name = "");
	virtual ~GameObject() {}

	virtual void update(float dt) {}
	virtual void init() {}
	virtual void draw() {}
	

	bool isActive() const { return m_active; }
	int getChoice() const { return choice; }
	void setChoise(int a) { choice = a; }
	void setActive(bool a) { m_active = a; }
};
