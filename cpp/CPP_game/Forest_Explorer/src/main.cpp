#include <sgg/graphics.h>
#include "gamestate.h"

void draw()
{
    GameState::getInstance()->draw();
}

void update(float dt)
{
    GameState::getInstance()->update(dt);
}
enum scale_mode_t {
    CANVAS_SCALE_WINDOW,
    CANVAS_SCALE_STRETCH,
    CANVAS_SCALE_FIT
};

int main(int argc, char** argv)
{
    graphics::createWindow(GameState::getInstance()->getWindowWidth(), GameState::getInstance()->getWindowHeight(), "FOREST EXPLORER ");
    
    GameState::getInstance()->init();

    graphics::setDrawFunction(draw);
    graphics::setUpdateFunction(update);

    graphics::setCanvasSize(GameState::getInstance()->getCanvasWidth(), GameState::getInstance()->getCanvasHeight());
    graphics::setCanvasScaleMode(graphics::CANVAS_SCALE_FIT);
 
    graphics::startMessageLoop();
    GameState::getInstance()->~GameState();
    delete  GameState::getInstance();
    graphics::destroyWindow();

	return 0;
}

