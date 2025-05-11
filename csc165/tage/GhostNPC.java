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
		System.out.println("A ghostNPC has ben created with ID " + id ); //TODO: remove
	}
	public GhostNPC(){
		
		super(GameObject.root());
		id = 8080;
		setLocalLocation(new Vector3f(0f,0f,0f));
		setLocalRotation(new Matrix4f().identity());
		
		System.out.println("A generic ghostNPC has been created."); //TODO: remove
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

	public void updateLocation(){}
	public void randomizeLocation(int seedX, int seedZ){
		setLocalLocation(new Vector3f((float)seedX/4.0f - 5.0f, 0f, (float)seedZ/4.0f - 5.0f));
	}
}