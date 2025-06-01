    public PatternBuilder groupEnd(String s) {
        s = s.replace("xxxx", "x{4}").replace("xxx", "x{3}")
				.replace("xx", "x{2}");
		return expression(")" + s);
    }
