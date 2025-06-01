	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for tag
		super.onComponentTag(tag);

		// Set href to link to this link's linkClicked method
		CharSequence url = getURL();

		// append any anchor
		url = appendAnchor(tag, url);

		// If we're disabled
		if (!isLinkEnabled())
		{
			disableLink(tag);
		}
		else
		{
			// if the tag is an anchor proper
			if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link") ||
				tag.getName().equalsIgnoreCase("area"))
			{
				// generate the href attribute
				tag.put("href", url);

				// Add any popup script
				if (popupSettings != null)
				{
					// NOTE: don't encode to HTML as that is not valid
					// JavaScript
					tag.put("onclick", popupSettings.getPopupJavaScript());
				}
			}
			else if (tag.getName().equalsIgnoreCase("script") ||
				tag.getName().equalsIgnoreCase("style"))
			{
				tag.put("src", url);
			}
			else
			{
				// generate a popup script by asking popup settings for one
				if (popupSettings != null)
				{
					popupSettings.setTarget("'" + url + "'");
					String popupScript = popupSettings.getPopupJavaScript();
					tag.put("onclick", popupScript);
				}
				else
				{
					// or generate an onclick JS handler directly
					// in firefox when the element is quickly clicked 3 times a second request is
					// generated during page load. This check ensures that the click is ignored
					tag.put(
						"onclick",
						"var win = this.ownerDocument.defaultView || this.ownerDocument.parentWindow; " +
							"if (win == window) { window.location.href='" +
							url +
							"'; } ;return false");
				}
			}


			// If the subclass specified javascript, use that
			final CharSequence onClickJavaScript = getOnClickScript(url);
			if (onClickJavaScript != null)
			{
				tag.put("onclick", onClickJavaScript);
			}
		}

	}
