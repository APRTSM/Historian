	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		AppendingStringBuffer asb = new AppendingStringBuffer();
		asb.append("function attachChoiceHandlers(markupId, callbackScript) {\n");
		asb.append(" var inputNodes = wicketGet(markupId).getElementsByTagName('input');\n");
		asb.append(" for (var i = 0 ; i < inputNodes.length ; i ++) {\n");
		asb.append(" var inputNode = inputNodes[i];\n");
		asb.append(" if (!inputNode.type) continue;\n");
		asb.append(" var inputType = inputNode.type.toLowerCase();\n");
		asb.append(" if (inputType == 'checkbox' || inputType == 'radio') {\n");
		asb.append(" Wicket.Event.add(inputNode, 'click', callbackScript);\n");
		asb.append(" }\n");
		asb.append(" }\n");
		asb.append("}\n");

		response.renderJavascript(asb, "attachChoice");

		response.renderOnLoadJavascript("attachChoiceHandlers('" + getComponent().getMarkupId() +
			"', function() {" + getEventHandler() + "});");

	}
