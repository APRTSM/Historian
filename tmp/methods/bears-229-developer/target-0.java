	public static void setConcept(Obs obs, Object value) {
		Object identifier = null;
		if (value instanceof Map) {
			Object uuid = ((Map) value).get(RestConstants.PROPERTY_UUID);
			if (uuid != null) {
				identifier = uuid;
			}
		}

		if (identifier == null) {
			identifier = value;
		}

		obs.setConcept(ConversionUtil.getConverter(Concept.class).getByUniqueId((String) identifier));
	}
