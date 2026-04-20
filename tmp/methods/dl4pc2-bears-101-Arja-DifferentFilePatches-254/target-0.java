    public PatternBuilder groupEnd(String s) {
        fragments.add(s);
		return expression(")" + s);
    }
