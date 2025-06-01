		public static void set()
		{
			Session session = Session.get();
			session.bind();
			InterceptData data = new InterceptData();
			Request request = RequestCycle.get().getRequest();
			data.originalUrl = request.getOriginalUrl();
			Iterator<QueryParameter> itor = data.originalUrl.getQueryParameters().iterator();
			while (itor.hasNext())
			{
				QueryParameter parameter = itor.next();
				String parameterName = parameter.getName();
				if (WebRequest.PARAM_AJAX.equals(parameterName) ||
					WebRequest.PARAM_AJAX_BASE_URL.equals(parameterName) ||
					WebRequest.PARAM_AJAX_REQUEST_ANTI_CACHE.equals(parameterName))
				{
					itor.remove();
				}
			}

			data.postParameters = new HashMap<String, List<StringValue>>();
			for (String s : request.getPostParameters().getParameterNames())
			{
				if (WebRequest.PARAM_AJAX.equals(s) || WebRequest.PARAM_AJAX_BASE_URL.equals(s) ||
					WebRequest.PARAM_AJAX_REQUEST_ANTI_CACHE.equals(s))
				{
					continue;
				}
				data.postParameters.put(s, new ArrayList<StringValue>(request.getPostParameters()
					.getParameterValues(s)));
			}
			data.continueOk = false;
			session.setMetaData(key, data);
		}
