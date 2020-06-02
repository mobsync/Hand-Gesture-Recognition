package handgesture;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
final public class ReadProp {
	
	
	public static String getPropertyValue(String key)
	{
		InputStream input = null;
		Properties prop = new Properties();
		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			return prop.getProperty(key);
			

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "";
	}

	public static void main(String args[])
	{
		System.out.println(getPropertyValue("hindi"));
	}

}
