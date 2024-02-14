package com.example.demo.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Users {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	int id;
	String username;
	String emailId;
	String password;
	String gender;
	String role;
	String address;
	boolean isPremium;
	public Users() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Users(int id, String username, String emailId, String password, String gender, String role, String address,
			boolean isPremium) {
		super();
		this.id = id;
		this.username = username;
		this.emailId = emailId;
		this.password = password;
		this.gender = gender;
		this.role = role;
		this.address = address;
		this.isPremium = isPremium;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public boolean isPremium() {
		return isPremium;
	}
	public void setPremium(boolean isPremium) {
		this.isPremium = isPremium;
	}
	@Override
	public String toString() {
		return "Users [id=" + id + ", username=" + username + ", emailId=" + emailId + ", password=" + password
				+ ", gender=" + gender + ", role=" + role + ", address=" + address + ", isPremium=" + isPremium + "]";
	}
		
	
	
}
