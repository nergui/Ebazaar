package business.customersubsystem;

import business.externalinterfaces.CustomerProfile;

class CustomerProfileImpl implements CustomerProfile{
	private String firstName;
	private String lastName;
	private Integer custId;
	//CustomerProfile(){}
	CustomerProfileImpl(Integer custid,String firstName,String lastName){
		this.custId = custid;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Integer getCustId() {
		return custId;
	}
	public void setCustId(Integer id) {
		custId = id;
	}
}
