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
					sendCreateMessage(game.getPlayerPosition(), game.getPlayerRotation());
				}
				else
				{	System.out.println("join failure confirmed");
					game.setIsConnected(false);
				}
				break;
			case BYE:
				ghostID = message.getRemoteID();
				ghostManager.removeGhostAvatar(ghostID);
				break;
			case DSFR: 
				message.getVector(ghostVector);
				ghostID = message.getRemoteID();
				try
				{	ghostManager.createGhostAvatar(ghostID, ghostVector, spot.startingScale);
System.out.println("This ghost was made by ID" + id+" in ProtocolClient DSFR at position " + ghostVector.toString());
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
				break;
			case CREATE:
				message.getVector(ghostVector);
				message.getMatrix(ghostMatrix);
				ghostID = message.getRemoteID();
				try
				{	ghostManager.createGhostAvatar(ghostID, ghostVector, spot.startingScale);
					ghostManager.turnGhostAvatar(ghostID, ghostMatrix);
System.out.println("This ghost was made by "+id+" in ProtocolClient CREATE at position " + ghostVector.toString());
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
				break;
			case WSDS:
				ghostID = message.getRemoteID();
				sendDetailsForMessage(ghostID, game.getPlayerPosition());
				break;
			case MOVE:
				message.getVector(ghostVector);
				ghostID = message.getRemoteID();
				ghostManager.updateGhostAvatar(ghostID, ghostVector);
//System.out.println("in Protocol the ghostVector = " + ghostVector);
				break;
			case TURN:
				message.getMatrix(ghostMatrix);
				ghostID = message.getRemoteID();
				ghostManager.turnGhostAvatar(ghostID, ghostMatrix);
				break;
			case DEFAULT:
				break;
			default:
				System.out.println("an unknown MessageType was sent to ProtocolClient");
		}
	}

//		String strMessage = (String)message;
//		System.out.println("message received -->" + strMessage);
//		String[] messageTokens = strMessage.split(",");
		
		// Game specific protocol to handle the message
//		if(messageTokens.length > 0)
/*		if(message.type != Message.MessageType.DEFAULT)
		{
			// Handle JOIN message
			// Format: (join,success) or (join,failure)
//			if(messageTokens[0].compareTo("join") == 0)
			{	//if(messageTokens[1].compareTo("success") == 0)
				if(message.getSuccess())
				{	System.out.println("join success confirmed");
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				}
				else
				{	System.out.println("join failure confirmed");
					game.setIsConnected(false);
			}	}
			
 			// Handle BYE message
			// Format: (bye,remoteId)
			if(messageTokens[0].compareTo("bye") == 0)
			{	// remove ghost avatar with id = remoteId
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
			}
			
			// Handle CREATE message
			// Format: (create,remoteId,x,y,z)
			// AND
			// Handle DETAILS_FOR message
			// Format: (dsfr,remoteId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0 || (messageTokens[0].compareTo("dsfr") == 0))
			{	// create a new ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));

				try
				{	ghostManager.createGhostAvatar(ghostID, ghostPosition, 3f);
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
			}
			
			// Handle WANTS_DETAILS message
			// Format: (wsds,remoteId)
			if (messageTokens[0].compareTo("wsds") == 0)
			{
				// Send the local client's avatar's information
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition());
			}
			
			
			if(messageTokens[0].compareTo("turn")==0){
				//Format: (turn,remoteID,worldRotation)	note: no up vector because it's 0,1,0 
				UUID ghostID = UUID.fromString(messageTokens[1]);
//				System.out.println("Attempting to turn");

				//parse into a matrix
				ghostOrientation = new Matrix4f( //Ur, Uu, Uf, 0, Vr, Vu, Vf, 0, nR, Nu, Nf, 0, 0, 0, 0, 1
					Float.parseFloat(messageTokens[2]),Float.parseFloat(messageTokens[3]),Float.parseFloat(messageTokens[4]),Float.parseFloat(messageTokens[5]),
					Float.parseFloat(messageTokens[6]),Float.parseFloat(messageTokens[7]),Float.parseFloat(messageTokens[8]),Float.parseFloat(messageTokens[9]),
					Float.parseFloat(messageTokens[10]),Float.parseFloat(messageTokens[11]),Float.parseFloat(messageTokens[12]),Float.parseFloat(messageTokens[13]),
					Float.parseFloat(messageTokens[14]),Float.parseFloat(messageTokens[15]),Float.parseFloat(messageTokens[16]),Float.parseFloat(messageTokens[17])
				);
				ghostManager.turnGhostAvatar(ghostID, ghostOrientation);
			}
			
			// Handle MOVE message
			// Format: (move,remoteId,x,y,z)
			if (messageTokens[0].compareTo("move") == 0)
			{
				// move a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);

				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));
				
				ghostManager.updateGhostAvatar(ghostID, ghostPosition);
	}	}	}*/
	
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
//			message.clear();
//			sendPacket(new String("join," + id.toString()));
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
			//sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the client�s Avatar�s position. The server 
	// takes this message and forwards it to all other clients registered 
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessage(Vector3f position, Matrix4f facing)
	{	try 
		{	
			message.addItem(id);
			message.addItem(Message.MessageType.CREATE);
			message.addItem(position);
			message.addItem(facing);
			/*String message = new String("create," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();*/

			sendPacket(message);
//			message.clear();
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
		{	
			message.addItem(id);
			message.addRemoteID(remoteId);
			message.addItem(Message.MessageType.DSFR);
			message.addItem(position);
			/*String message = new String("dsfr," + remoteId.toString() + "," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();*/

			sendPacket(message);
//			message.clear();
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
//System.out.println("position in protocol is " + position);
//System.out.println("message in sendMoveMessage in ProtocolClient\n" +message.toString());
			/*String message = new String("move," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();*/

			sendPacket(message);
//			message.clear();
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}

	public void sendTurnMessage(Matrix4f orientation){
		try{
			message.addItem(id);
			message.addItem(Message.MessageType.TURN);
			message.addItem(orientation);
			/*String message = new String("turn," + id.toString());
			message += "," + orientation.m00();
			message += "," + orientation.m10();
			message += "," + orientation.m20();
			message += "," + orientation.m30();
			message += "," + orientation.m01();
			message += "," + orientation.m11();
			message += "," + orientation.m21();
			message += "," + orientation.m31();
			message += "," + orientation.m02();
			message += "," + orientation.m12();
			message += "," + orientation.m22();
			message += "," + orientation.m32();
			message += "," + orientation.m03();
			message += "," + orientation.m13();
			message += "," + orientation.m23();
			message += "," + orientation.m33();*/

			sendPacket(message);
//			message.clear();
			
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
