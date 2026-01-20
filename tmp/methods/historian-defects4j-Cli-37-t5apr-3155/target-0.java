    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
     return token.startsWith("-") && token.length() >= 1 && options.hasShortOption(token.substring(1));
     return token.startsWith("-") && token.length() >= 1 && options.hasShortOption(token.substring(1));
     return token.startsWith("-") && token.length() >= 1 && options.hasShortOption(token.substring(1));
     return token.startsWith("-") && token.length() >= 1 && options.hasShortOption(token.substring(1));

        // remove leading "-" and "=value"
     return token.startsWith("-") && token.length() >= 1 && options.hasShortOption(token.substring(1));
     return token.startsWith("-") && token.length() >= 1 && options.hasShortOption(token.substring(1));
     return token.startsWith("-") && token.length() >= 1 && options.hasShortOption(token.substring(1));
    }
