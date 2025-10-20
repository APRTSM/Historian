switch (str.length()) {
    case 2:
        return str.equalsIgnoreCase("on");
    case 3:
        return str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("y\u0131") || str.equalsIgnoreCase("\u0443\u0433");
    case 4:
        return str.equalsIgnoreCase("true");
    default:
        return false;
}