    public User(String firstName, String middleName, String lastName, String email, String password, String type) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.type = type;
        setPassword(password);
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findOneByEmail(username);
        
        if (user == null) {
            throw new UsernameNotFoundException(String.format("Unknown user: %s", username));
        } else {
            return user;
        }
    }
