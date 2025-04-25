package tage.shapes;
import java.io.*;
import java.lang.Math;
import java.nio.*;
import java.util.*;
import java.awt.Color;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;
//import org.joml.*;


public class WaterTerrain extends TerrainPlane{
    private int noiseTexture, noiseDimensions = 256;
    private double[][][] noise = new double[noiseDimensions][noiseDimensions][noiseDimensions];
		//tex coords are in first two dimensions with third moving to change the exact image
    private java.util.Random random = new java.util.Random();
    private GL4 gl; //needed to create a 3d texture in memory
	private double blurriness = 0.2; // tunable haziness of texture
	private double intensity = 100.0; // tunable quantity of waves

    
    private int d = 0; //changes where in the texture is getting sent to the vertex shader
    //d += elapsedTime * .000002; if (d >= 0.99f) d = 0.01f;
		//from csc155 example code 14.8 which moved view through texture in frag shader which will need an analogue in the vertex shader
			//line 70 of StandardVert.glsl
		//fragColor = texture(s,vec3(tc.x, tc.y, d));
		//model off heightmap in standardVert.glsl
	//heightmapping code will need some changing in RenderObjectStandard.java in tage/objectRenderers

    public WaterTerrain(){//MyGame g){ //thought I had to take GLContext from RenderSystem
        super();
        gl = (GL4) GLContext.getCurrentGL();
//        gl = g.getEngine().getRenderSystem().getGLCanvas();
        generateNoise();	
		noiseTexture = buildNoiseTexture();
    }

	public int getHeightMap(){ return noiseTexture; }

	private void fillDataArray(byte data[])
	{ double zoom = 64.0;
	  for (int i=0; i<noiseDimensions; i++)
	  { for (int j=0; j<noiseDimensions; j++)
	    { for (int k=0; k<noiseDimensions; k++)
	      {	float brightness = 1.0f - (float) turbulence(i,j,k,zoom) / 256.0f;
			Color c = new Color(brightness, brightness, 1.0f, 1.0f);
	        data[i*(noiseDimensions*noiseDimensions*4)+j*(noiseDimensions*4)+k*4+0] = (byte) c.getRed();
	        data[i*(noiseDimensions*noiseDimensions*4)+j*(noiseDimensions*4)+k*4+1] = (byte) c.getGreen();
	        data[i*(noiseDimensions*noiseDimensions*4)+j*(noiseDimensions*4)+k*4+2] = (byte) c.getBlue();
	        data[i*(noiseDimensions*noiseDimensions*4)+j*(noiseDimensions*4)+k*4+3] = (byte) 0;
	} } } }

	private int buildNoiseTexture()
	{	//GL4 gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseDimensions*noiseDimensions*noiseDimensions*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseDimensions, noiseDimensions, noiseDimensions);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				noiseDimensions, noiseDimensions, noiseDimensions, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	private void generateNoise()
	{	for (int x=0; x<noiseDimensions; x++)
		{	for (int y=0; y<noiseDimensions; y++)
			{	for (int z=0; z<noiseDimensions; z++)
				{	noise[x][y][z] = random.nextDouble();
	}	}	}	}
	
	private double smoothNoise(double zoom, double x1, double y1, double z1)
	{	//get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		//neighbor values that wrap
		double x2 = x1 - 1; if (x2<0) x2 = (Math.round(noiseDimensions / zoom)) - 1;
		double y2 = y1 - 1; if (y2<0) y2 = (Math.round(noiseDimensions / zoom)) - 1;
		double z2 = z1 - 1; if (z2<0) z2 = (Math.round(noiseDimensions / zoom)) - 1;

		//smooth the noise by interpolating
		double value = 0.0;
		value += fractX       * fractY       * fractZ       * noise[(int)x1][(int)y1][(int)z1];
		value += (1.0-fractX) * fractY       * fractZ       * noise[(int)x2][(int)y1][(int)z1];
		value += fractX       * (1.0-fractY) * fractZ       * noise[(int)x1][(int)y2][(int)z1];	
		value += (1.0-fractX) * (1.0-fractY) * fractZ       * noise[(int)x2][(int)y2][(int)z1];
				
		value += fractX       * fractY       * (1.0-fractZ) * noise[(int)x1][(int)y1][(int)z2];
		value += (1.0-fractX) * fractY       * (1.0-fractZ) * noise[(int)x2][(int)y1][(int)z2];
		value += fractX       * (1.0-fractY) * (1.0-fractZ) * noise[(int)x1][(int)y2][(int)z2];
		value += (1.0-fractX) * (1.0-fractY) * (1.0-fractZ) * noise[(int)x2][(int)y2][(int)z2];
		
		return value;
	}

	private double turbulence(double x, double y, double z, double zoom)
	{	double sum = 0.0, maxZoom = zoom, w=Math.log(zoom)/Math.log(2)+1;
		while(zoom >= 0.9)
		{	sum += (smoothNoise(zoom, x/zoom, y/zoom, z/zoom) * zoom)/w + (w-1)/(2*w);
			zoom = zoom / 2.0; w--;
		}
		sum = 1024.0 * sum / maxZoom - 97.6;
		sum = 256.0 * logistic(sum - intensity);
		return sum;
	}

	private double logistic(double x)
	{	
		return (1.0/(1.0+Math.pow(2.718,-blurriness*x)));
	}
}
