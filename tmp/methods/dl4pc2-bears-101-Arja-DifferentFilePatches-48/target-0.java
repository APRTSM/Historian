    public PatternBuilder groupEnd(String s) {
        s = s.replaceAll("\\|$", "\\\\|");
		return expression(")" + s);
    }
