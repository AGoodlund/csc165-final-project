import org.joml.*;
public class NPC
{ 
	double locationX, locationY, locationZ;
	double dir = 0.1;
	double size = 1.0;
	int id;
	public NPC()
	{ 
		locationX=0.0;
		locationY=0.0;
		locationZ=0.0;
		id = 3621;
	}
	
	public void randomizeLocation(int seedX, int seedZ)
	{ 
		locationX = seedZ;
		locationX = ((double)seedX)/4.0 - 5.0;
		locationY = 0;
		locationZ = -2;
	}
	public double getX() { return locationX; }
	public double getY() { return locationY; }
	public double getZ() { return locationZ; }
	public void getBig() { size=2.0; }
	public void getSmall() { size=1.0; }
	public double getSize() { return size; }
	public void updateLocation()
	{ 
		if (locationX > 10) dir=-0.1;
		if (locationX < -10) dir=0.1;
		
		locationX = locationX + dir;
	} 
	
	public Vector3f getPosition ()
	{
		
		float locX = (float)locationX;
		float locY = (float)locationY;
		float locZ = (float)locationZ;
		Vector3f loc = new Vector3f(locX, locY,locZ);
		//this.getWorldLocation(loc);
		return loc;
	}
	
		public void setPosition ()
	{
		//TODO: add to this
		/*Vector3f loc = new Vector3f();
		this.getWorldLocation(loc);
		return loc;*/
	}
	
}
