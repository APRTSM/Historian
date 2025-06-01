    public int getMaxShadowPagesForXPos(CssContext c, int x) {
        IdentValue dir = getCutOffPageDirection();
        float fx = (float) x;
        float fw = (float) getContentWidth(c);
        
        if (fw == 0f) {
            return 0;
        }
        
        if (dir == IdentValue.LTR) { 
            return (x > 0 ? ((int) (fx / fw)) : 0);
        }
        
        return (x < 0 ? ((int) (Math.abs(fx) / fw)) : 0);
    }
    private void addBoxToShadowPages(
            CssContext c, Box container, int pageNumber,
            PageResult pageResult, Shape ourClip,
            /* adds-to: */ List<PageResult> clipPages,
            Layer layer, AddToShadowPage addToMethod) {
        
        PageBox basePageBox = getPageBox(pageNumber);
        
        AffineTransform ctm = container.getContainingLayer().getCurrentTransformMatrix();
        Rectangle bounds = container.getBorderBox(c);
        // TODO: RTL overflow.
        int maxX = (int) (ctm == null ? bounds.getMaxX() : getMaxXFromTransformedBox(bounds, ctm));
        int maxShadowPages = basePageBox.getMaxShadowPagesForXPos(c, maxX);
        
        for (int i = 0; i < maxShadowPages; i++) {
            Rectangle shadowPageClip = pageResult.getShadowWindowOnDocument(basePageBox, c, i);
            
            boolean intersects = addToMethod.boundsBox() == AddToShadowPage.AGGREGATE_BOX ? 
                    intersectsAggregateBounds(shadowPageClip, container) :
                    intersectsBorderBoxBounds(c, shadowPageClip, container);
            
            if (intersects) {
                PageResult shadowPageResult = getOrCreateShadowPage(pageResult, i);
                
                if (addToMethod.add(this, shadowPageResult, container, ourClip, layer)) {
                    clipPages.add(shadowPageResult);
                }
            }
        }
    }
	private static float getPageTranslateX(float absTranslateX, int shadowPageNumber, PageBox page, CssContext c) {
	    if (shadowPageNumber == -1) {
	        return absTranslateX + page.getMarginBorderPadding(c, CalculatedStyle.LEFT);    
	    }
	    
	    Rectangle shadow = page.getDocumentCoordinatesContentBoundsForInsertedPage(c, shadowPageNumber);
	    
	    if (page.getCutOffPageDirection() == IdentValue.LTR) { 
	        return absTranslateX - (float) shadow.getMinX() + (page.getMarginBorderPadding(c, CalculatedStyle.LEFT) * (shadowPageNumber + 1));
	    } else {
	        return absTranslateX - (float) shadow.getMinX() + (page.getMarginBorderPadding(c, CalculatedStyle.RIGHT) * (shadowPageNumber + 1));
	    }
	}
