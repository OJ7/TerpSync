package com.terpsync;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("AdminAccounts")
public class AdminAccounts extends ParseObject{


	public AdminAccounts() {}

	public String getUsername() {
		return getString("username");
	}

	public String getPassword() {
		return getString("password");
	}

	public String getOrganizatonName() {
		return getString("organizationName");
	}

	public void setUsername(String userN) {
		put("username", userN);
	}

	public void setPassword(String pw) {
		put("password", pw);
	}

	public void setOrganizationName(String name) {
		put("organizationName", name);
	}
}