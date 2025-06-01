	public CharSequence getCallbackFunctionBody(CallbackParameter... extraParameters)
	{
		AjaxRequestAttributes attributes = getAttributes();
		attributes.setEventNames();
		CharSequence attrsJson = renderAjaxAttributes(getComponent(), attributes);
		StringBuilder sb = new StringBuilder();
		sb.append("var attrs = ");
		sb.append(attrsJson);
		sb.append(";\n");
		sb.append("var params = {");
		boolean first = true;
		for (CallbackParameter curExtraParameter : extraParameters)
		{
			if (curExtraParameter.getAjaxParameterName() != null)
			{
				if (!first)
					sb.append(',');
				else
					first = false;
				sb.append('\'').append(curExtraParameter.getAjaxParameterName()).append("': ")
					.append(curExtraParameter.getAjaxParameterCode());
			}
		}
		sb.append("};\n");
		if (attributes.getExtraParameters().isEmpty())
		{
			sb.append("attrs.").append(AjaxAttributeName.EXTRA_PARAMETERS).append(" = params;\n");
		}
		else
		{
			sb.append("attrs.").append(AjaxAttributeName.EXTRA_PARAMETERS).append(" = Wicket.merge(attrs.")
					.append(AjaxAttributeName.EXTRA_PARAMETERS).append(", params);\n");
		}
		sb.append("Wicket.Ajax.ajax(attrs);\n");
		return sb;
	}
