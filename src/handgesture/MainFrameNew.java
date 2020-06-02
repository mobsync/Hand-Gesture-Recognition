package handgesture;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;

public class MainFrameNew extends javax.swing.JFrame implements WindowListener
{
	String firstImage="";
	static int count=0;
	BufferedWriter bw = null;
	FileWriter fw = null;
	boolean toStopRecog=false;
  static
  {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  private Boolean begin = false;
  private Boolean firstFrame = true;
  private VideoCapture video = null;
  private CaptureThread thread = null;
  private MatOfByte matOfByte = new MatOfByte();
  private BufferedImage bufImage = null;
  private InputStream in;
  private Mat frameaux = new Mat(500, 620, CvType.CV_8UC3);
  private Mat frame = new Mat(500, 620, CvType.CV_8UC3);
  private Mat lastFrame = new Mat(500, 620, CvType.CV_8UC3);
  private Mat currentFrame = new Mat(500, 620, CvType.CV_8UC3);
  private Mat processedFrame = new Mat(500, 620, CvType.CV_8UC3);
  private ImagePanel image;
  private BackgroundSubtractorMOG bsMOG = new BackgroundSubtractorMOG();
  private int savedelay = 0;
  String currentDir = "";
  String detectionsDir = "detections";
  String username="";
  String characterName=null;
  Object[] list = {
  };
  public MainFrameNew()
  {
    initComponents();
    image = new ImagePanel(new ImageIcon("figs/320x240.gif").getImage());
    jPanelSource1.add(image, BorderLayout.CENTER);

    currentDir = "c:\\temp\\detection";//Paths.get(".").toAbsolutePath().normalize().toString();
    File fileDirecory=new File(currentDir);
    if(!fileDirecory.exists())
    	fileDirecory.mkdirs();
    // new java.io.File( "." ).getCanonicalPath();
    //System.out.println("Current dir: " + currentDir);
    //System.out.println("Detections dir: " + detectionsDir);
    username=JOptionPane.showInputDialog(null,"Please Enter Your Name");
    File userDirectory=new File(fileDirecory+File.separator+username);
    
    if(!userDirectory.exists())
    {
    	userDirectory.mkdirs();
    }
    
    
    File fileDirecoryTest=new File(fileDirecory+File.separator+username+File.separator+"test");
    if(!fileDirecoryTest.exists())
    	fileDirecoryTest.mkdirs();
    jTextFieldSaveLocation.setText(userDirectory.getAbsolutePath());
    detectionsDir=userDirectory.getAbsolutePath();
    addWindowListener(this);
  }

  private void start()
  {
    //System.out.println("You clicked the start button!");
	  count=0;
    if(!begin)
    {
      int sourcen = Integer.parseInt(jTextFieldSource1.getText());
      System.out.println("Opening source: " + sourcen);

      video = new VideoCapture(sourcen);

      if(video.isOpened())
      {
        thread = new CaptureThread();
        thread.start();
        begin = true;
        firstFrame = true;
      }
    }
  }

  private void stop()
  {
    //System.out.println("You clicked the stop button!");
	  count=0;
	  toStopRecog=false;
    if(begin)
    {
      try
      {
        Thread.sleep(500);
      }
      catch(Exception ex)
      {
      }
      video.release();
      begin = false;
    }
  }

  public static String getCurrentTimeStamp()
  {
    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//dd/MM/yyyy
    Date now = new Date();
    String strDate = sdfDate.format(now);
    return strDate;
  }

  public ArrayList<Rect> detection_contours(Mat frame, Mat outmat)
  {
    Mat v = new Mat();
    Mat vv = outmat.clone();
    List<MatOfPoint> contours = new ArrayList();
    Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

    double maxArea = 100;
    int maxAreaIdx;
    Rect r;
    ArrayList<Rect> rect_array = new ArrayList();

    for(int idx = 0; idx < contours.size(); idx++)
    {
      Mat contour = contours.get(idx);
      double contourarea = Imgproc.contourArea(contour);
      if(contourarea > maxArea)
      {
        // maxArea = contourarea;
        maxAreaIdx = idx;
        r = Imgproc.boundingRect(contours.get(maxAreaIdx));
        rect_array.add(r);
        Imgproc.drawContours(frame, contours, maxAreaIdx, new Scalar(0, 0, 255));
      }
    }

    v.release();
    return rect_array;
  }

  
  
  
  
  int counttoStart=0;
  
  
  class CaptureThread extends Thread
  {
    @Override
    public void run()
    {
      if(video.isOpened())
      {
        while(begin == true)
        {
          //video.read(frameaux);
        	if(toStopRecog)
        	setTitle(""+(50-counttoStart++));
        	
          video.retrieve(frameaux);
          Imgproc.resize(frameaux, frame, frame.size());
          frame.copyTo(currentFrame);
          
          if(firstFrame)
          {
            frame.copyTo(lastFrame);
            firstFrame = false;
            continue;
          }

          if(jCheckBoxMotionDetection.isSelected())
          {
            Imgproc.GaussianBlur(currentFrame, currentFrame, new Size(3, 3), 0);
            Imgproc.GaussianBlur(lastFrame, lastFrame, new Size(3, 3), 0);
            
            //bsMOG.apply(frame, processedFrame, 0.005);
            Core.subtract(currentFrame, lastFrame, processedFrame);
            //Core.absdiff(frame,lastFrame,processedFrame);
            
            Imgproc.cvtColor(processedFrame, processedFrame, Imgproc.COLOR_RGB2GRAY);
            //
            
            int threshold = jSliderThreshold.getValue();
            //Imgproc.adaptiveThreshold(processedFrame, processedFrame, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);
            Imgproc.threshold(processedFrame, processedFrame, threshold, 255, Imgproc.THRESH_BINARY);

            ArrayList<Rect> array = detection_contours(currentFrame, processedFrame);
            ///*
            if(array.size() > 0)
            {
              Iterator<Rect> it2 = array.iterator();
              while(it2.hasNext())
              {
                Rect obj = it2.next();
                Core.rectangle(currentFrame, new Point(5,currentFrame.cols()/2),new Point(5,currentFrame.rows()/2),
                  new Scalar(0, 255, 0), 1);
              }
            }
            //*/
            
            if(jCheckBoxAlarm.isSelected())
            {
              double sensibility = jSliderSensibility.getValue();
              //System.out.println(sensibility);
              double nonZeroPixels = Core.countNonZero(processedFrame);
              //System.out.println("nonZeroPixels: " + nonZeroPixels);

              double nrows = processedFrame.rows();
              double ncols = processedFrame.cols();
              double total = nrows * ncols / 10;

              double detections = (nonZeroPixels / total) * 100;
              //System.out.println(detections);
              Core.rectangle(currentFrame, new Point(20,20),new Point(250,250),
                      new Scalar(0, 255, 0), 1);
              if(detections >= sensibility)
              {
                //System.out.println("ALARM ENABLED!");
                //Core.putText(currentFrame, "MOTION DETECTED", 
                  //new Point(5,currentFrame.cols()/2), //currentFrame.rows()/2 currentFrame.cols()/2
                  //Core.FONT_HERSHEY_TRIPLEX , new Double(1), new Scalar(0,0,255));

                if(jCheckBoxSave.isSelected())
                {
                  if(savedelay == 2)
                  {
                    //String filename = jTextFieldSaveLocation.getText() + File.separator + "capture_" + getCurrentTimeStamp() + ".jpg";
                    //System.out.println("Saving results in: " + filename);
                   // Highgui.imwrite(filename, processedFrame);
                    savedelay = 0;
                  }
                  else
                    savedelay = savedelay + 1;
                }
              }
              else
              {
                savedelay = 0;
                //System.out.println("");
              }
            }
            //currentFrame.copyTo(processedFrame);
          }
          else
          {
            //frame.copyTo(processedFrame);
          }
          
          currentFrame.copyTo(processedFrame);

          Highgui.imencode(".jpg", processedFrame, matOfByte);
          byte[] byteArray = matOfByte.toArray();

          try
          {
            in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            if(count==0){/*
            	System.out.println("Inside Originl");
             firstImage = jTextFieldSaveLocation.getText() + File.separator + "original.jpg";
            //Highgui.imwrite(filename, processedFrame);
          
            
	        File outputfile = new File(firstImage);
	       //System.out.println("outputfile--"+outputfile.getAbsolutePath());
	        try {
				ImageIO.write(bufImage, "jpg", outputfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	        count++;
            }
            else
            {
            	count++;
            }
          }
          catch(Exception ex)
          {
            ex.printStackTrace();
          }

          //image.updateImage(new ImageIcon("figs/lena.png").getImage());
          image.updateImage(bufImage);

          frame.copyTo(lastFrame);

          try
          {
            Thread.sleep(1);
          }
          catch(Exception ex)
          {
          }
          
          /////////////
          //System.out.println(toStopRecog+" "+counttoStart);
          if(toStopRecog && counttoStart>50)
          {
        	  
        	  System.out.println("Inside Condition");
        	  counttoStart=0;
          	
          		
          		
    		          	  System.out.println("DATA");
    		          	  try {
    		      			//Thread.sleep(1000);
    		          		BufferedImage resultCrop=bufImage.getSubimage(20, 20, 235, 235);
    		      				BufferedImage result = new BufferedImage(
    		      						resultCrop.getWidth(),
    		      						resultCrop.getHeight(),
    		      	                    BufferedImage.TYPE_BYTE_BINARY);
    		
    		      	            Graphics2D graphic = result.createGraphics();
    		      	            graphic.drawImage(result, 0, 0, Color.WHITE, null);
    		      	            graphic.dispose();
    		      	            String file=detectionsDir+"\\test\\"+File.separator+File.separator+characterName+"_file"+System.currentTimeMillis()+".jpg";
    		      	            File folder=new File(detectionsDir+"\\");
    		      	            if(!folder.exists())
    		      	            	folder.mkdirs();
    		      	            
    		      	            
    		      	            File output = new File(file);
    		      	            
    		      	            ImageIO.write(resultCrop, "png", output);
    		      	            
    		      	            
    		      	            Thread.sleep(2000);
    		      	            ConvertBlackWhite.convertToGreyScale(file);		
    		      	            SURFDetector sURFDetector=new SURFDetector();
    		      	          sURFDetector.listData1=null;
    		      	           String fileName= sURFDetector.main(detectionsDir+"\\", file,list);  
    		      	           if(fileName!= null && fileName.length()>2){
    		      	        	   System.out.println("list=="+sURFDetector.listData1);
    		      	        	 jPanel1.list=sURFDetector.listData1;
    		      	        	jPanel1.repaint();
    		      	        	repaint();
    		      	           	String value=fileName.substring(fileName.lastIndexOf("\\")+1,fileName.indexOf("_"));
    		      	          String propValue=ReadProp.getPropertyValue(value);
      		  	           	System.out.println("propValue="+propValue);
      		  	           	if(propValue==null)
      		  	           	{	jTextArea1.setText(value);
      		  	           	
      		  	           		SpeechSynthesisTest.getVoice("Matched "+value);
      		  	           	
      		  	           	}
      		  	           	else{
      		  	           	jTextArea1.setText(propValue);
      		  	           		SpeechSynthesisTest.getVoice("Matched "+propValue);
      		  	           	}
      		      	           	toStopRecog=false;
      		      	           	
      		      	           	
      		      	           }
      		      	           else
      		      	           {
      		      	        	  JOptionPane.showMessageDialog(null,"No Result Found");
      		      	        	toStopRecog=false;
      		      	           }
    		      	                  
    		              
    		              
    		            }
    		            catch(Exception e)
    		            {
    		          	  e.printStackTrace();
    		            }
          }
          
          
          
          /////////////
        }
      }
      
     
      
      
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelSource1 = new javax.swing.JPanel();
        jLabelSource1 = new javax.swing.JLabel();
        jTextFieldSource1 = new javax.swing.JTextField();
        jButtonStart = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jCheckBoxMotionDetection = new javax.swing.JCheckBox();
        jSliderThreshold = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCheckBoxAlarm = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jSliderSensibility = new javax.swing.JSlider();
        jTextFieldSaveLocation = new javax.swing.JTextField();
        jCheckBoxSave = new javax.swing.JCheckBox();
        jPanel1 = new Slideshow();
        
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hand Detection");

        jPanelSource1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanelSource1Layout = new javax.swing.GroupLayout(jPanelSource1);
        jPanelSource1.setLayout(jPanelSource1Layout);
        jPanelSource1Layout.setHorizontalGroup(
            jPanelSource1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 606, Short.MAX_VALUE)
        );
        jPanelSource1Layout.setVerticalGroup(
            jPanelSource1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 420, Short.MAX_VALUE)
        );

        jLabelSource1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelSource1.setText("Source 1:");

        jTextFieldSource1.setText("0");

        jButtonStart.setText("Training Set");
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            	
              //jButtonStartActionPerformed(evt);
          	  if(characterName==null){
          		  	characterName=JOptionPane.showInputDialog(null,"Please enter Character For Training Set");
          	  		SpeechSynthesisTest.getVoice("Hello "+username+" You Selected "+characterName);
          	  }
          	  
          	  String filename = jTextFieldSaveLocation.getText() + File.separator + "capture_" + getCurrentTimeStamp() + ".jpg";
                //System.out.println("Saving results in: " + filename);
                
                	try {
      					//Thread.sleep(1000);
                		BufferedImage resultCrop=bufImage.getSubimage(20, 20, 235, 235);
      					BufferedImage result = new BufferedImage(
      							resultCrop.getWidth(),
      							resultCrop.getHeight(),
      		                    BufferedImage.TYPE_BYTE_BINARY);

      		            Graphics2D graphic = result.createGraphics();
      		            graphic.drawImage(result, 0, 0, Color.WHITE, null);
      		            graphic.dispose();
      		            String file=detectionsDir+File.separator+File.separator+characterName+"_file"+System.currentTimeMillis()+".jpg";
      		            
      		            File output = new File(file);
      		            ImageIO.write(resultCrop, "png", output);
      		            
      		            
      		            Thread.sleep(2000);
      		           ConvertBlackWhite.convertToGreyScale(file);	
      		           
      				} catch (Exception e) {
      					// TODO Auto-generated catch block
      					e.printStackTrace();
      				}
                
            }
          });

        jButtonStop.setText("Stop");
        jButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStopActionPerformed(evt);
            }
        });

        jCheckBoxMotionDetection.setText("Start To Detect");

        jSliderThreshold.setMaximum(255);
        jSliderThreshold.setPaintLabels(true);
        jSliderThreshold.setPaintTicks(true);
        jSliderThreshold.setValue(15);
        jSliderThreshold.setVisible(false);	
        jLabel1.setText("Threshold:");
        jLabel1.setVisible(false);
        jLabel2.setText("(zero for local webcamera)");

        jCheckBoxAlarm.setText("Voice");
        jCheckBoxAlarm.setSelected(true);
        jLabel3.setText("Sensibility:");
        jLabel3.setVisible(false);
        jSliderSensibility.setMinimum(1);
        jSliderSensibility.setPaintLabels(true);
        jSliderSensibility.setPaintTicks(true);
        jSliderSensibility.setValue(10);
        jSliderSensibility.setVisible(false);
        jCheckBoxSave.setText("Save detections in:");

        jPanel1.setBackground(new java.awt.Color(255, 153, 153));
        jPanel1.setName("sliderPanel"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 608, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Reset");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
        	 public void actionPerformed(java.awt.event.ActionEvent evt)
             {
               //dispose();
               //main(new String[]{});
           	  characterName=null;
           	  
             }
        });

        jButton2.setText("Recog");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
        	
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            	System.out.println("Recog Button Clicked");
            	start();
            	toStopRecog=true;
			}
            
          });
        
        jCheckBoxMotionDetection.setText("Start To Detect");
        jCheckBoxMotionDetection.addActionListener(new ActionListener() {
    		
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			// TODO Auto-generated method stub
    	          if(jCheckBoxMotionDetection.isSelected())
    	          {
    	        	  jButtonStartActionPerformed(e);
    	          }
    			
    		}
    	});

        jButton3.setText("Output");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	SpeechSynthesisTest.getVoice(" "+jTextArea1.getText());
            }
        });
        //jButton3.setVisible(false);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanelSource1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelSource1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldSource1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSliderSensibility, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBoxAlarm)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxSave))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBoxMotionDetection)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jSliderThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addComponent(jTextFieldSaveLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelSource1)
                    .addComponent(jTextFieldSource1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelSource1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxMotionDetection)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxSave)
                            .addComponent(jCheckBoxAlarm)
                            .addComponent(jTextFieldSaveLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jSliderThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jSliderSensibility, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonStart)
                    .addComponent(jButtonStop)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonStartActionPerformed
    {//GEN-HEADEREND:event_jButtonStartActionPerformed
      start();
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonStopActionPerformed
    {//GEN-HEADEREND:event_jButtonStopActionPerformed
      stop();
    }//GEN-LAST:event_jButtonStopActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    	System.exit(0);
    }//GEN-LAST:event_jButton3ActionPerformed

  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try
    {
      for(javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
      {
        if("Windows".equals(info.getName()))
        {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    }
    catch(ClassNotFoundException ex)
    {
      java.util.logging.Logger.getLogger(MainFrameNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch(InstantiationException ex)
    {
      java.util.logging.Logger.getLogger(MainFrameNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch(IllegalAccessException ex)
    {
      java.util.logging.Logger.getLogger(MainFrameNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    catch(javax.swing.UnsupportedLookAndFeelException ex)
    {
      java.util.logging.Logger.getLogger(MainFrameNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        MainFrameNew mainFrame = new MainFrameNew();
        mainFrame.setVisible(true);
        mainFrame.setLocationRelativeTo(null);
      }
    });
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JCheckBox jCheckBoxAlarm;
    private javax.swing.JCheckBox jCheckBoxMotionDetection;
    private javax.swing.JCheckBox jCheckBoxSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelSource1;
    private Slideshow jPanel1;//Slide Show Images
    private javax.swing.JPanel jPanelSource1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSliderSensibility;
    private javax.swing.JSlider jSliderThreshold;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextFieldSaveLocation;
    private javax.swing.JTextField jTextFieldSource1;
    // End of variables declaration//GEN-END:variables
    
    public class Slideshow extends JPanel {
        JLabel pic;
        Timer tm;
        int x = 0;
        //Images Path In Array
        Object[] list = {
                        };
        public Slideshow(){
        	
        	setBackground(Color.cyan);
        }
        
        public void paint(Graphics g)
        {
        	if(list!=null && list.length>0){
        		System.out.println(list[0].toString());
        		Image img=new ImageIcon(list[0].toString()).getImage();
        		
        	g.drawImage(img,0,0,null);
        	}
        }
        
        

   

    }

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		System.exit(0);
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
