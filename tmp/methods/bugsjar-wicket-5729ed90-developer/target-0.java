	private String removeComment(String rawMarkup)
	{
		// For reasons I don't understand, the following regex <code>"<!--(.|\n|\r)*?-->"<code>
		// causes a stack overflow in some circumstances (jdk 1.5)
		// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050507
		// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
		int pos1 = rawMarkup.indexOf("<!--");
		while (pos1 != -1)
		{
			int pos2 = rawMarkup.indexOf("-->", pos1 + 4);

			final StringBuilder buf = new StringBuilder(rawMarkup.length());
			if (pos2 != -1)
			{
				final String comment = rawMarkup.substring(pos1 + 4, pos2);

				// See wicket-2105 for an example where this rather simple regex throws an exception
				// CONDITIONAL_COMMENT = Pattern.compile("\\[if .+\\]>(.|\n|\r)*<!\\[endif\\]");
				// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050507
				// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993
				if (CONDITIONAL_COMMENT.matcher(comment).matches() == false)
				{
					buf.append(rawMarkup.substring(0, pos1));
					if (rawMarkup.length() >= pos2 + 3)
					{
						buf.append(rawMarkup.substring(pos2 + 3));
					}
					rawMarkup = buf.toString();
				}
				else
				{
					pos1 = pos2;
				}
			}
			pos1 = rawMarkup.indexOf("<!--", pos1);
		}
		return rawMarkup;
	}
