
package business;

import middleware.MiddlewareCleanup;
import middleware.externalinterfaces.Cleanup;


public class BusinessCleanup implements Cleanup {
    Cleanup mc;
    
    public void cleanup() {
//test
        mc = new MiddlewareCleanup();
        mc.cleanup();
        
    }

}
