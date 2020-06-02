package handgesture;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ConvertBlackWhite {

    public static void main(String... args) {

        try {

            File input = new File("c:\\temp\\hands_PNG1522051633331.png");
            BufferedImage image = ImageIO.read(input);

            BufferedImage result = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_BYTE_BINARY);

            Graphics2D graphic = result.createGraphics();
            graphic.drawImage(image, 0, 0, Color.WHITE, null);
            graphic.dispose();

            File output = new File("c:\\temp\\detection\\out.jpg");
            ImageIO.write(result, "png", output);
            
           // convertToGreyScale();

        }  catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    
    public static String convertToGreyScale(String fileName)
    {
    	
    	 BufferedImage img = null;
    	    File f = null;

    	    //read image
    	    try{
    	      f = new File(fileName);
    	      img = ImageIO.read(f);
    	    }catch(IOException e){
    	      System.out.println(e);
    	    }

    	    //get image width and height
    	    int width = img.getWidth();
    	    int height = img.getHeight();

    	    //convert to grayscale
    	    for(int y = 0; y < height; y++){
    	      for(int x = 0; x < width; x++){
    	        int p = img.getRGB(x,y);

    	        int a = (p>>24)&0xff;
    	        int r = (p>>16)&0xff;
    	        int g = (p>>8)&0xff;
    	        int b = p&0xff;

    	        //calculate average
    	        int avg = (r+g+b)/3;

    	        //replace RGB value with avg
    	        p = (a<<24) | (avg<<16) | (avg<<8) | avg;

    	        img.setRGB(x, y, p);
    	      }
    	    }

    	    //write image
    	    try{
    	      f = new File(fileName);
    	      ImageIO.write(img, "jpg", f);
    	      return f.getAbsolutePath();
    	    }catch(IOException e){
    	      System.out.println(e);
    	    }
    	    return null;
    }
    

}