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
