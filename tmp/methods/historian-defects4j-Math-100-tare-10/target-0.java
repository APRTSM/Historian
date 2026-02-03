    protected void addParameter(EstimatedParameter p) {
	if(p.isBound()){
	return;
	}
        parameters.add(p);
    }
