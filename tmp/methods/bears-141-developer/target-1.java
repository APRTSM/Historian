    public void setEmail(String email) {
	if(email != null)
		this.email = email.toLowerCase();
    }
    public User(String firstName, String middleName, String lastName, String email, String password, String type) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        setEmail(email);
        this.type = type;
        setPassword(password);
    }
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findOneByEmail(username.toLowerCase());
        
        if (user == null) {
            throw new UsernameNotFoundException(String.format("Unknown user: %s", username));
        } else {
            return user;
        }
    }
