	protected final Set<String> getConfig(PropertyDescriptor<List<String>> descriptor) {
		Set<String> ret = new HashSet<String>();
		List<String> props = getProperty(descriptor);
		for (String value: props) {
		}
		
		return ret;
	}
	public void start(RuleContext ctx) {
    	init();
    }
