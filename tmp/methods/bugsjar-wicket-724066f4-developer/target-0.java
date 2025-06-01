	public static JSONArray asArray(Map<String, Object> map) throws JSONException
	{
		JSONArray jsonArray = new JSONArray();

		if (map != null)
		{
			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				String name = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof List) {
					List<?> values = (List<?>) value;
					for (Object v : values)
					{
						if (v != null)
						{
							JSONObject object = new JSONObject();
							object.put("name", name);
							object.put("value", v);
							jsonArray.put(object);
						}
					}
				}
				else if (value != null)
				{
					if (value.getClass().isArray())
					{
						Object[] array = (Object[]) value;
						for (Object v : array)
						{
							if (v != null)
							{
								JSONObject object = new JSONObject();
								object.put("name", name);
								object.put("value", v);
								jsonArray.put(object);
							}
						}
					}
					else
					{
						JSONObject object = new JSONObject();
						object.put("name", name);
						object.put("value", value);
						jsonArray.put(object);
					}
				}
			}
		}

		return jsonArray;
	}
