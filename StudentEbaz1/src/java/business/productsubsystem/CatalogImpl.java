package business.productsubsystem;

import business.externalinterfaces.Catalog;

public class CatalogImpl implements Catalog {
	private String catId;
	private String name;
	public CatalogImpl(String id, String name) {
		this.catId = id;
		this.name = name;
	}
	public String getId() {
		return catId;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		catId = id;
		
	}

	public void setName(String name) {
		this.name = name;
		
	}

}
