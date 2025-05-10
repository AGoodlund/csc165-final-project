package tage;
import static com.jogamp.opengl.GL4.*;

import java.util.ArrayList;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.gl2.GLUT;

import org.joml.*;

/**
* Manages strings, implemented as GLUT strings.
* This class is instantiated automatically by the engine.
* Note that this class utilizes deprectated OpenGL functionality.
* <p>
* The available fonts are:
* <ul>
* <li> GLUT.BITMAP_8_BY_13
* <li> GLUT.BITMAP_9_BY_15
* <li> GLUT.BITMAP_TIMES_ROMAN_10
* <li> GLUT.BITMAP_TIMES_ROMAN_24
* <li> GLUT.BITMAP_HELVETICA_10
* <li> GLUT.BITMAP_HELVETICA_12
* <li> GLUT.BITMAP_HELVETICA_18
* </ul>
 * <p>
 * Baseline colors for HUD text
 * <ul>
 * <li> red
 * <li> green
 * <li> blue
 * <li> purple
 * <li> yellow
 * <li> teal
 * <li> white
 * <li> black
 * </ul>
* @author Scott Gordon, extended by Aaron Goodlund
*/

public class HUDmanager
{	private GLCanvas myCanvas;
	private GLUT glut = new GLUT();
	private Engine engine;

	private ArrayList<HUDElement> HUDList = new ArrayList<HUDElement>();
/* 	public Vector3f red = new Vector3f(1f,0f,0f), green = new Vector3f(0f,1f,0f), blue = new Vector3f(0f,0f,1f), 
					purple = new Vector3f(1f,1f,0f), yellow = new Vector3f(1f,0f,1f), teal = new Vector3f(0f,1f,1f),
					white = new Vector3f(1f,1f,1f), black = new Vector3f(0f,0f,0f);*/
	public float[] 	red = {1,0,0}, green = {0,1,0}, blue = {0,0,1},
					purple = {1,1,0}, yellow = {1,0,1}, teal = {0,1,1},
					white = {1,1,1}, black = {0,0,0};
//------------------------------------------------------

	// The constructor is called by the engine, and should not be called by the game application.
	// It initializes the two HUDs to empty strings.

//to make this more robust might want to put the hud elements into a hashtable like what RenderSystem does with viewports. or a hud manager manager that puts hud objects into an ArrayList
	//might be worth making a HUDElement class with the manager being used to hold an array list of them. Would then be able to use the hash table to name the hud type 

	protected HUDmanager(Engine e){ engine = e; }
	
	protected void setGLcanvas(GLCanvas g) { myCanvas = g; }

	protected void drawHUDs()
	{	GL4 gl4 = (GL4) GLContext.getCurrentGL();
		GL4bc gl4bc = (GL4bc) gl4;

		gl4.glUseProgram(0);
		
		for(HUDElement HUD:HUDList){
			gl4bc.glColor3f(HUD.getRed(), HUD.getGreen(), HUD.getBlue());
			gl4bc.glWindowPos2d (HUD.getX(), HUD.getY());
			glut.glutBitmapString(HUD.getHUDfont(), HUD.getHUDString());
		}
	}

/** add a HUD element to the list reading name in designated color at window pixel position (x,y).
 * Returns the element's index for later updating
 */
	public int addHUDElement(String name, Vector3f color, int x, int y){
		HUDElement ele = new HUDElement(name, color, x, y);
		HUDList.add(ele);
		return HUDList.indexOf(ele);
	}
	public int addHudElement(String name, float[] color, int x, int y){ return addHUDElement(name, new Vector3f(color), x, y); }

/** Remove the hud element at the designated index. This has the potential of throwing off HUD calls if you trim the list to remove the empty spaces */
	public void removeHUDElement(int index){ HUDList.remove(index); }// HUDList.trimToSize(); }	
	public void setHUDFont(int index, int font){ HUDList.get(index).setHUDfont(font); }				//.get(name) returns null if the key isn't in the list. Check for that before setting the font
	public void setHUDColor(int index, Vector3f color){ HUDList.get(index).setHUDColor(color); }
	public void setHUDValue(int index, String name){ HUDList.get(index).setHUDString(name); }
	public void setHUDPosition(int index, int x, int y){ HUDList.get(index).setPosition(x, y); }
/** Reset all values of a hud element */
	public void setHUD(int index, String name, Vector3f color, int x, int y){ HUDList.get(index).updateHUD(name, color, x, y); }
}