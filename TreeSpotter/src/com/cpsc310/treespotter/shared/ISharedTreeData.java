package com.cpsc310.treespotter.shared;

public interface ISharedTreeData {

	public abstract String getID();

	public abstract String getLocation();

	public abstract int getCivicNumber();

	public abstract String getTreeID();

	public abstract String getNeighbourhood();

	public abstract String getStreet();

	public abstract int getHeightRange();

	public abstract float getDiameter();

	public abstract String getPlanted();

	public abstract String getCultivar();

	public abstract String getGenus();

	public abstract String getSpecies();

	public abstract String getCommonName();

	public abstract LatLong getLatLong();

}