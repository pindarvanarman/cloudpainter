import java.io.File;
import java.net.URL;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

//
// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".
//

class CloudFaceLines {
	
	public static void main(String[] args) {
		CloudFaceLines cfl = new CloudFaceLines();
		cfl.run();
	}


  public void run() {
    System.out.println("\nRunning Line Detector");

    //libopencv_java248.dylib
    
     //System.load(new File("C:/development/workspace/CloudFaceCV/resources/libopencv_java248.dylib").getAbsolutePath());

    //linesoutbw_50_5_90_10_10_4_
    
    //mary 
    //marydream_75_5_90_10_10_2_
    for (int lt = 75; lt<=75; lt=lt+25 ) {
    	 for (int r = 5; r<=5; r+=2 ) {
    		 for (int e1 = 90; e1<=90; e1+=90 ) {
         		for (int e2 = 5; e2<=30; e2+=10 ) {
             		for (int e3 = 5; e3<=30; e3+=10 ) {
                 		for (int e4 = 2; e4<=2; e4+=2 ) {

    //Mat img = Highgui.imread("data/topdown-6.jpg");
     Mat img = Highgui.imread("C:/development/workspace/CloudFaceCV/1.jpg");

	
	// load the image
	
	
	// generate gray scale and blur
	Mat gray = new Mat();
	Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
	Imgproc.blur(gray, gray, new Size(3, 3));
	
	// detect the edges
	Mat edges = new Mat();
	int lowThreshold = lt;
	int ratio = r;
	Imgproc.Canny(gray, edges, lowThreshold, lowThreshold * ratio);
	
	Mat lines = new Mat();
	Imgproc.HoughLinesP(edges, lines, 1, Math.PI / e1, e2, e3, e4);
	
	
	
	for(int i = 0; i < lines.cols(); i++) {
		double[] val = lines.get(0, i);
		Core.line(img, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(0, 0, 255), 2);
		
		 //SendStrokesToElasticSearchCluster(String indexName, int strokeNumber, double startX, double startY, double endX, double endY, int width, int height, String painting)
	}
	
    Highgui.imwrite("C:/development/workspace/CloudFaceCV/corinne11_1200_"+lt+"_"+r+"_"+e1+"_"+e2+"_"+e3+"_"+e4+"_.jpg", img);
	//Mat image = Highgui.imread();
                 		}
              	    }
          	    }
     	    }
    	 }
    }

	//ImgWindow.newWindow(edges);
	//ImgWindow.newWindow(gray);
	//ImgWindow.newWindow(img);
    
  
  }

 

}