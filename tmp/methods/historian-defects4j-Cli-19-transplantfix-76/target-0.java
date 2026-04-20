    private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (options.hasOption(token))
        {
            currentOption = options.getOption(token);
            tokens.add(token);
        }
        else if (!eatTheRest)
        {
            eatTheRest = true;
            tokens.add(token);
        }
    }
