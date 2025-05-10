package tage.nodeControllers;
import tage.*;
import org.joml.*;

/**
* A RotationController is a node controller that, when enabled, causes any object
* it is attached to to rotate in place around the tilt axis specified.
* @author Scott Gordon
*/
public class RollController extends NodeController
{
	private float yawSpeed = 1.0f, pitchSpeed = 1.0f, rotAmt;
//	private Matrix4f pitchMatrix, yawMatrix, newRotation;
//    private Vector3f rotVec;

	/** Creates a rotation controller with vertical axis, and speed=1.0. */
	public RollController() { super(); }

	/** Creates a rotation controller with rotation axis and speed as specified. */
	public RollController(float speed)
	{	super();
		yawSpeed = speed;
        pitchSpeed = speed;
//		pitchMatrix = new Matrix4f();
//		yawMatrix = new Matrix4f();
	}

	/** sets the rotation speed when the controller is enabled */
	public void setSpeed(float s) { yawSpeed = s; pitchSpeed = s; }

    public void setYawSpeed(float s){ yawSpeed = s; }

    public void setPitchSpeed(float s){ pitchSpeed = s; }

	/** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	*   during display().  It is for engine use and should not be called by the application.
	*/
	public void apply(GameObject go)
	{	//float elapsedTime = super.getElapsedTime();
//		float rotAmt = elapsedTime * yawSpeed;
		go.localYaw(yawSpeed);
//		rotAmt = elapsedTime * pitchSpeed;
		go.pitch(pitchSpeed);

		//pitch then yaw
/* 		rotAmt = elapsedTime * pitchSpeed;
		pitchMatrix = go.getWorldRotation();
		yawMatrix = go.getWorldRotation();

		newRotation = new Matrix4f().rotation(rotAmt, go.getWorldUpVector());
		newRotation.rotation(rotAmt,go.getWorldRightVector());
		go.setLocalRotation(newRotation.mul(pitchMatrix).mul(yawMatrix));
*/		
//		newRotation = new Matrix4f().rotation(rotAmt, go.getWorldRightVector());
//		go.setLocalRotation(newRotation.mul(yawMatrix)); 
 
/*/yaw
		rotMatrix = go.getWorldRotation();
        newRotation = new Matrix4f().rotation(rotAmt, go.getWorldRightVector());
        go.setLocalRotation(newRotation.mul(rotMatrix)); 
//pitch
        rotAmt = elapsedTime * pitchSpeed;
        rotMatrix = go.getWorldRotation();
        newRotation = new Matrix4f().rotation(rotAmt, go.getWorldUpVector());
        go.setLocalRotation(newRotation.mul(rotMatrix));*/

	}
}

/*
 *  yaw
        Vector3f localUp = getWorldUpVector();
        Matrix4f worldRot = getWorldRotation(), addedRotation = (new Matrix4f()).rotation(rad, localUp);
        setLocalRotation(addedRotation.mul(worldRot));

    pitch
        Vector3f worldRight = getWorldRightVector();
		Matrix4f worldRot = getWorldRotation(), addedRotation = (new Matrix4f()).rotation(rad, worldRight);
		setLocalRotation(addedRotation.mul(worldRot));
 */