package com.example.campuseventsapp;

/*
 * Card Object class
 * Each card has title, image and a description.
 */
public class Card {

	private int imageId;
	private String cardTitle;
	private String cardDescription;
	
	//Constructor
	public Card(int imageId, String cardTitle, String cardDescription){
		this.imageId = imageId;
		this.cardTitle = cardTitle;
		this.cardDescription = cardDescription;
	}
	
	//Getters and Setters
	public int getImageId(){
		return imageId;
	}
	
	public void setImageId(int imageId){
		this.imageId = imageId;
	}
	
	public String getTitle(){
		return this.cardTitle;
	}
	
	public void setTitle(String cardTitle){
		this.cardTitle = cardTitle;
	}
	
	public String getDescription(){
		return cardDescription;
	}
	
	public void setDescription(String description){
		this.cardDescription = description;
	}
	
	@Override
	public String toString(){
		return cardTitle + "\n" + cardDescription;
	}
	
	
	
}
