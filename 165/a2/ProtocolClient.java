package a2;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.networking.client.GameConnectionClient;
import tage.networking.Message;
//import tage.networking.Message.CharacterType;

public class ProtocolClient extends GameConnectionClient
{
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id, ghostID;
//	private Matrix4f ghostOrientation = new Matrix4f();
	private Matrix4f ghostMatrix = new Matrix4f();
	private Vector3f ghostVector = new Vector3f();
	private Message message = Message.getMessage();
	private Message.MessageType t;
	/*private GhostNPC ghostNPC;
	
	
	// ------------- GHOST NPC SECTION --------------
	private void createGhostNPC(Vector3f position) throws IOException
	{ 
		if (ghostNPC == null)
			ghostNPC = new GhostNPC(0, game.getNPCshape(),game.getNPCtexture(), position);
	}
	private void updateGhostNPC(Vector3f position, double gsize)
	{ 
		boolean gs;
		if (ghostNPC == null)
		{ try{ createGhostNPC(position);}
		  catch (IOException e) { System.out.println("error creating npc"); }
	}
	
	ghostNPC.setPosition(position);
	if (gsize == 1.0)
		gs=false;
	
	else 
		gs=true;
	ghostNPC.setSize(gs);
}*/

public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException 
	{	super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
	}
	
	public UUID getID() { return id; }

	@Override
	protected void processPacket(Object o)
	{	message.copy((Message)o);
		t = message.type;
//System.out.println("At beginning of processPacket in Protocol\n\nmessage is\n" + message);
					//getID is receiver (here), getRemoteID is sender(GameServer)
		switch(t){	//making a switch statement because it's faster than if/else
			case JOIN:

				if(message.getSuccess())
				{	System.out.println("join success confirmed");
					game.setIsConnected(true);
					game.getPlayerPosition(ghostVector); game.getPlayerRotation(ghostMatrix);
					sendCreateMessage(ghostVector, ghostMatrix);//game.getPlayerPosition(), game.getPlayerRotation());
				}
				else
				{	System.out.println("join failure confirmed");
					game.setIsConnected(false);
				}
				break;
			case BYE:

				ghostID = message.getSenderID();
				ghostManager.removeGhostAvatar(ghostID);
				break;
			case DSFR: 

				message.getVector(ghostVector);
				message.getMatrix(ghostMatrix);
				ghostID = message.getSenderID();
				try
				{	//ghostManager.createGhostAvatar(ghostID, ghostVector, ghostMatrix, message.getShape(), message.getTexture(), spot.startingScale);
					ghostManager.createGhostAvatar(ghostID, ghostVector, ghostMatrix, spot.startingScale);
//System.out.println("This ghost was made by ID" + id+" in ProtocolClient DSFR at position " + ghostVector.toString());
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
				break;
			case CREATE:
				message.getVector(ghostVector);
				message.getMatrix(ghostMatrix);
				ghostID = message.getSenderID();
				try
				{	//ghostManager.createGhostAvatar(ghostID, ghostVector, ghostMatrix, message.getShape(), message.getTexture(), spot.startingScale);
					ghostManager.createGhostAvatar(ghostID, ghostVector, ghostMatrix, spot.startingScale);
//					ghostManager.turnGhostAvatar(ghostID, ghostMatrix);
//System.out.println("This ghost was made by "+id+" in ProtocolClient CREATE at position " + ghostVector.toString());
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
				break;
			case WSDS:
				ghostID = message.getSenderID();
				game.getPlayerPosition(ghostVector);
				sendDetailsForMessage(ghostID, ghostVector);//game.getPlayerPosition());
				//TODO: also needs to get the player's ObjShape and TextureImage name
				break;
			case MOVE:

				message.getVector(ghostVector);
				ghostID = message.getSenderID();
				ghostManager.updateGhostAvatar(ghostID, ghostVector);
//System.out.println("in Protocol the ghostVector = " + ghostVector);
				break;
			case TURN:

				message.getMatrix(ghostMatrix);
				ghostID = message.getSenderID();
				ghostManager.turnGhostAvatar(ghostID, ghostMatrix);
				break;
				
			/*case CREATE_NPC:
				// create a new ghost NPC
				// Parse out the position
				Vector3f ghostPosition = new Vector3f(
				Float.parseFloat(messageTokens[1]), //TODO: These need to be updated for the new system
				Float.parseFloat(messageTokens[2]),
				Float.parseFloat(messageTokens[3]));
				try
				{ createGhostNPC(ghostPosition);
				} catch (IOException e) { System.out.println("Error creating ghost npc"); } // error creating ghost avatar
				break;*/
				
			case MNPC:
				break;
			case IS_NEAR:
				break;

			case CHANGE_NPC:
				ghostID = message.getSenderID();
				message.getVector(ghostVector);
//				Message.CharacterType s = message.getCharacter();
//System.out.println("message recieved as\n" + message.toString());
				ghostManager.changeGhostAvatar(ghostID, (int)ghostVector.x());//, message.character);
				break;				
			case DEFAULT:

				break;
			default:
				System.out.println("an unknown MessageType was sent to ProtocolClient");
				break;
		}
	}
	
//TODO: this is where messages are created	
	// The initial message from the game client requesting to join the 
	// server. localId is a unique identifier for the client. Recommend 
	// a random UUID.
	// Message Format: (join,localId)
	
	public void sendJoinMessage()
	{	try 
		{	message.addItem(id);
			message.addItem(Message.MessageType.JOIN);
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the client is leaving the server. 
	// Message Format: (bye,localId)

	public void sendByeMessage()
	{	try 
		{	
			message.addItem(id);
			message.addItem(Message.MessageType.BYE);

			sendPacket(message);
			message.clear();
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the client�s Avatar�s position. The server 
	// takes this message and forwards it to all other clients registered 
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessage(Vector3f position, Matrix4f facing)//, String shapeName, String textureName)
	{	try 
		{	
			message.addItem(id);
			message.addItem(Message.MessageType.CREATE);
			message.addItem(position);
			message.addItem(facing);

			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the local avatar's position. The server then 
	// forwards this message to the client with the ID value matching remoteId. 
	// This message is generated in response to receiving a WANTS_DETAILS message 
	// from the server.
	// Message Format: (dsfr,remoteId,localId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID remoteId, Vector3f position)
	{	try 
		{	//System.out.println("sent remoteID = " + remoteId);
			message.addItem(id);
			message.addDestination(remoteId);
			message.addItem(Message.MessageType.DSFR);
			message.addItem(position);

			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the local avatar has changed position.  
	// Message Format: (move,localId,x,y,z) where x, y, and z represent the position.

	public void sendMoveMessage(Vector3f position)
	{	try 
		{	
			message.addItem(id);
			message.addItem(Message.MessageType.MOVE);
			message.addItem(position);

			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}

	public void sendTurnMessage(Matrix4f orientation){
		try{
			message.addItem(id);
			message.addItem(Message.MessageType.TURN);
			message.addItem(orientation);

			sendPacket(message);			
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public void changeAvatar(Message.MessageType name, int pos){
		try{
			message.addItem(id);
			message.addItem(Message.MessageType.CHANGE_NPC);
			message.addItem(new Vector3f((float)pos, 0, 0));
//			message.addChar(name);

System.out.println("Sending Message as " + message.toString());

			sendPacket(message);
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}

public void trace(){
	System.out.println("\nPROTOCOL_CLIENT" + message.toString());
}
}
