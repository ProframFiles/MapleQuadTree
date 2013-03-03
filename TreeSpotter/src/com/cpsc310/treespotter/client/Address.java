package com.cpsc310.treespotter.client;

/**
 * Class to hold a street number, street address pair.
 * Mostly used for parsing.
 * 
 */
public class Address {
	private int num;
	private String street;
	private boolean valid;

	/**
	 * Parses String input to get number and street address
	 * 
	 * @param input
	 */
	public Address(String input) {
		String[] addr = input.split("[,]", 2);
		addr = addr[0].split("\\s+", 2);
		if (addr.length < 1) {
			num = -1;
			street = "";
			valid = false;
			return;
		}
		try {
			num = Integer.parseInt(addr[0]);
			street = addr[1].trim();
			valid = true;
		} catch (Exception e) {
			num = -1; // possibly no street number
			street = addr[0] + " " + addr[1];
			valid = true;
		}
	}

	public Address(int n, String s) {
		num = n;
		street = s;
	}

	public int getNumber() {
		return num;
	}

	public String getStreet() {
		return street;
	}

	public boolean isValid() {
		return valid;
	}
}
