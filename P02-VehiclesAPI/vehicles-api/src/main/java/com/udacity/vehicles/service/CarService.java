package com.udacity.vehicles.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;

    WebClient maps;
    WebClient pricing;


    public CarService(CarRepository repository, WebClient maps, WebClient pricing) {
        this.repository = repository;
        this.maps = maps;
        this.pricing = pricing;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {

        Car car = null;
        Optional<Car> optionalCar = repository.findById(id);
        if(optionalCar.isPresent()) car = optionalCar.get();
        else throw new CarNotFoundException();

        try {
            String priceStr = pricing.get().uri("/services/price?vehicleId="+id).toString();
            Price price = new ObjectMapper().readValue(priceStr, Price.class);
            car.setPrice(price.getCurrency()+price.getPrice());
        } catch (Exception ex){
            //ignore
        }

        /**
         * TODO: Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service.
         * TODO: Set the location of the vehicle, including the address information
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         * http://localhost:9191/maps?lat=1&lon=1
         */
        try {
            Location loc = car.getLocation();
            String uriStr = String.format("/maps?lat={0}&lon={1}", loc.getLat(), loc.getLon());
            String mapStr = pricing.get().uri(uriStr).toString();
            Address address = new ObjectMapper().readValue(mapStr, Address.class);
            loc.setAddress(address.getAddress());
            loc.setCity(address.getCity());
            loc.setState(address.getState());
            loc.setZip(address.getZip());
        } catch (Exception ex){
            //ignore
        }


        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Car car = null;
        Optional<Car> optionalCar = repository.findById(id);
        if(optionalCar.isPresent()) car = optionalCar.get();
        else throw new CarNotFoundException();

        this.repository.delete(car);
    }
}
