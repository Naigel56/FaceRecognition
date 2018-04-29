package it.polimi.inginf.camera.m754986;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import it.polimi.inginf.camera.m754986.Camera_assistant_754986Activity;




public class camera_view extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	// create surface holder to properly display images
	private SurfaceHolder sholder;
	// openCV camera
    private VideoCapture camera;
    // Mat for RBG and BW images
    private Mat colorMat = new Mat();
    private Mat grayMat = new Mat();
    // counter of thread execution
    private int numThread = 0;
    // paint used to display fps and infos
    private Paint paint = new Paint();
    private Paint paintInfos = new Paint();
    // width and height parameters 
    private int swidth = 0;
    private int sheight = 0;
    // classifiers
    private CascadeClassifier faces;
    private CascadeClassifier eyes;
    // string for infos
    private String extraInfos = "";
    private int positionInfos;
    
    // FPS VARIABLES: could be moved in a separate class with other parts from FPS code
    private long lastTime;
    private int lastNumFrame = 1;
    private float afps = 0;
    // system start time
    private long startTime;
    
    
    
    
    
    
    
    // initialize surfaceholder and timer
	public camera_view(Context context) {
		super(context);
		// assign surfaceholder to manipulate content
		sholder = getHolder();
		sholder.addCallback(this);

        Log.i("surfaceView", "Nuova SurfaceView: " + this.getClass());
        
        // load haar xml files
        try {
	        // create inputstream for faces and eyes
        	InputStream facesInput = context.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
        	InputStream eyesInput = context.getResources().openRawResource(R.raw.haarcascade_eye);
	        
        	// create temporary dirs and files
	        File directory = context.getDir("tempDir", Context.MODE_PRIVATE);
	        File tmpFileFaceInput = new File(directory, "haarcascade_frontalface_default.xml");
	        File tmpFileEyesInput = new File(directory, "haarcascade_eye.xml");
	        
	        FileOutputStream tmpFileFaceOutput = new FileOutputStream(tmpFileFaceInput);
	        FileOutputStream tmpFileEyesOutput = new FileOutputStream(tmpFileEyesInput);
	        
	        // read facesInput
	        byte[] buffer = new byte[8192];
	        int bytesRead;
	        while ((bytesRead = facesInput.read(buffer)) != -1) {
	        	tmpFileFaceOutput.write(buffer, 0, bytesRead);
	        }
	        facesInput.close();
	        tmpFileFaceOutput.close();
	        
	        // read eyesInput
	        while ((bytesRead = eyesInput.read(buffer)) != -1) {
	        	tmpFileEyesOutput.write(buffer, 0, bytesRead);
	        }
	        eyesInput.close();
	        tmpFileEyesOutput.close();
	        
	        // assign cascade files to proper object
	        faces = new CascadeClassifier(tmpFileFaceInput.getAbsolutePath());
	        eyes = new CascadeClassifier(tmpFileEyesInput.getAbsolutePath());
	        
	        // delete files and directory
	        tmpFileFaceInput.delete();
	        tmpFileEyesInput.delete();
	        directory.delete();
	    
	        // eventually signals reading error
        } catch (IOException e) {
        	Log.e("file_input", "errore di lettura: "+e);
        }

	}
	
	
	// initialize surface with camera binding and start thread
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("surfaceView", "SurfaceCreated in esecuzione...");
	    // initialize camera using openCV function
		camera = new VideoCapture(Highgui.CV_CAP_ANDROID);
	    // if camera succesfully open, strat thread
		if (camera.isOpened()) {
			Log.i("surfaceView_surfaceCreated", "Camera aperta, run start");
	        (new Thread(this)).start();
	    // else put error in log and release camera to avoid errors
		} else {
	        camera.release();
	        camera = null;
	        Log.i("surfaceView_surfaceCreated", "Impossibile aprire camera, fatal error");
	    }
	}
	
	
	// set proper width and height on surfaceChanged event
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.i("surfaceView", "Evento surfaceChanged");
		
		synchronized (this) {
            // only if camera is correctly opened, set width and height
			if (camera != null && camera.isOpened()) {
                // find possible camera sizes
				List<Size> supportedSizes = camera.getSupportedPreviewSizes();
				// select the highest resolution according to native display resolution
				for(Size elem : supportedSizes) {
					if ((elem.height <= Camera_assistant_754986Activity.dispHeight) && (elem.height > sheight) && (elem.width <= Camera_assistant_754986Activity.dispWidth) && (elem.width > swidth)) {
						swidth = (int)elem.width;
						sheight = (int) elem.height;
						
					}
				}
				// set size for paints
				if (sheight > 400) {
					paint.setTextSize(30);
				} else {
					paint.setTextSize(20);
				}
				paintInfos.setTextSize(paint.getTextSize());

				// set frame width and height
            	camera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, swidth);
                camera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, sheight);
                
                Log.i("surfaceView_surfaceChanged", "Dimensioni preview settate a " + swidth + " x " + sheight);
            }
        }
	}
	

	// acquire and display images, eventually modified
	public void run() {
		Log.i("surfaceView", "Thread iniziato");

		// start the loop
        while (true) {
        	// FPS CODE: set startTime, initialize lastTime and set size of fps code
        	if (numThread == 0) {
        		startTime = System.currentTimeMillis();
        		lastTime = startTime;
        	}
        	// +1 to cicle counter
        	numThread++;
        	// initialize bitmap to assign camera source
        	Bitmap cameraBitmap = null;

            synchronized (this) {
                // check if camera is set to null and eventually break, to avoid crash when
            	// try to exit from app
            	if (camera == null) {
                    break;
            	}
            	// check if next image is ready to be processed (some frame could be lost)
                if (!camera.grab()) {
                    Log.i("run", "prossimo frame non pronto");
                    break;
                }

            	// take new frame from camera (see takeFrame function for more details)
            	cameraBitmap = takeFrame(camera); 	
            }

            // create canvas to draw image from camera and display it
            Canvas canvas = sholder.lockCanvas();
            canvas.drawBitmap(cameraBitmap, 0, 0, null);
            
            // FPS CODE: every 3 seconds calculate approximate fps
            if ((System.currentTimeMillis() - lastTime)>1000) {
            	afps = (((numThread - lastNumFrame)*1000)/(System.currentTimeMillis() - lastTime));
            	
            	lastNumFrame = numThread;
            	lastTime = System.currentTimeMillis();
            }
            // FPS CODE: select color based on average number of frame
            if (afps > 10) {
            	paint.setColor(Color.GREEN);
            } else if (afps < 5) {
            	paint.setColor(Color.RED);
            } else {
            	paint.setColor(Color.YELLOW);
            }
            // FPS CODE: draw fps infos
            canvas.drawText("fps: " + (int)afps, 20, sheight - 80, paint);
            
        	// display infos about selected parameters
            canvas.drawText(infoString(), 20, sheight - 40, paint);
            
            // eventually display extra infos
            if (Camera_assistant_754986Activity.helperParam) {
            	canvas.drawText(extraInfos, positionInfos, 40, paintInfos);
            }
                        
            // display canvas
            sholder.unlockCanvasAndPost(canvas);
            // free memory
            cameraBitmap.recycle();
            
            //Log.i("surfaceView", "Ciclo " + numThread + " completato");
        }
	}


	// take a frame from native camera using openCV functions and return a bitmap
	private Bitmap takeFrame(VideoCapture camera) {
		// retrieve color frame, used to display preview
		camera.retrieve(colorMat, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
		// retrieve gray frame, used for features recognition
		camera.retrieve(grayMat, Highgui.CV_CAP_ANDROID_GREY_FRAME);
			
		// initialize bitmap
	    Bitmap tmpBitmap = Bitmap.createBitmap(colorMat.cols(), colorMat.rows(), Bitmap.Config.ARGB_8888);
	    
	    // detect faces
	    List<Rect> foundFaces = new LinkedList<Rect>();
	    // also scale factor (third value) influences fps 
	    if (Camera_assistant_754986Activity.faceParam != 0) {
	    	// find faces
	    	faces.detectMultiScale(grayMat, foundFaces, 2, 2, 2, new Size(sheight*0.2*Camera_assistant_754986Activity.faceParam, sheight*0.2*Camera_assistant_754986Activity.faceParam));

		    // draw detected faces
		    for (Rect tmpRect:foundFaces) {
		    	Core.rectangle(colorMat, tmpRect.tl(), tmpRect.br(), new Scalar(0, 255, 255, 0), 2);
		    }
		    
		    // eventually find eyes
		    if (Camera_assistant_754986Activity.eyesParam == true) {
		    	List<Rect> foundEyes = new LinkedList<Rect>();
		    	eyes.detectMultiScale(grayMat, foundEyes, 2, 2, 2, new Size(sheight*0.08*Camera_assistant_754986Activity.faceParam, sheight*0.08*Camera_assistant_754986Activity.faceParam));
			    // draw detected eyes
			    for (Rect tmpRect:foundEyes) {
			    	Core.rectangle(colorMat, tmpRect.tl(), tmpRect.br(), new Scalar(0, 255, 255, 0), 2);
			    }
		    }
		    
		    // set parameters to call function to understand relative faces position
		    int mediumSxFaces = 0;
		    int mediumDxFaces = 0;
		    int numFaces = 0;
		    short position;
		    extraInfos = "";
		    
		    if (!foundFaces.isEmpty() && Camera_assistant_754986Activity.helperParam) {
		    	// found sum of sx and dx coords
		    	for (Rect tmpRect:foundFaces) {
		    		mediumSxFaces += tmpRect.tl().x;
		    		mediumDxFaces += tmpRect.br().x;
		    		numFaces++;
		    		//baluba = " | "+mediumSxFaces+" / "+mediumDxFaces+" / "+numFaces+" | ";
		    	}
		    	
		    	// call function to understand position relative to preview sizes
		    	position = findFacePosition(mediumSxFaces, mediumDxFaces, numFaces);
		    	
		    	// set infos to display
		    	setDisplayInfos(position);
		    }
 
	    }
	    
	    // convert Mat to bitmap and return
	    // ALWAYS check if matToBitmap return true, or returned tmpBitmap could be
	    // invalid and eventually cause crash
	    if (Utils.matToBitmap(colorMat, tmpBitmap)) {
	        return tmpBitmap;
	    // if not valid free bitmap and return null
	    } else {        
	        tmpBitmap.recycle();
	        return null;
	    }
	        
	}
	
	
	// set a string to display infos of selected parameters
	private String infoString() {
		String dispayInfo;
		if (Camera_assistant_754986Activity.faceParam == 0) {
    		dispayInfo = "Detection OFF";
    	} else if (Camera_assistant_754986Activity.faceParam == 1) {
    		if (Camera_assistant_754986Activity.eyesParam == true) {
    			dispayInfo = "Face detection: small | Eyes ON";
    		} else {
    			dispayInfo = "Face detection: small | Eyes OFF";
    		}
    	} else {
    		if (Camera_assistant_754986Activity.eyesParam == true) {
    			dispayInfo = "Face detection: big | Eyes ON";
    		} else {
    			dispayInfo = "Face detection: big | Eyes OFF";
    		}
    	}
		
		return dispayInfo;
	}
	
	
	// find position of the face
	// return 0 if central, negative number if too sx, positive number if too dx
	private short findFacePosition(int sx, int dx, int num) {
		// find medium distance from left border
		sx = sx/num;
		// find medium distance from right border
		dx = dx/num;
		dx = swidth - dx;
		// find relative difference
		int difference = sx - dx;
		
		// decide how much faces are centred
		int unit = swidth / 9;
		if(difference > 2*unit) {
			return -2;
		} else if ((difference <= 2*unit)&&(difference > unit)) {
			return -1;
		} else if ((difference <= unit)&&(difference > -unit)) {
			return 0;
		} else if ((difference <= -unit)&&(difference > -2*unit)) {
			return 1;
		} else {
			return 2;
		}
	}
	
	// set display infos
	private void setDisplayInfos(short id) {
		if (id == 0) {
			paintInfos.setColor(Color.GREEN);
			extraInfos = "OK - SCATTA";
			positionInfos = (swidth / 2) - (swidth/15);
		} else {
			paintInfos.setColor(Color.RED);
			
			if (id > 0) {
				extraInfos = "<-- TOO DX";
				positionInfos = (swidth / 2) + (swidth/10);
			} else {
				extraInfos = "TOO SX -->";
				positionInfos = (swidth / 2) - (swidth/7);
			}
		}		
	}
	

	// release camera on close
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("surfaceView", "SurfaceDestroyed");
        // check if camera is not yet released to avoid crash on exit
		if (camera != null) {
            synchronized (this) {
                camera.release();
                camera = null;
            }
        }
		
	}
	
}
