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
	
	public GhostNPC()
	{ 
		npc = new NPC;
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
	
	public Vector3f getPosition ()
	{
		Vector3f loc = new Vector3f();
		this.getWorldLocation(loc);
		return loc;
	}
	
		public Matrix4f getOrientation ()
	{
		Matrix4f ori = new Matrix4f();
		this.getWorldRotation(ori);
		return ori;
	}
}