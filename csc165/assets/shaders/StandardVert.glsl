#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 vertNormal;

out vec2 tc;
out vec3 varyingNormal;
out vec3 varyingVertPos;
out vec3 vVertPos;

out vec4 varyingColor;

struct Light
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
	float constantAttenuation;
	float linearAttenuation;
	float quadraticAttenuation;
	float range;
	vec3 direction;
	float cutoffAngle;
	float offAxisExponent;
	float type;
	float enabled;
};
struct Material
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

Light light;

uniform vec4 globalAmbient;
uniform Material material;
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
uniform int envMapped;
uniform int has_texture;
uniform int tileCount;
uniform int heightMapped;
uniform int hasLighting;
uniform int solidColor;
uniform vec3 color;
uniform int num_lights;
uniform int fields_per_light;

uniform int colorSplashPointer;
	//reference TumblingCube for vert/frag shaders if need be


layout (std430, binding=0) buffer lightBuffer { float lightArray[]; };
layout (binding = 0) uniform sampler2D samp;
layout (binding = 1) uniform samplerCube t;
layout (binding = 2) uniform sampler2D height;

void main(void)
{	vVertPos = (v_matrix * m_matrix * vec4(vertPos,1.0)).xyz;
	varyingVertPos = (m_matrix * vec4(vertPos,1.0)).xyz;
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;

	// Most of the time this height offset is 0.
	// If this is a terrain plane, and has a height map, then this will do the height mapping.
	vec4 p = vec4(vertPos.x, vertPos.y + (texture(height,texCoord)).r, vertPos.z, 1.0);

	// Compute the texture coordinates depending on the specified tileFactor 
	tc = texCoord;
	tc = tc * tileCount;
	gl_Position = p_matrix * v_matrix * m_matrix * p;
	
//	varyingColor = vec4(1.0, 0.0, 0.0, 1.0);							//works as expected without lighting
	varyingColor = vec4(vertPos,1.0)*0.5 + vec4(0.5, 0.5, 0.5, 0.5);	//works acceptably without lighting. Each point maintains a color, so it does not change color with the camera angle
//need to alter ObjShape class so you can get the largest number of x, y, z of objects to make it work with objects whose model matrix does not fall between -1 and 1

//	varyingColor = vec4(vertPos, 1.0);									//uses the raw vertices as colors, so very saturated and looks a bit off 
//	varyingColor = vec4(vVertPos, 1.0);									//no
//	varyingColor = vec4(varyingVertPos, 1.0);							//does not work
//	varyingColor = p_matrix * v_matrix * m_matrix * vec4(vertPos,1.0);		//getting there. changes color as the object moves from one half of the screen to another
//	varyingColor = vec4();
	//whatever holds the location on the screen put into vec4(vertPos,1.0)*0.5 + vec4(0.5, 0.5, 0.5, 0.5);
}
