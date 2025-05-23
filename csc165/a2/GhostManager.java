package a2;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
//import tage.networking.Message.MessageType;

public class GhostManager
{
	private MyGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();
	private GhostAvatar ghostAvatar;
	private int characterFlag = 0;

	public GhostManager(VariableFrameRateGame vfrg)
	{	game = (MyGame)vfrg;
	}
	
	public void createGhostAvatar(UUID id, Vector3f position) throws IOException
	{	createGhostAvatar(id, position, 0.25f);
	}
	public void createGhostAvatar(UUID id, Vector3f position, float scale) throws IOException{
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(scale);
		newAvatar.setLocalScale(initialScale);
		ghostAvatars.add(newAvatar);
	}
	public void createGhostAvatar(UUID id, Vector3f position, Matrix4f rotation, float scale) throws IOException{
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(scale);
		newAvatar.setLocalScale(initialScale);
		newAvatar.setLocalRotation(rotation);
		ghostAvatars.add(newAvatar);
		
	}
	public void createGhostAvatar(UUID id, Vector3f position, Matrix4f rotation, String ghostShape, String ghostTexture, float scale) throws IOException{
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(scale);
		newAvatar.setLocalScale(initialScale);
		newAvatar.setLocalRotation(rotation);
		ghostAvatars.add(newAvatar);
		
	}
	public void removeGhostAvatar(UUID id)
	{	ghostAvatar = findAvatar(id);
		if(ghostAvatar != null)
		{	game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
			ghostAvatars.remove(ghostAvatar);
		}
		else
		{	System.out.println("tried to remove, but unable to find ghost in list\n" + "Cannot find UUID " + id);
		}
	}

	private GhostAvatar findAvatar(UUID id)
	{	Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while(it.hasNext())
		{	ghostAvatar = it.next();
			if(ghostAvatar.getID().compareTo(id) == 0)
			{	return ghostAvatar;
			}
		}		
		return null;
	}
	
	public void updateGhostAvatar(UUID id, Vector3f position)
	{		
		setGhostPosition(id, position);
	}
	public void updateGhostAvatar(UUID id, Matrix4f orientation){
		turnGhostAvatar(id, orientation);
	}
	
	public void setGhostPosition (UUID id, Vector3f position)
	{
		ghostAvatar = findAvatar(id);
		
		if (ghostAvatar != null)
			ghostAvatar.setPosition(position);
//System.out.println("ghost " + id + " sent pos " + position);
		else
		{	System.out.println("Tried to update ghost avatar position, but unable to find ghost in list\nCannot find UUID " + id);
		}
	}

	public void turnGhostAvatar(UUID id, Matrix4f orientation){
		ghostAvatar = findAvatar(id);
		if(ghostAvatar != null)
			ghostAvatar.setLocalRotation(orientation);
	}

	public void setGhostScale(UUID id, float scale){ 
		ghostAvatar = findAvatar(id);
		if(ghostAvatar != null)
		ghostAvatar.setLocalScale(new Matrix4f().scaling(scale));
	}

	public void changeGhostAvatar(UUID id, int pos){//, MessageType changeTo){
		ghostAvatar = findAvatar(id);
		if(ghostAvatar == null)
			return;

		Matrix4f m = new Matrix4f(); //created during runtime because this should not be called all that often
//		characterFlag++;
//		characterFlag %= 4;
		switch(pos){//characterFlag){
			case 0:
				ghostAvatar.setTextureImage(game.getDiverTexture());
				ghostAvatar.setShape(game.getDiverShape());
				game.getDiverSize(m);
				ghostAvatar.setLocalScale(m);
				break;
			case 1:
				ghostAvatar.setTextureImage(game.getDolTexture());
				ghostAvatar.setShape(game.getDolShape());
				game.getDolSize(m);
				ghostAvatar.setLocalScale(m);
				break;
			case 2:
				ghostAvatar.setTextureImage(game.getEnemyTexture());
				ghostAvatar.setShape(game.getEnemyShape());
				game.getEnemySize(m);
				ghostAvatar.setLocalScale(m);
				break;
			case 3:
				ghostAvatar.setTextureImage(null);
				ghostAvatar.setShape(game.getDolShape());
				game.getDolSize(m);
				ghostAvatar.setLocalScale(m);
				ghostAvatar.getRenderStates().setPositionalColor(true);
				break;
			default:
				break;
		}
	}
}