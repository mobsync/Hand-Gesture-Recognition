package handgesture;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

/**
 * Created by Kinath on 8/6/2016.
 */
public class SURFDetector {
	
	public  Map<Integer,String> mapList=new TreeMap<Integer,String>(); 
	 public static Object[] listData1 = {
	  };
	public  List<String> outList=new ArrayList<String>();
	public static void main(String ars[]) {
		
		//main("C:\\temp\\detection\\sameer\\","1_file1553406643554.jpg");
	}
	int largest=0;
  	String imageToReturn;
    public  String  main(String dir,String fileLocation,Object[] listData) {

        File lib = null;
        String os = System.getProperty("os.name");
        String bitness = System.getProperty("sun.arch.data.model");

        if (os.toUpperCase().contains("WINDOWS")) {
            if (bitness.endsWith("64")) {
                lib = new File("lib//x64//" + System.mapLibraryName("opencv_java2411"));
            } else {
                lib = new File("lib//x86//" + System.mapLibraryName("opencv_java2411"));
            }
        }

        System.out.println(lib.getAbsolutePath());
        System.load(lib.getAbsolutePath());

        String bookObject =fileLocation;
        System.out.println(bookObject);
        String files[]=new File(dir+"").list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.endsWith(".jpg") || name.endsWith(".png");
			}
		} );
  	
        List<String> imageList=new ArrayList<String>();
  	if(files!= null && files.length>0){
  	
  	for(int index=0;index<files.length;index++){
  		
  		try{
  		
  		 String bookScene = dir+""+files[index];
  		 System.out.println(bookScene);

         //System.out.println("Started....");
         //System.out.println("Loading images...");
         Mat objectImage = Highgui.imread(bookObject, Highgui.CV_LOAD_IMAGE_COLOR);
         Mat sceneImage = Highgui.imread(bookScene, Highgui.CV_LOAD_IMAGE_COLOR);

         MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
         FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SURF);
         //System.out.println("Detecting key points...");
         featureDetector.detect(objectImage, objectKeyPoints);
         KeyPoint[] keypoints = objectKeyPoints.toArray();
         //System.out.println(keypoints);

         MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
         DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
         //System.out.println("Computing descriptors...");
         descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);

         // Create the matrix for output image.
         Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
         Scalar newKeypointColor = new Scalar(255, 0, 0);

        // System.out.println("Drawing key points on object image...");
         Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

         // Match object image with the scene image
         MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
         MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
         //System.out.println("Detecting key points in background image...");
         featureDetector.detect(sceneImage, sceneKeyPoints);
         //System.out.println("Computing descriptors in background image...");
         descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

         Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
         Scalar matchestColor = new Scalar(0, 255, 0);

         List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
         DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        // System.out.println("Matching object and scene images...");
         descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

         //System.out.println("Calculating good match list...");
         LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();
         
         float nndrRatio = 0.7f;

         for (int i = 0; i < matches.size(); i++) {
             MatOfDMatch matofDMatch = matches.get(i);
             DMatch[] dmatcharray = matofDMatch.toArray();
             DMatch m1 = dmatcharray[0];
             DMatch m2 = dmatcharray[1];

             if (m1.distance <= m2.distance * nndrRatio) {
                 goodMatchesList.addLast(m1);

             }
         }

         if (goodMatchesList.size() >= 7) {
             System.out.println("Object Found!!!"+goodMatchesList.size());
             
             List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
             List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();
             
             LinkedList<Point> objectPoints = new LinkedList<>();
             LinkedList<Point> scenePoints = new LinkedList<>();

             for (int i = 0; i < goodMatchesList.size(); i++) {
                 objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
                 scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
             }

             MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
             objMatOfPoint2f.fromList(objectPoints);
             MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
             scnMatOfPoint2f.fromList(scenePoints);

             Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

             Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
             Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

             obj_corners.put(0, 0, new double[]{0, 0});
             obj_corners.put(1, 0, new double[]{objectImage.cols(), 0});
             obj_corners.put(2, 0, new double[]{objectImage.cols(), objectImage.rows()});
             obj_corners.put(3, 0, new double[]{0, objectImage.rows()});

             //System.out.println("Transforming object corners to scene corners...");
             Core.perspectiveTransform(obj_corners, scene_corners, homography);

             Mat img = Highgui.imread(bookScene, Highgui.CV_LOAD_IMAGE_COLOR);

             Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 4);
             Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 4);
             Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
             Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);

            // System.out.println("Drawing matches image...");
             MatOfDMatch goodMatches = new MatOfDMatch();
             goodMatches.fromList(goodMatchesList);

             Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

             String time=""+System.currentTimeMillis();
             String img1="c:\\temp\\outputImage"+time+".jpg";
             Highgui.imwrite(img1, outputImage);
             outList.add(img1);
             img1="c:\\temp\\matchoutput"+time+".jpg";
             mapList.put(goodMatchesList.size(),bookScene+":-:"+img1);	
             outList.add(img1);
             Highgui.imwrite(img1, matchoutput);
             img1="c:\\temp\\img"+time+".jpg";
             outList.add(img1);
             Highgui.imwrite(img1, img);
         } else {
             System.out.println("Object Not Found");
         }
  		}
  		catch(Exception e)
  		{}
  	}
  	}
  	System.out.println(mapList);
  	int count=0;
  	
  	for(Map.Entry<Integer, String> entry:mapList.entrySet())
  	{
  		if(entry.getKey()>largest){
  			largest=entry.getKey();
  			imageToReturn=entry.getValue();
  		}
  		if(count<1){
  		imageList.add(entry.getValue());
  		count++;
  		}
  	}
  	count=0;
  	for(int ii=0;ii<outList.size();ii++)
  	{
  		
  	  		imageList.add(outList.get(ii));
  	  		count++;
  	  		
  	}
  	
        
        if(imageList!=null && imageList.size()>0){
        	listData1=new String[]{imageToReturn.split(":-:")[1]};
        	System.out.println("listData"+imageToReturn.split(":-:")[1]);
     //  Slideshow show=new Slideshow(imageList.toArray());
      // show.setVisible(true);
        return imageToReturn.split(":-:")[0];
        }
        else
        {
        	//JOptionPane.showMessageDialog(null, "No Result Found");
        	return "";
        }

       
    }
}
