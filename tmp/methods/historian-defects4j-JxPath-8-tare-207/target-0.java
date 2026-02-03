    private boolean containsMatch(Iterator it, Object value) {
        while (it.hasNext()) {
            Object element = it.next();
	if(it.hasNext()){

            if (compute(element, value)) {
                return true;
	}
            }
        }
        return false;
    }
