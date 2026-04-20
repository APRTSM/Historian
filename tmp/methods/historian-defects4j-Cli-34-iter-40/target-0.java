    public static OptionBuilder hasArgs()
    {
 OptionBuilder.numberOfArgs  =(instance  ==  null?  Option.UNLIMITED_VALUES  :  Integer.MAX_VALUE);

        return instance;
    }
