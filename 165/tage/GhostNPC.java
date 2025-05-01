package tage;
import org.joml.*;

public class GhostNPC extends GameObject
{ 
	private int id;
	public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p)
	{ 
		super(GameObject.root(), s, t);
		this.id = id;
		setPosition(p);
	}
	//TODO: Add ghost specific behavior functions to this
	public void setSize(boolean big) 
	{ 
		if (!big) { this.setLocalScale((new Matrix4f()).scaling(0.5f)); }
		else { this.setLocalScale((new Matrix4f()).scaling(1.0f)); }
	}
	
		public void setPosition (Vector3f move)
	{ 
		this.translate(move.x(), move.y(), move.z()); 
	}
}