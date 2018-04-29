package it.polimi.inginf.camera.m754986;
// project developed by Lorenzo Cavazzi, id 754986, www.inginf.polimi.it
// lorenzo.cavazzi@mail.polimi.it
// Image analysis course (AA 2011), Vincenzo Caglioti, Alessandro Giusti

// Aim of the project is using openCV libraries to find features and giving
// realtime infos on smartphone display. Main goal is demonstrate if
// using haar-like features is feasible in a reliable way by measuring fps
// in different conditions.
// More infos in attached readme file


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;


public class Camera_assistant_754986Activity extends Activity {
	private MenuItem faceBig;
	private MenuItem faceSmall;
	private MenuItem faceOff;
	private MenuItem eyesOn;
	private MenuItem eyesOff;
	private MenuItem helperOn;
	private MenuItem helperOff;
	
	// default display dimensions
	public static int dispHeight;
	public static int dispWidth;
	
	// detection parameters
	// face: 0 = off, 1 = small, 2 = big
	public static short faceParam = 2;
	public static boolean eyesParam = false;
	public static boolean helperParam = true;
	
	// expose menu
	private Menu exposeMenu;
	
	
	
	
	// on activity init
	public Camera_assistant_754986Activity() {
		Log.i("activity", "Inizializzazione di " + this.getClass());
    }
	
	// on activity creation
    public void onCreate(Bundle savedInstanceState) {
    	Log.i("activity", "Attivita creata");
    	super.onCreate(savedInstanceState);
    	
    	// remove application title and system bar
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
    	// acquire display height and width
    	DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        dispHeight = displaymetrics.heightPixels;
        dispWidth = displaymetrics.widthPixels;
    	
        // set view
    	setContentView(new camera_view(this));
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	// expose menu, so it can be updated from others functions
    	exposeMenu = menu;
    	// draw menu
    	redraw_menu();
    	
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem mItem) {
        // change parameters based on selection
    	if (mItem == faceBig)
        	faceParam = 2;
        else if (mItem == faceSmall)
        	faceParam = 1;
        else if (mItem == faceOff) {
        	faceParam = 0;
    		eyesParam = false;
        } else if (mItem == eyesOn) {
        	if (faceParam == 0)
        		faceParam = 1;
        	eyesParam = true;
        } else if (mItem == eyesOff)
        	eyesParam = false;
        else if (mItem == helperOn)
        	helperParam = true;
        else if (mItem == helperOff)
        	helperParam = false;
        
    	// redraw menu
    	redraw_menu();
        
        return true;
    }
    
    // update menu with new parameters
    private void redraw_menu () {
    	// clear old menu options
    	exposeMenu.clear();
    	
    	// draw face detection options
    	if (faceParam == 0) {
    		faceBig = exposeMenu.add("feature size: Big");
    		faceSmall = exposeMenu.add("feature size: Small");
    	} else if (faceParam == 1) {
    		faceBig = exposeMenu.add("feature size: Big");
    		faceOff = exposeMenu.add("turn face OFF");
    	} else {
    		faceSmall = exposeMenu.add("feature size: Small");
    		faceOff = exposeMenu.add("turn face OFF");
    	}
    	
    	// draw eyes detection options
    	if (eyesParam == true) {
    		eyesOff = exposeMenu.add("turn eyes OFF");
    	} else {
    		eyesOn = exposeMenu.add("turn eyes ON");
    	}
    	
    	// turn on/off onscreen assistant
    	if (helperParam == true) {
    		helperOff = exposeMenu.add("turn helper OFF");
    	} else {
    		helperOn = exposeMenu.add("turn helper ON");
    	}

    }
    
}