package business;

//implement

//import middleware.EBazaarException;
import business.exceptions.BusinessException;
import business.externalinterfaces.DynamicBean;
import business.externalinterfaces.Rules;
import business.externalinterfaces.RulesConfigKey;
import business.externalinterfaces.RulesConfigProperties;
import business.externalinterfaces.RulesSubsystem;
import business.rulesbeans.QuantityBean;
import business.rulesubsystem.RulesSubsystemFacade;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RulesQuantity implements Rules {

	private HashMap<String,DynamicBean> table;
	private DynamicBean bean;
	private RulesConfigProperties config = new RulesConfigProperties();
	
	public RulesQuantity(Quantity quantity){
		bean = new QuantityBean(quantity);
	}
	public String getModuleName() {
		return config.getProperty(RulesConfigKey.QUANTITY_MODULE.getVal());
	}

	public String getRulesFile() {
		return config.getProperty(RulesConfigKey.QUANTITY_RULES_FILE.getVal());
	}
	public void prepareData() {
		table = new HashMap<String,DynamicBean>();		
		String deftemplate = config.getProperty(RulesConfigKey.QUANTITY_DEFTEMPLATE.getVal());
		table.put(deftemplate, bean);
	}
	public void runRules() throws BusinessException, RuleException{
    	RulesSubsystem rules = new RulesSubsystemFacade();
    	rules.runRules(this);
	}
	public HashMap<String,DynamicBean> getTable(){
		return table;
	}

	@SuppressWarnings("unchecked")
	public List getUpdates() {
		// nothing to do
		return new ArrayList();
	}

	public void populateEntities(List<String> updates) {
		// nothing to do
		
	}

}
