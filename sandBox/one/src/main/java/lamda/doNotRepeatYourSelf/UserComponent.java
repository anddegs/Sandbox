package lamda.doNotRepeatYourSelf;

public interface UserComponent {
	UserResponse fetchAllUsers(UserRequest req);

	UserResponse fetchUser(UserRequest req);

	UserResponse deleteUser(UserRequest req);

	UserResponse updateUser(UserRequest req);

	UserResponse insertUser(UserRequest req);
}