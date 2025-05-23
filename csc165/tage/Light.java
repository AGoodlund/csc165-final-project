package tage;
import org.joml.*;

/**
* Holds light information that can be modified by the game application.
* <p>
* The game application should add a light as follows:
* <ul>
* <li> 1. instantiate the light using a Light constructor
* <li> 2. set the type of light using the ENUM (POSITIONAL or SPOTLIGHT).  The default is POSITIONAL.
* <li> 3. set the desired ADS characteristics using setAmbient, setDiffuse, and setSpecular
* <li> 4. set the desired location using setLocation
* <li> 5. set the desired direction, cutoffAngle, and offAxisExponent if this is a spotlight
* <li> 6. add the light to the game using the addLight() function in SceneGraph
* </ul>
* <p>
* TAGE supports ADS lighting for an unlimited number of positional and spot lights.
* Global ambient light is also maintained as a static class entity (default RGB=(.3,.3,.3).
* Each light has fields for ambient, diffuse, and specular contributions.
* Default values are A=(.6,.6,.6), D=(.8,.8,.8), S=(1,1,1)
* <p>
* The values stored in the Light objects are exactly duplicated in an
* SSBO (shader storage buffer object) maintained in the LightManager,
* allowing for an unlimited number of lights to be sent to the shaders.
* As a result, mutators for the light fields also inform the LightManager
* so that the same updates are done in the SSBO.
* @author Scott Gordon
*/

public class Light
{
	// ------------ STATIC -------------
	private static float globalAmbient[] = { 0.3f, 0.3f, 0.3f, 1.0f };

	/** returns a reference to the float array containing the global ambient RGB values */
	public static float[] getGlobalAmbient() { return globalAmbient; }

	/** sets the global ambient R, G, and B values */
	public static void setGlobalAmbient(float r, float g, float b)
	{	globalAmbient[0] = r;
		globalAmbient[1] = g;
		globalAmbient[2] = b;
		globalAmbient[3] = 1.0f;
	}
	private static Engine engine;
	protected static void setEngine(Engine e) { engine = e; }
	// ---------------------------------

	public enum LightType
	{	POSITIONAL,	// Default
		SPOTLIGHT	//
	}

	private LightType lightType = LightType.POSITIONAL;
	private int index = -1;
	private float enabled = 1;
	private float[] location = { 0.0f, 2.0f, 0.0f };
	private float[] ambient = { 0.3f, 0.3f, 0.3f, 1.0f };
	private float[] diffuse = { 0.8f, 0.8f, 0.8f, 1.0f };
	private float[] specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] direction = { 0.0f, -1.0f, 0.0f };

	// ----- ATTENUATION FACTORS ----------
	// KC should be >= 1
	// At least one other factor should be > 0 for attenuation to occur.
	// Set factors to 1,0,0 to disable attenuation.
	// This light is disabled beyond the specified range value.

	private float constantAttenuation = 1;
	private float linearAttenuation   = 0;
	private float quadraticAttenuation = 0;
	private float range = 10000.0f;

	// -------- ADDITIONAL SPOTLIGHT FACTORS --------

	private float cutoffAngle = 30.0f;
	private float offAxisExponent = 1.0f;

	/** The game application should call this constructor to create a Light object, then set its characteristics with the accessors. */
	public Light()
	{
	}

	// For internal engine use only.
	// Each light has an integer index used to coordinate the light objects
	// with the entries in the lights SSBO held in the SceneGraph.
	// The game application should not try to make use of this index, and must not change it.

	protected void setIndex(int i) { index = i; }

	// For internal engine use only.
	// Each light has an integer index used to coordinate the light objects
	// with the entries in the lights SSBO held in the SceneGraph.
	// The game application should not try to make use of this index, and must not change it.

	protected int getIndex() { return index; }

	/** sets the world location (as a Vector3f) for this light */
	public void setLocation(Vector3f l)
	{	location[0] = l.x();
		location[1] = l.y();
		location[2] = l.z();
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** sets the ambient RGB characteristic for this light */
	public void setAmbient(float r, float g, float b)
	{	ambient[0] = r;
		ambient[1] = g;
		ambient[2] = b;
		ambient[3] = 1.0f;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}
	public void setAmbient(float[] f){ setAmbient(f[0],f[1],f[2]); }

	/** sets the diffuse RGB characteristic for this light */
	public void setDiffuse(float r, float g, float b)
	{	diffuse[0] = r;
		diffuse[1] = g;
		diffuse[2] = b;
		diffuse[3] = 1.0f;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}
	public void setDiffuse(float[] f){ setDiffuse(f[0],f[1],f[2]); }

	/** sets the specular RGB characteristic for this light */
	public void setSpecular(float r, float g, float b)
	{	specular[0] = r;
		specular[1] = g;
		specular[2] = b;
		specular[3] = 1.0f;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}
	public void setSpecular(float[] f){ setSpecular(f[0],f[1],f[2]); }
	
/** sets ADS to the same value */
	public void setColor(float r, float g, float b){
		setAmbient(r,g,b);
		setDiffuse(r, g, b);
		setSpecular(r, g, b);
	}
	public void setColor(float[] f){
		setAmbient(f);
		setDiffuse(f);
		setSpecular(f);
	}

	/** sets a constant attenuation factor for this light */
	public void setConstantAttenuation(float ca)
	{	constantAttenuation = ca;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** sets a linear attenuation factor for this light */
	public void setLinearAttenuation(float la)
	{	linearAttenuation = la;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** sets a quadratic attenuation factor for this light */
	public void setQuadraticAttenuation(float qa)
	{	quadraticAttenuation = qa;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** Not yet implemented. */
	public void setRange(float r)
	{	range = r;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** sets the direction for this light - applicable if this is a spotlight */
	public void setDirection(Vector3f d)
	{	direction[0] = d.x();
		direction[1] = d.y();
		direction[2] = d.z();
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** sets the cutoff angle for this light - applicable if this is a spotlight */
	public void setCutoffAngle(float coa)
	{	cutoffAngle = coa;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** sets the off axis exponent for this light - applicable if this is a spotlight */
	public void setOffAxisExponent(float oae)
	{	offAxisExponent = oae;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** sets this light's LightType to either POSITIONAL or SPOTLIGHT */
	public void setType(LightType t)
	{	float type;
		lightType = t;
		if (lightType == LightType.POSITIONAL) type = 0.0f; else type = 1.0f;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}
	
	/** enables this light (turns it on) */
	public void enable()
	{	enabled = 1;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}
	
	/** disables this light (turns it off) */
	public void disable()
	{	enabled = 0;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}
	
	/** if this light is on, turn it off (and vice versa) */
	public void toggleOnOff()
	{	enabled = 1.0f - enabled;
		LightManager lm = engine.getLightManager();
		lm.setHasChanged();
	}

	/** returns this light's LightType (returns POSITIONAL or SPOTLIGHT */
	public LightType getLightType() { return lightType; }
	
	/** returns a boolean true if this light is enabled (on), or false if this light is disabled (off) */
	public boolean isEnabled() { return (enabled == 1.0f); }

	/** gets a reference to the float array containing this light's ambient RGB characteristic */
	public float[] getAmbient() { return ambient; }

	/** gets a reference to the float array containing this light's diffuse RGB characteristic */
	public float[] getDiffuse() { return diffuse; }

	/** gets a reference to the float array containing this light's specular RGB characteristic */
	public float[] getSpecular() { return specular; }

	/** gets a reference to the float array containing this light's world location (x,y,z) */
	public float[] getLocation() { return location; }

	/** gets a copy of this light's world location (x,y,z) as a Vector3f */
	public Vector3f getLocationVector() { return (new Vector3f(location)); }


	/** gets this light's constant attenuation factor, if applicable */
	public float getConstantAttenuation() { return constantAttenuation; }

	/** gets this light's linear attenuation factor, if applicable */
	public float getLinearAttenuation() { return linearAttenuation; }

	/** gets this light's quadratic attenuation factor, if applicable */
	public float getQuadraticAttenuation() { return quadraticAttenuation; }

	/** gets this light's range (not yet implemented) */
	public float getRange() { return range; }

	/** gets this light's direction - applicable for a spotlight */
	public float[] getDirection() { return direction; }

	/** gets this light's cutoff angle - applicable for a spotlight */
	public float getCutoffAngle() { return cutoffAngle; }

	/** gets this light's off axis exponent - applicable for a spotlight */
	public float getOffAxisExponent() { return offAxisExponent; }
}