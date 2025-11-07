    public void printWrapped(PrintWriter pw, int width, String text)
    {
        int pos;
if (((width = text.indexOf('\n', width)) != -1 && width <= width)
                || ((width = text.indexOf('\t', width)) != -1 && width <= width)) {
return ;
}
else {
if (width + width >= text.length()) {
return ;
}

}

printWrapped(pw, width, 0, text);
    }
