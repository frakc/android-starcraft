package hotheart.starcraft.graphics.render.opengl;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.opengl.GLUtils;

import hotheart.starcraft.files.GrpFile;
import hotheart.starcraft.graphics.StarcraftPalette;
import hotheart.starcraft.graphics.render.RenderImage;

public class OpenGLRenderImage extends RenderImage {

	class Frame {
		public IntBuffer mVertexBuffer;
		public int texture;

		public Frame(int i, GL10 gl) {
			int[] ids = new int[1];
			gl.glGenTextures(1, ids, 0);
			texture = ids[0];

			int[] coords = { image.widths[i], image.heights[i],
					image.widths[i], 0, 0, 0, 0, image.heights[i] };

			ByteBuffer vbb = ByteBuffer.allocateDirect(coords.length * 4);
			vbb.order(ByteOrder.nativeOrder());
			mVertexBuffer = vbb.asIntBuffer();
			mVertexBuffer.put(coords);
			mVertexBuffer.position(0);

			gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);

			gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
					GL10.GL_REPLACE);

			Bitmap bitmap = image.createBitmap(i,
					StarcraftPalette.normalPalette);

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, Bitmap
					.createScaledBitmap(bitmap, 16, 16, true), 0);
			bitmap.recycle();

			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_LINEAR);

		}
	}

	FloatBuffer mTexBuffer;
	ByteBuffer mIndexBuffer;

	// int[] coords = { 1, 1, 1, 0, 0, 0, 0, 1 };
	float[] texCoords = { 1, 1, 1, 0, 0, 0, 0, 1 };
	byte[] vertex_strip = { 1, 0, 2, 3 };

	GrpFile image;

	Frame[] frames = null;

	synchronized void init(GL10 gl) {
		if (frames != null)
			return;

		ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTexBuffer = tbb.asFloatBuffer();
		mTexBuffer.put(texCoords);
		mTexBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(vertex_strip.length);
		mIndexBuffer.order(ByteOrder.nativeOrder());
		mIndexBuffer.put(vertex_strip);
		mIndexBuffer.position(0);

		frames = new Frame[image.count];
		for (int i = 0; i < frames.length; i++)
			frames[i] = new Frame(i, gl);
		//
		// int[] ids = new int[1];
		// gl.glGenTextures(1, ids, 0);
		// tex = ids[0];
		//
		// gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		//
		// gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		// GL10.GL_REPLACE);
		//
		// Bitmap bitmap = Bitmap.createBitmap(2, 2, Config.RGB_565);
		// bitmap.setPixel(0, 0, Color.RED);
		// bitmap.setPixel(1, 0, Color.GREEN);
		// bitmap.setPixel(0, 1, Color.GREEN);
		// bitmap.setPixel(1, 1, Color.RED);
		//
		// GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		// bitmap.recycle();
		//
		// gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
		// GL10.GL_LINEAR);
		// gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
		// GL10.GL_LINEAR);

	}

	OpenGLRender render;

	public OpenGLRenderImage(OpenGLRender r, GrpFile data) {
		render = r;
		image = data;
	}

	@Override
	public void draw(int x, int y, boolean align, int baseFrame, int angle,
			int function, int remapping, int teamColor) {

		init(render.gl);

		render.gl.glPushMatrix();

		render.gl.glTranslatex(x, y, 0);
		
		render.gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		render.gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		render.gl.glVertexPointer(2, GL10.GL_FIXED, 0,
				frames[baseFrame].mVertexBuffer);
		render.gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);

		render.gl.glActiveTexture(GL10.GL_TEXTURE0);
		render.gl.glBindTexture(GL10.GL_TEXTURE_2D, frames[baseFrame].texture);

		render.gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4,
				GL10.GL_UNSIGNED_BYTE, mIndexBuffer);

		render.gl.glPopMatrix();
	}

}
