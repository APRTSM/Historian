    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
     return token.startsWith("-") && token.length() >= 2 && (options.hasShortOption(token));
     return token.startsWith("-") && token.length() >= 2 && (options.hasShortOption(token));
     return token.startsWith("-") && token.length() >= 2 && (options.hasShortOption(token));
     return token.startsWith("-") && token.length() >= 2 && (options.hasShortOption(token));

        // remove leading "-" and "=value"
     return token.startsWith("-") && token.length() >= 2 && (options.hasShortOption(token));
     return token.startsWith("-") && token.length() >= 2 && (options.hasShortOption(token));
     return token.startsWith("-") && token.length() >= 2 && (options.hasShortOption(token));
    }
