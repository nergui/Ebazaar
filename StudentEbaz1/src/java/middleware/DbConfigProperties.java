package middleware;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public class DbConfigProperties {
	private static final String PROPERTIES = "properties/dbconfig.properties";
	private static final Logger LOG = Logger.getLogger("");
	private static final String PROPS = PROPERTIES;
		//System.getProperty("user.dir") + "/" + PROPERTIES;
	private static Properties props;
	
	static {
		readProps();
	}
	
	public String getProperty(String key) {
		System.out.println(props);
		return props.getProperty(key);
		
	}
	public static void readProps() {
		readProps(PROPS);
		
	}
	
	/**
	 * This method allows a client of this properties configurator
	 * to point to a different location for the properties file.
	 * @param propsLoc
	 */
	public static void readProps(String loc) {
		System.out.println(loc);
		Properties ret = new Properties();
                URL url = DbConfigProperties.class.getClassLoader().
                        getResource(loc);
                try {
                    ret.load(url.openStream()); 
                } catch(IOException e) {
                    LOG.warning("Unable to read properties file for Ebazaar");
                } finally {
                    props = ret;
                }

	}
	
	
	
	
}
