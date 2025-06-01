		public static void set()
		{
			Session session = Session.get();
			session.bind();
			InterceptData data = new InterceptData();
			Request request = RequestCycle.get().getRequest();
			data.originalUrl = request.getOriginalUrl();
			data.postParameters = new HashMap<String, List<StringValue>>();
			for (String s : request.getPostParameters().getParameterNames())
			{
				data.postParameters.put(s, new ArrayList<StringValue>(request.getPostParameters()
					.getParameterValues(s)));
			}
			data.continueOk = false;
			session.setMetaData(key, data);
		}
