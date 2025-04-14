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

public class Message implements Serializable{  //the thing that gets sent rather than strings
    private UUID ID, remoteID;  //ID is sender, remoteID is receiver
    private static Message message;//singleton to force reuse
    private boolean respondSuccessful;
    private int i;
    //private float scale;

    private Vector3f v;
    private Matrix4f m; 

    public MessageType type;    //left as public so it's very easy to use
    
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
//    RESPOND
    }

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
    public void addItem(UUID sender){ ID = sender; }
/** add a MessageType enum to the message */
    public void addItem(MessageType type){ this.type = type; }
/** add a receiver UUID to the message */
    public void addRemoteID(UUID receiver){ remoteID = receiver; }
/** set ID to self and receiver to given UUID */
    public void replyTo(UUID receiver){
        ID = remoteID;
        remoteID = receiver;

//System.out.println("\nreceiver:\t" + receiver + "\nID:\t\t" + ID + "\nremoteID:\t" + remoteID);
    }

    private void setIDType(UUID sender, UUID receiver, MessageType type){ ID = sender; receiver = remoteID; this.type = type; }

/** fill dest with values from message's Vector */
    public void getVector(Vector3f dest){ dest.set(v); }
/** fill dest with values from message's Matrix */
    public void getMatrix(Matrix4f dest){ dest.set(m); }
/** get UUID number of the receiver */
    public UUID getID(){ return remoteID; }
/** get UUID number of the sender */
    public UUID getRemoteID(){ return ID; } 

/** clear vector, matrix, and MessageType data from the message. keeps sender and receiver UUIDs intact */
    public void clear(){
        m.identity();
        v.set(0f,0f,0f);
        type = MessageType.DEFAULT;
        ID = remoteID = null;
        //ID can't be cleared easily as far as I can tell
    }
/** copy the values from the given Message to this one */
    public void copy(Message m){
        ID = m.ID;
        remoteID = m.remoteID;
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
        s += "\n\tSender ID = " + ID;
        s += "\n\tDestination ID = " + remoteID;
        s += "\n\tv:\n" + v;
        s += "\n\tm:\n" + m;

        return s;
    }
}
