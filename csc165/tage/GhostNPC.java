package tage;
import org.joml.*;
import java.util.UUID;

public class GhostNPC extends GameObject	//TODO:This is just GhostAvatar for NPCs. Simplify it!
{ 
	private int id;
	private UUID uuid;

	public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p) 
	{ 
		super(GameObject.root(), s, t);
		this.id = id;
		setLocalLocation(p);
		//System.out.println("A ghostNPC has ben created with ID " + id );
		//The ghostNPCs did spawn in, but they were invisible. I spent a long time 
	}
	public GhostNPC(UUID id, ObjShape s, TextureImage t, Vector3f p){
		super(GameObject.root(), s, t);
		uuid = id;
		setPosition(p);
	}
/* 
	public GhostNPC(){
		
		super(GameObject.root());
		id = 8080;
		setLocalLocation(new Vector3f(0f,0f,0f));
		setLocalRotation(new Matrix4f().identity());
		
		//System.out.println("A generic ghostNPC has been created.");
	}
*/
	public void setPosition (Vector3f move) { setLocalLocation(move); }

	public UUID getID(){ return uuid; }
/* 
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
*/	
}