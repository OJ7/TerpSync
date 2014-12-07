package com.example.campuseventsapp.card;

@SuppressWarnings("serial")
public class NoDataException extends Exception {
   
	public NoDataException(){
        super();
    }

    public NoDataException(String message){
        super(message);
    }
}
