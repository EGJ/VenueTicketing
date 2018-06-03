package com.example.company.struct;

public abstract class SeatHold {
	//The id of the seatholder
	private int id;

	/**
	 * @return The id of the seatholder
	 */
	public int getSeatHolder(){
		return id;
	}

	/**
	 * @param id The id of the seatholder
	 */
	public void setSeatHolder(int id){
		this.id = id;
	}
}
