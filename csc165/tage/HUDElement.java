package tage;

import com.jogamp.opengl.util.gl2.GLUT;
import org.joml.*;


/** A container class to hold HUD elements for the HUDmanager's Arraylist 
 * <p>
 * @author Aaron Goodlund
*/
public class HUDElement {
    
    private float [] HUDColor;
    private String HUDstring;
	private int HUDfont = GLUT.BITMAP_TIMES_ROMAN_24;
	private int xPos, yPos;

    public HUDElement(String name, Vector3f color, int x, int y){ 
        HUDColor = new float[3];
        HUDColor[0]=color.x(); HUDColor[1]=color.y(); HUDColor[2]=color.z();
        HUDstring = name;
        xPos=x;yPos=y;
    }
    public HUDElement(String name, float[] color, int x, int y){ 
        HUDColor = new float[3];
        HUDColor[0]=color[0]; HUDColor[1]=color[1]; HUDColor[2]=color[1];
        HUDstring = name;
        xPos=x;yPos=y;
    }
//-------------G/S HUD name-----------------------
    public String getHUDString(){ return HUDstring; }
    public void setHUDString(String name){ HUDstring = name;}
//-------------G/S Font---------------------------
    public void setHUDfont(int font){ HUDfont = font; }
    public int getHUDfont(){ return HUDfont; }
//-------------G/S HUD color elements-------------
    public void setHUDColor(Vector3f color){ HUDColor[0]=color.x(); HUDColor[1]=color.y(); HUDColor[2]=color.z(); }
    public void setHUDColor(float r, float g, float b){ HUDColor[0] = r; HUDColor[1] = g; HUDColor[2] = b; }
    public void setHUDColor(float[] c){ if(c.length > 3) return; HUDColor[0]=c[0]; HUDColor[1]=c[1]; HUDColor[2]=c[2]; }
    public float[] getHUDColorArray(){ return HUDColor; }
    public Vector3f getHUDColorVector(){ return new Vector3f(HUDColor[0], HUDColor[1], HUDColor[2]); }
    public float getRed(){ return HUDColor[0]; }
    public float getBlue(){ return HUDColor[1]; }
    public float getGreen(){ return HUDColor[2]; }
//-------------G/S position-----------------------
    public void setX(int x){ xPos=x; }
    public void setY(int y){ yPos=y; }
    public void setPosition(int x, int y){ xPos=x; yPos=y; }
    public int getX(){ return xPos; }
    public int getY(){ return yPos; }
    public int[] getPosition(){ return new int[] {xPos,yPos}; }
/** update all values of a HUD string at once */
    public void updateHUD(String name, Vector3f color, int x, int y){ setHUDString(name); setHUDColor(color); setX(x); setY(y); }
}
