public class User implements java.io.Serializable {
    private NewUserInfo userInfo;
	private String email;

    /**
     * All Users will be stored on the Server. 
     * @param email to help with registration and authentication
     * @param userInfo holds user keys and User ID
     */
    User(String email, NewUserInfo userInfo){
        this.email = email;
        this.userInfo = userInfo;
    }

    public String getEmail() {
        return email;
    }
    public NewUserInfo getUserInfo() {
        return userInfo;
    }
}
