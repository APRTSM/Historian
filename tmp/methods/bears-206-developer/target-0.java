    public int getMaxShadowPagesForXPos(CssContext c, int x) {
        IdentValue dir = getCutOffPageDirection();
        float fx = (float) x;
        float fw = (float) getContentWidth(c);
        
        if (fw == 0f) {
            return 0;
        }
        
        if (dir == IdentValue.LTR) { 
            return (int) (x > 0 ? (Math.ceil(fx / fw) - 1) : 0);
        }
        
        return (int) (x < 0 ? (Math.ceil(Math.abs(fx) / fw)) : 0);
    }
