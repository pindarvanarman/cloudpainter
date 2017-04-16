import java.net.URL;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

//
// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".
//
class CloudFaceDetect {
  public void run() {
    System.out.println("\nRunning DetectFaceDemo");

    // Create a face detector from the cascade file in the resources
    // directory.
    for (int num=1;num<13; num++) {
    //URL u = new URL("C:/development/workspace/CloudFaceCV/resources/lbpcascade_frontalface.xml");
try {    
    URL url2 = getClass().getResource("C:/development/workspace/CloudFaceCV/resources/lbpcascade_frontalface.xml");
    //CascadeClassifier faceDetector = new CascadeClassifier(getClass().getResource("resources/lbpcascade_frontalface.xml").getPath());
    CascadeClassifier faceDetector = new CascadeClassifier("C:/development/workspace/CloudFaceCV/resources/lbpcascade_frontalface.xml");
    CascadeClassifier eyeDetector = new CascadeClassifier("C:/development/workspace/CloudFaceCV/resources/haarcascade_eye.xml");
    CascadeClassifier mouthDetector = new CascadeClassifier("C:/development/workspace/CloudFaceCV/resources/haarcascade_mcs_mouth.xml");
    
    //Mat image = Highgui.imread(getClass().getResource("/lena.png").getPath());
    Mat image = Highgui.imread("C:/development/workspace/CloudFaceCV/"+num+".jpg");
    MatOfInt rejectLevels = new MatOfInt();
    MatOfDouble weightLevels = new MatOfDouble();
    // Detect faces in the image.
    // MatOfRect is a special container class for Rect.
    MatOfRect faceDetections = new MatOfRect();
    faceDetector.detectMultiScale(image, faceDetections, rejectLevels, weightLevels);
    
    MatOfRect eyeDetections = new MatOfRect(); 
    eyeDetector.detectMultiScale(image, eyeDetections, rejectLevels, weightLevels);
    
    MatOfRect mouthDetections = new MatOfRect(); 
    mouthDetector.detectMultiScale(image, mouthDetections, rejectLevels, weightLevels);

    System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

    // Draw a bounding box around each face.

    Rect faceRect = new Rect();
    for (Rect rect : faceDetections.toArray()) {
        if(faceRect==null || rect.width>faceRect.width) {
        	faceRect = rect;
        }
    }
    Core.rectangle(image, new Point(faceRect.x, faceRect.y), new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height), new Scalar(0, 255, 0), 10);
    
    Vector eyeRects = new Vector();
    int eyeDifference = 0;
    for (Rect rect : eyeDetections.toArray()) {
    	if (rect.width>faceRect.width/10 &&
    		rect.x > faceRect.x	&&
    		rect.y > faceRect.y	&&
    		rect.x < faceRect.x+faceRect.width	&&
    		rect.y < faceRect.y+faceRect.height	
    			) {
    		eyeRects.add(rect);
    	}
    }   
    
    //Hunters First Algorithm - programmed by Pindar
    //if more than 2 eyes
    //erase smallest eye
    int smallestEyeIndex = 0;
    Rect smallestEyeFound = new Rect(0,0,10000,10000);
    if (eyeRects.size()>2) {
    	for (int i=0;i<eyeRects.size();i++) {
    		if ( ((Rect)eyeRects.elementAt(i)).width < smallestEyeFound.width) {
    			smallestEyeFound = (Rect)eyeRects.elementAt(i);
    			smallestEyeIndex = i;
    		}
    	}
    	eyeRects.remove(smallestEyeIndex);
    }
    
    
    for (int i=0;i<eyeRects.size();i++) {
    	Rect rect = (Rect)eyeRects.elementAt(i);
    	Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0 , 0), 10);
    }
    
    
    int averageEyeY = 0;
    int averageEyeHeight = 0;
    for (int i=0; i<eyeRects.size(); i++){
    	averageEyeY += (((Rect)eyeRects.get(i)).y+(((Rect)eyeRects.get(i)).height/2));
    	averageEyeHeight += (((Rect)eyeRects.get(i)).height);
    }
    if (eyeRects.size()>=1) {
    	averageEyeY = averageEyeY/eyeRects.size();
      averageEyeHeight = averageEyeHeight/eyeRects.size();
    }
    Rect mouthRect = new Rect();
    for (Rect rect : mouthDetections.toArray()) {
    	if (rect.width<faceRect.width/2  &&
  			rect.width>faceRect.width/5  &&
  			rect.y > averageEyeY+averageEyeHeight &&
  			rect.x > faceRect.x	&&
    		rect.y > faceRect.y	&&
    		rect.x < faceRect.x+faceRect.width	&&
    		rect.y < faceRect.y+faceRect.height	
    			) {
    		if (mouthRect.width == 0) {
    			mouthRect = rect;
    		} else {
    			if (rect.y < mouthRect.y) {
    				mouthRect = rect;
    			}
    		}
    		
    	}
    }
    Core.rectangle(image, new Point(mouthRect.x, mouthRect.y), new Point(mouthRect.x + mouthRect.width, mouthRect.y + mouthRect.height), new Scalar(255, 0 , 0), 10);
    
    int sym = CalculateFaceSymmetry(faceRect, (Rect)eyeRects.elementAt(0), (Rect)eyeRects.elementAt(1), mouthRect);
    // Save the visualized detection.

    int facing = CalculateDirectionOffFace(faceRect, (Rect)eyeRects.elementAt(0), (Rect)eyeRects.elementAt(1), mouthRect);
    // Save the visualized detection.
    String facingDir = "";
    if (facing<-20) {
    	facingDir = "right";
    } else  if (facing>20) {
    	facingDir = "left";
    } else {
    	facingDir = "forward";
    }
    Rect crop = getCropping(faceRect, facing);
  		
	Core.rectangle(image, new Point(crop.x, crop.y), new Point(crop.x + crop.width, crop.y + crop.height), new Scalar(0, 0 , 255), 10);
    Core.putText(image, "rating: "+sym+"" , new Point(50, 100), 2, 3, new Scalar(0, 0 , 0), 5);//(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0 , 255), 10);
    Core.putText(image, "looking "+facingDir+": "+facing+"" , new Point(50, 200), 2, 3, new Scalar(0, 0 , 0), 5);//(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0 , 255), 10);
	

    
    
    String filename = "ratingsfaceDetection_"+num+".png";
    System.out.println(String.format("Writing %s", filename));
    Highgui.imwrite(filename, image);
    
    
    } catch (Exception e) {
    }
    }
    }
    
  public int CalculateFaceSymmetry(Rect face, Rect eyeOne, Rect eyeTwo, Rect mouth) {
	  //calculate difference of eye levels
	  //calculate center of eyes vs middle of face horizontally
	  //calculate mouth to center of eyes
	  double differenceOfEyeLevel = Math.abs(eyeOne.y-eyeTwo.y)*2;
	  double distanceOfEyesFromCenterOfFace = Math.abs( (face.x+face.width/2) - (((eyeOne.x+(eyeOne.width/2)) + (eyeTwo.x+(eyeTwo.width/2)))/2)  );
	  double distanceOfMouthFromCenterOfFace = Math.abs( (face.x+face.width/2) - (mouth.x+(mouth.width/2))  );
	  double distanceOfEyesFromCenterOfMouth = Math.abs( (mouth.x+(mouth.width/2)) - (((eyeOne.x+(eyeOne.width/2)) + (eyeTwo.x+(eyeTwo.width/2)))/2)  );
	  double totalError = differenceOfEyeLevel + distanceOfEyesFromCenterOfFace + distanceOfMouthFromCenterOfFace + distanceOfEyesFromCenterOfMouth;
	  double symmetryScore = 40/totalError;
	  int percentage = (int)(symmetryScore*100);
      return percentage;
  }
  
  public int CalculateDirectionOffFace(Rect face,  Rect eyeOne, Rect eyeTwo, Rect mouth) {
	  int faceCenter = (face.x+(face.width/2));
	  int mouthCenter = (mouth.x+(mouth.width/2));
	  int eyeCenter = (  ((eyeOne.x+(eyeOne.width/2)) + (eyeTwo.x+(eyeTwo.width/2)))      /2);
	  
	  int distanceOfEyesFromCenter = (( mouthCenter - eyeCenter  ));// + (faceCenter - eyeCenter);
      return distanceOfEyesFromCenter;
  }
  
  public Rect getCropping(Rect faceRect, int facing) {
	  Rect newCrop = new Rect();
	  //Rect aspectRatio = new Rect(0,0,880,1144);
	  Rect aspectRatio = new Rect(0,0,1144,1144);
	  int widthDiff = aspectRatio.width - faceRect.width;
	  int widthShift = 0;
	    if (facing<-20) {
	    	//widthShift = -(widthDiff/4)*3;
	    	widthShift = (widthDiff/4);
	    } else  if (facing>20) {
	    	//widthShift = (widthDiff/4)*3;
	    	widthShift = -(widthDiff/4);
	    } else {
	    	widthShift = 0;
	    }
	    int val = (faceRect.x-(widthDiff/2))+widthShift;
	  aspectRatio.x = val;
	  
	  
	  
	  int heightDiff = aspectRatio.height - faceRect.height;
	  aspectRatio.y = faceRect.y-(heightDiff/3);
	  return aspectRatio;
  }
  

}