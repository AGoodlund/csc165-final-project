import org.joml.*;
import java.util.UUID;
import a2.spot;

public class NPC
{ 
	private Vector3f pos, dir;
	private UUID id;
	private boolean hunting = false;

	private Vector4f v = new Vector4f();
	private float[] defaultPos;

	private int hp = 2;

	public NPC()
	{ 
		id = UUID.randomUUID();
		dir = new Vector3f(0,0,1);
		pos = new Vector3f();
	}
	
	public void randomizeLocation(int seedX, int seedZ)
	{ 
System.out.println("NPC " + id + " seeded at " + seedX + ", " + seedZ);
		pos.set((float)seedX/4f-5f, 2f, (float)seedZ/4f-5f);
		defaultPos = new float[] {pos.x(), pos.y(), pos.z()};
//System.out.println("NPC spawning at pos " + pos + "\n");
	}
	public void updateLocation(float elapsedTime) 	//TICK
	{ 
		if(hunting){
			dir.mul(spot.NPCSpeed);
			pos.add(dir);			//move at <speed> in current direction
//System.out.println("npc.updateLocation changed to " + pos);
		}
	} 

	public UUID getID() { return id; }
	
	public void getPosition (Vector3f dest) { 
//System.out.println("getPosition of NPC is " + pos);
		dest.set(pos); 
//System.out.println("getPosition of dest is " + dest);	
	}
	public void setPosition (Vector3f position) { pos.set(position); }

	public void getRotation(Matrix4f dest) {
		dest.m20(dir.x());
		dest.m21(dir.y());
		dest.m22(dir.z());
	}

	public void lookAt(Vector3f p){ //set dir to be pointing towards the target position
//System.out.println("running npc.lookAt");
		Vector3f temp = new Vector3f(pos);
		dir.set(p.x()-temp.x(), p.y() - temp.y(), p.z()-temp.z()).normalize();
	}

	public void setHunt(boolean s){ hunting = s; }
	public void takeDamage(int dmg){ hp -= dmg; if(hp < 1) die(); }
	private void die(){
		pos.set(defaultPos);
		hp = 2;
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