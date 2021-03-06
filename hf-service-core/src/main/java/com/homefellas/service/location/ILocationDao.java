package com.homefellas.service.location;

import java.util.List;

import org.hibernate.Query;

import com.homefellas.model.location.Country;
import com.homefellas.model.location.County;
import com.homefellas.model.location.LocationAlias;
import com.homefellas.model.location.State;
import com.homefellas.model.location.Zip;

public interface ILocationDao {

	public List<LocationAlias> getCityAliases();
	public List<LocationAlias> getStateAliases();
	public List<LocationAlias> getCountyAliases();
	public List<Zip> getZips();
	public List<State> getStates();
	public List<County> getCounties();
	
	//create
	public County createCounty(County county);
	public State createState(State state);
	public Zip createZip(Zip zip);
	public LocationAlias createLocationAlias(LocationAlias locationAlias);
	public Country createCountry(Country country);
}
