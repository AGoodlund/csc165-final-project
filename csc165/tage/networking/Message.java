package tage.networking;

import java.io.Serializable;
import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Class for normalizing client/server communication
 *  Built to be used primarily by ProtocolClient and GameServerUDP/TCP
 * 
 *  @author: Aaron Goodlund
 */

public class Message implements Serializable{
    private UUID senderID, receiverID;
    private static Message message; //singleton to force reuse and lower memory overhead
    private boolean respondSuccessful;
    private float num;

    private Vector3f v;
    private Matrix4f m; 

    public MessageType type, character;    //left as public so it's easier to access

//    public CharacterType character;
    
/** an enum that holds the type of message is being sent/received. This may need to be its own thing */
    public enum MessageType{
    DEFAULT,
    JOIN,
    BYE,
    CREATE,
    DSFR,
    WSDS,
    TURN,
    MOVE,
	CREATE_NPC,
    CHANGE_NPC,
	MNPC,
	IS_NEAR,
	NPC_REQUEST,
    DIVER,
    ENEMY,
    DOL,
    GAY_DOL
    }
/* * enum to hold ghost avatar looks because it was sending Strings as null 
    public enum CharacterType{
    DIVER,
    ENEMY,
    DOL,
    GAY_DOL
    }
*/
    private Message(){
        v = new Vector3f();
        m = new Matrix4f();
    }

/** Singleton construction */
    public static Message getMessage(){
        if(message == null)
            message = new Message();
        return message;
    }
/** make a complete message with a Vector3f */
    public void makeMessage(Vector3f input, UUID sender, UUID receiver, MessageType type){
        v.set(input);
        setIDType(sender, receiver, type);
    }
/** make a complete message with a Matrix4f */
    public void makeMessage(Matrix4f input, UUID sender, UUID receiver, MessageType type){
        m.set(input);
        setIDType(sender, receiver, type);
    }
/** make a complete message with both a Matrix and Vector */
    public void makeMessage(Vector3f vec, Matrix4f mat, UUID sender, UUID receiver, MessageType type){
        v.set(vec);
        m.set(mat);
        setIDType(sender, receiver, type);
    }
/** add a Vector3f to the message */
    public void addItem(Vector3f input){ v.set(input); }
/** add a Matrix4f to the message */
    public void addItem(Matrix4f input){ m.set(input); }
/** add sender UUID to the message */
    public void addItem(UUID sender){ senderID = sender; }
/** add a MessageType enum to the message */
    public void addItem(MessageType type){ this.type = type; }
/** add an arbitrary float to the message */
    public void addItem(float f){ num = f; }
/** add a character to the message */
    public void addChar(MessageType s){ character = s; }
/** retrieve a float from the message */
    public float getNum(){ return num; } 
/** retrieve shape name the message */
    public String getShape(){ return shape; }
/** add texture name string */
    public void addTexture(String s){ texture = s; } //TODO: Change from strings to ints, or use string in the switch statement
/** retrieve texture name */
    public String getTexture(){ return texture; }

/** add a receiver UUID to the message */
    public void addDestination(UUID receiver){ receiverID = receiver; }
/** fill dest with values from message's Vector */
    public void getVector(Vector3f dest){ dest.set(v); }
/** fill dest with values from message's Matrix */
    public void getMatrix(Matrix4f dest){ dest.set(m); }
/** get UUID number of the sender */
    public UUID getSenderID(){ return senderID; }
/** get UUID number of the receiver */
    public UUID getReceiverID(){ return receiverID; } 

/** clear vector, matrix, and MessageType data from the message. keeps sender and receiver UUIDs intact */
    public void clear(){
        m.identity();
        v.set(0f,0f,0f);
        type = MessageType.DEFAULT;
        senderID = receiverID = null;
    }
/** copy the values from the given Message to this one */
    public void copy(Message m){
        senderID = m.senderID;
        receiverID = m.receiverID;
        v.set(m.v);
        this.m.set(m.m);
        respondSuccessful = m.respondSuccessful;
        type = m.type;
    }
/** return whether connection was successful */
    public boolean getSuccess(){ return respondSuccessful; }
/** set whether connection was successful */
    public void setSuccess(boolean s){ respondSuccessful = s;} 
/** return the relevant stored values as a String */
    public String toString(){
        String s;
        s = "\nContents of Message:\n";
        s += "\tMessageType = " + type;
        s += "\n\tCharacterType = " + character;
        s += "\n\tSender ID = " + senderID;
        s += "\n\tDestination ID = " + receiverID;
        s += "\n\tshape = " + shape; //These are just strings
        s += "\n\ttexture = " + texture;
        s += "\n\tv:\n" + v;
        s += "\n\tm:\n" + m;

        return s;
    }

    private void setIDType(UUID sender, UUID receiver, MessageType type){ senderID = sender; receiver = receiverID; this.type = type; }
}
