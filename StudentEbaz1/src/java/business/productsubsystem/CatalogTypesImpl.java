package business.productsubsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import business.externalinterfaces.CatalogTypes;

/**
 * @author pcorazza
 * <p>
 * Class Description: This is a bean...it just holds
 * data in memory. It is not the entity class for a catalog --
 * it is just reference data, stored in memory.
 */
public class CatalogTypesImpl implements CatalogTypes {
    HashMap<Integer,String> catalogIdToName = new HashMap<Integer,String>();
    HashMap<String,Integer> catalogNameToId = new HashMap<String,Integer>();
    public List<String[]> getCatalogNamesStringArrays() {
    	List<String[]> retVal = new ArrayList<String[]>();
    	Collection<String> vals = catalogIdToName.values();
    	for(String s : vals){
    		retVal.add(new String[]{s});
    	}
    	return retVal;
 
        
    }
   
    public List<String> getCatalogNames() {
      	String[] names = catalogIdToName.values().toArray(new String[0]);
    	return Arrays.asList(names);    
    }
    public String getCatalogName(Integer id){
        return catalogIdToName.get(id);
    }

    public void addCatalog(Integer id, String name) {
        catalogIdToName.put(id,name);
        catalogNameToId.put(name,id);
    }
    public Integer getCatalogId(String name) {
        return catalogNameToId.get(name);
        
        
    }
	
}
