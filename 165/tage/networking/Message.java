package tage.networking;

import org.joml.Vector3f;
import org.joml.Matrix4f;

import java.io.Serializable;
import java.util.UUID;

/** class for normalizing client/server communication
 * @author: Aaron Goodlund
 */

public class Message implements Serializable{  //the thing that gets sent rather than strings
    private float[] vector; //could be expanded to a 3x3 matrix if it could be used to send UVN vectors
    private float[] matrix;
    private UUID ID;
    private static Message message;//singleton to force reuse
    private int i;
    private boolean respondSuccessful;

    public MessageType type;
    
/** an enum that holds the type of message is being sent/received. This may need to be its own thing */
    public enum MessageType{
    DEFAULT,
    JOIN,
    BYE,
    CREATE,
    DSFR,
    TURN,
    MOVE,
    RESPOND
    }

    private Message(){
        vector = new float[3];
        matrix = new float[16];
        //might need to initialize a UUID just so there isn't a null issue
    }

/** Singleton construction */
    public static Message getMessage(){
        if(message == null)
            message = new Message();
        return message;
    }
/** make a complete message with a float array of 3 or 16 */
    public void makeMessage(float[] input, UUID ID, MessageType type){
        if(input.length == 3)
            System.arraycopy(input, 0, vector, 0, 3);
        else if(input.length == 16)
            System.arraycopy(input, 0, matrix, 0, 16);
        else System.out.println("Unknown message contents sent to tage.networking.Message.java\ndid not send a vector or matrix");
        setIDType(ID, type);
    }
/** make a complete message with a Vector3f */
    public void makeMessage(Vector3f input, UUID ID, MessageType type){
        loadVector(input);
        setIDType(ID, type);
    }
/** make a complete message with a Matrix4f */
    public void makeMessage(Matrix4f input, UUID ID, MessageType type){
        loadMatrix(input);
        setIDType(ID, type);
    }
/** add a Vector3f to the message */
    public void addItem(Vector3f input){ loadVector(input); }
/** add a Matrix4f to the message */
    public void addItem(Matrix4f input){ loadMatrix(input); }
/** add a UUID to the message */
    public void addItem(UUID ID){ this.ID = ID; }
/** add a MessageType enum to the message */
    public void addItem(MessageType type){ this.type = type; }

    private void setIDType(UUID ID, MessageType type){ this.ID = ID; this.type = type; }
/** receive stored Vector */
    public Vector3f getVector(){ return new Vector3f(vector); } 
/** receive stored Matrix4f */
    public Matrix4f getMatrix(){ 
        return new Matrix4f(matrix[0], matrix[1], matrix[2], matrix[3],
                            matrix[4], matrix[5], matrix[6], matrix[7],
                            matrix[8], matrix[9], matrix[10], matrix[11],
                            matrix[12], matrix[13], matrix[14], matrix[15]);
    }
//might be able to do a helper version that uses pointers to fill a passed helper rather than creating and returning 
//the vector/matrix object. for matrix use Matrix4f.m__(float) to put everything into moo-m33 (Gordan says should work)

/** fill dest with values from message's Vector UNTESTED*/
    public Vector3f getVector(Vector3f dest){ dest.set(vector); return dest; }//dest.x(vector[0]); dest.y(vector[1]); dest.z(vector[2]); }
/** fill dest with values from message's Matrix UNTESTED */
    public Matrix4f getMatrix(Matrix4f dest){ 
        dest.set(matrix); return dest;
        /* 
        dest.m00(matrix[0]); dest.m10(matrix[1]); dest.m20(matrix[2]); dest.m30(matrix[3]);
        dest.m01(matrix[4]); dest.m11(matrix[5]); dest.m21(matrix[6]); dest.m31(matrix[7]);
        dest.m02(matrix[8]); dest.m12(matrix[9]); dest.m22(matrix[10]); dest.m32(matrix[11]);
        dest.m03(matrix[12]); dest.m13(matrix[13]); dest.m23(matrix[14]); dest.m33(matrix[15]);*/
    }
//I don't know if either of these actually work
/** get UUID number from message */
    public UUID getID(){ return ID; }

    private void loadVector(Vector3f v){
        vector[0] = v.x();
        vector[1] = v.y();
        vector[2] = v.z();
    }
    private void loadMatrix(Matrix4f m){
        matrix[0] = m.m00();
        matrix[1] = m.m10();
        matrix[2] = m.m20();
        matrix[3] = m.m30();
        matrix[4] = m.m01();
        matrix[5] = m.m11();
        matrix[6] = m.m21();
        matrix[7] = m.m31();
        matrix[8] = m.m02();
        matrix[9] = m.m12();
        matrix[10] = m.m22();
        matrix[11] = m.m32();
        matrix[12] = m.m03();
        matrix[13] = m.m13();
        matrix[14] = m.m23();
        matrix[15] = m.m33();
    }
/** clear vector, matrix, and MessageType data from the message */
    public void clear(){
        vector[0] = 0;
        vector[1] = 0;
        vector[2] = 0;

        for(i = 0; i < 16; i++)
            matrix[i] = i%4; //if all goes well this should turn it into the identity
        type = MessageType.DEFAULT;
        //ID can't be cleared easily as far as I can tell
    }

    public boolean getSuccess(){ return respondSuccessful; }
    public void setSuccess(boolean s){ respondSuccessful = s;} 
}
