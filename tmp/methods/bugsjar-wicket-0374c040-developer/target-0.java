	public CharSequence getCallbackFunctionBody(CallbackParameter... extraParameters)
	{
		AjaxRequestAttributes attributes = getAttributes();
		attributes.setEventNames();
		CharSequence attrsJson = renderAjaxAttributes(getComponent(), attributes);
		StringBuilder sb = new StringBuilder();
		sb.append("var attrs = ");
		sb.append(attrsJson);
		sb.append(";\n");
		JSONArray jsonArray = new JSONArray();
		for (CallbackParameter curExtraParameter : extraParameters)
		{
			if (curExtraParameter.getAjaxParameterName() != null)
			{
				try
				{
					JSONObject object = new JSONObject();
					object.put("name", curExtraParameter.getAjaxParameterName());
					object.put("value", new JsonFunction(curExtraParameter.getAjaxParameterCode()));
					jsonArray.put(object);
				}
				catch (JSONException e)
				{
					throw new WicketRuntimeException(e);
				}
			}
		}
		sb.append("var params = ").append(jsonArray).append(";\n");
		sb.append("attrs.").append(AjaxAttributeName.EXTRA_PARAMETERS)
				.append(" = params.concat(attrs.")
				.append(AjaxAttributeName.EXTRA_PARAMETERS).append(");\n");
		sb.append("Wicket.Ajax.ajax(attrs);\n");
		return sb;
	}
