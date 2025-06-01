	public void visitCtJavaDocTag(CtJavaDocTag docTag) {
		printer.write(COMMENT_STAR);
		printer.write(CtJavaDocTag.JAVADOC_TAG_PREFIX);
		printer.write(docTag.getType().name().toLowerCase());
		printer.write(" ");
		if (docTag.getType().hasParam()) {
			printer.write(docTag.getParam()).writeln().writeTabs();
		}

		String[] tagLines = docTag.getContent().split(LINE_SEPARATORS_RE);
		for (int i = 0; i < tagLines.length; i++) {
			String com = tagLines[i];
			if (i > 0 || docTag.getType().hasParam()) {
				printer.write(COMMENT_STAR);
			}
			if (docTag.getType().hasParam()) {
				printer.write("\t\t");
			}
			printer.write(com.trim()).writeln().writeTabs();
		}
	}
	public void visitCtComment(CtComment comment) {
		if (!env.isCommentsEnabled() && context.elementStack.size() > 1) {
			return;
		}
		switch (comment.getCommentType()) {
			case FILE:
				printer.write(JAVADOC_START).writeln();
				break;
			case JAVADOC:
				printer.write(JAVADOC_START).writeln().writeTabs();
				break;
			case INLINE:
				printer.write(INLINE_COMMENT_START);
				break;
			case BLOCK:
				printer.write(BLOCK_COMMENT_START);
				break;
		}
		String content = comment.getContent();
		switch (comment.getCommentType()) {
			case INLINE:
				printer.write(content);
				break;
			default:
				String[] lines = content.split(LINE_SEPARATORS_RE);
				for (int i = 0; i < lines.length; i++) {
					String com = lines[i];
					if (comment.getCommentType() == CtComment.CommentType.BLOCK) {
						printer.write(com);
						if (lines.length > 1) {
							printer.writeln().writeTabs();
						}
					} else {
						if (com.length() > 0) {
							printer.write(COMMENT_STAR + com).writeln().writeTabs();
						} else {
							printer.write(" *" /* no trailing space */ + com).writeln().writeTabs();
						}
					}

				}
				if (comment instanceof CtJavaDoc) {
					if (!((CtJavaDoc) comment).getTags().isEmpty()) {
						printer.write(" *").writeln().writeTabs();
					}
					for (CtJavaDocTag docTag : ((CtJavaDoc) comment).getTags()) {
						scan(docTag);
					}
				}
				break;
		}

		switch (comment.getCommentType()) {
			case BLOCK:
				printer.write(BLOCK_COMMENT_END);
				break;
			case FILE:
				printer.write(BLOCK_COMMENT_END);
				break;
			case JAVADOC:
				printer.write(BLOCK_COMMENT_END);
				break;
		}
	}
