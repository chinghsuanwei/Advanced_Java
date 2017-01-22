package widget;

import android.graphics.drawable.Drawable;

public abstract class Widget extends Drawable{
	public abstract String toCommand();
	public abstract void parseCommand(String cmd);
	public abstract boolean isPicked(int x, int y);
}
