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
