#include "UI.h"
#include <sgg/graphics.h>
#include "util.h"




bool UI::checkCollision(Box &button)
{
	return (fabs(mouse_pos_x - button.m_pos_x) * 2.0f < (mouse_width + button.m_width)) &&
		(fabs(mouse_pos_y - button.m_pos_y) * 2.0f < (mouse_height + button.m_height));
}

void UI::update(float dt)
{
	graphics::getMouseState(ms);
	mouse_pos_x = graphics::windowToCanvasX(ms.cur_pos_x);
	mouse_pos_y= graphics::windowToCanvasY(ms.cur_pos_y);
}

void UI::drawButton()
{
	graphics::getMouseState(ms);
	char s[20];
	
	sprintf_s(s, "(%5.2f, %5.2f)", mouse_pos_x, mouse_pos_y);
	SETCOLOR(br.fill_color, 1.0f, 0.0f, 0.0f);
	br.fill_opacity = 1.0f;
	graphics::drawText(mouse_pos_x - 0.4f, mouse_pos_y - 0.6f, 0.15f, s, br);

	graphics::drawRect(mouse_pos_x,mouse_pos_y,0.1f,0.1f,br);
}

UI::UI()
{
	graphics::getMouseState(ms);
}
