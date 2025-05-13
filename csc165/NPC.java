import org.joml.*;
import java.util.UUID;
import a2.spot;

public class NPC
{ 
	private Vector3f pos, dir;
	private UUID id;
	private boolean hunting = false;
	private Vector4f v = new Vector4f();

	private int hp = 2;

	public NPC()
	{ 
		id = UUID.randomUUID();
		dir = new Vector3f(0,0,-1);
	}
	
	public void randomizeLocation(int seedX, int seedZ)
	{ 
		pos = new Vector3f((float)seedX/4f-5f, 2f, (float)seedZ/4f-5f);

	}
	public void updateLocation(float elapsedTime) 	//TICK
	{ 
		if(hunting){
			dir.mul(spot.NPCSpeed*elapsedTime);
			pos.add(dir);			//move at (speed) in current direction
		}
	} 

	public UUID getID() { return id; }
	
	public Vector3f getPosition (Vector3f dest) { dest.set(pos); return dest; }
	public void setPosition (Vector3f position) { pos.set(position); }

	public Matrix4f getRotation(Matrix4f dest) {v.set(dir.x(), dir.y(), dir.z(), 0f); dest.setColumn(2, v); return dest; }

	public void lookAt(Vector3f p){ //set dir to be pointing towards the target position
		dir.set(p.x()-pos.x(), p.y() - pos.y(), p.z()-pos.z()).normalize();
	}

	public void setHunt(boolean s){ hunting = s; }
	public void takeDamage(int dmg){ hp -= dmg; if(hp <= 0) die(); }
	private void die(){
//		reset position and hp
	}
}
/* lookAt() from Camera.java
	Forward facing vector	N (dir)
	Right facing vector 	U
	Up facing vector		V
		public void lookAt(float x, float y, float z)
		{	setN((new Vector3f(x-location.x(), y-location.y(), z-location.z())).normalize());
		everything below this point is for if N is looking along the y axis (not possible), or setting U/V
			Vector3f copyN = new Vector3f(n); 
			if ((n.equals(0,1,0)) || (n.equals(0,-1,0)))
				u = new Vector3f(1f,0f,0f); 
			else
				u = (new Vector3f(copyN.cross(0f,1f,0f))).normalize();
			Vector3f copyU = new Vector3f(u);
			v = (new Vector3f(copyU.cross(n))).normalize(); 
		}
	*/