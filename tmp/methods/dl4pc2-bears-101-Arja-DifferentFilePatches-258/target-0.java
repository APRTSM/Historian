    public PatternBuilder groupEnd(String s) {
        s = s.replaceAll("\\|$", "\\\\|").replaceAll("^\\|", "\\\\|");
		return expression(")" + s);
    }
