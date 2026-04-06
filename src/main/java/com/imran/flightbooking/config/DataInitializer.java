package com.imran.flightbooking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.imran.flightbooking.entity.Airline;
import com.imran.flightbooking.entity.Airport;
import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.repository.AirlineRepository;
import com.imran.flightbooking.repository.AirportRepository;
import com.imran.flightbooking.repository.FlightRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AirlineRepository airlineRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== Starting DataInitializer ===");
        System.out.println("Current flights count: " + flightRepository.count());
        
        // Check if airports already exist
        if (airportRepository.count() == 0) {
            System.out.println("Initializing airports...");
            // Add airports to database
            Airport del = new Airport();
            del.setIataCode("DEL");
            del.setAirportName("Indira Gandhi International Airport");
            del.setCity("New Delhi");
            del.setCountry("India");
            airportRepository.save(del);
            
            Airport bom = new Airport();
            bom.setIataCode("BOM");
            bom.setAirportName("Chhatrapati Shivaji Maharaj International Airport");
            bom.setCity("Mumbai");
            bom.setCountry("India");
            airportRepository.save(bom);
            
            Airport blr = new Airport();
            blr.setIataCode("BLR");
            blr.setAirportName("Kempegowda International Airport");
            blr.setCity("Bangalore");
            blr.setCountry("India");
            airportRepository.save(blr);
            
            Airport hyd = new Airport();
            hyd.setIataCode("HYD");
            hyd.setAirportName("Rajiv Gandhi International Airport");
            hyd.setCity("Hyderabad");
            hyd.setCountry("India");
            airportRepository.save(hyd);
            
            Airport ccu = new Airport();
            ccu.setIataCode("CCU");
            ccu.setAirportName("Netaji Subhas Chandra Bose International Airport");
            ccu.setCity("Kolkata");
            ccu.setCountry("India");
            airportRepository.save(ccu);
        }
        
        // Check if airlines already exist
        if (airlineRepository.count() == 0) {
            System.out.println("Initializing airlines...");
            // Add airlines to database
            Airline ai = airlineRepository.save(new Airline("Air India", "AI", "India"));
            Airline indigo = airlineRepository.save(new Airline("IndiGo", "6E", "India"));
            Airline vistara = airlineRepository.save(new Airline("Vistara", "UK", "India"));
            Airline aie = airlineRepository.save(new Airline("Air India Express", "IX", "India"));
            Airline ua = airlineRepository.save(new Airline("United Airlines", "UA", "USA"));
            Airline dl = airlineRepository.save(new Airline("Delta Air Lines", "DL", "USA"));
            Airline aa = airlineRepository.save(new Airline("American Airlines", "AA", "USA"));
            Airline ek = airlineRepository.save(new Airline("Emirates", "EK", "UAE"));
            Airline fz = airlineRepository.save(new Airline("flydubai", "FZ", "UAE"));
            Airline g9 = airlineRepository.save(new Airline("Air Arabia", "G9", "UAE"));
            Airline _3i = airlineRepository.save(new Airline("Air Arabia Abu Dhabi", "3I", "UAE"));

            // Add sample flights if none exist
            if (flightRepository.count() == 0) {
                // DEL to BOM flights
                Flight f1 = new Flight();
                f1.setFlightNumber("AI440");
                f1.setSource("DEL");
                f1.setDestination("BOM");
                f1.setDepartureTime("10:00 AM");
                f1.setArrivalTime("12:30 PM");
                f1.setPrice(5200);
                f1.setAirline(ai);
                flightRepository.save(f1);

                Flight f2 = new Flight();
                f2.setFlightNumber("6E202");
                f2.setSource("DEL");
                f2.setDestination("BOM");
                f2.setDepartureTime("01:30 PM");
                f2.setArrivalTime("04:00 PM");
                f2.setPrice(4800);
                f2.setAirline(indigo);
                flightRepository.save(f2);

                Flight f3 = new Flight();
                f3.setFlightNumber("UK870");
                f3.setSource("DEL");
                f3.setDestination("BOM");
                f3.setDepartureTime("06:15 PM");
                f3.setArrivalTime("08:40 PM");
                f3.setPrice(5900);
                f3.setAirline(vistara);
                flightRepository.save(f3);

                // DEL to BLR flights
                Flight f4 = new Flight();
                f4.setFlightNumber("AI441");
                f4.setSource("DEL");
                f4.setDestination("BLR");
                f4.setDepartureTime("07:00 AM");
                f4.setArrivalTime("09:30 AM");
                f4.setPrice(6100);
                f4.setAirline(ai);
                flightRepository.save(f4);

                Flight f5 = new Flight();
                f5.setFlightNumber("6E205");
                f5.setSource("DEL");
                f5.setDestination("BLR");
                f5.setDepartureTime("12:00 PM");
                f5.setArrivalTime("02:30 PM");
                f5.setPrice(5500);
                f5.setAirline(indigo);
                flightRepository.save(f5);

                // BOM to HYD flights
                Flight f6 = new Flight();
                f6.setFlightNumber("AI442");
                f6.setSource("BOM");
                f6.setDestination("HYD");
                f6.setDepartureTime("08:00 AM");
                f6.setArrivalTime("10:00 AM");
                f6.setPrice(4500);
                f6.setAirline(ai);
                flightRepository.save(f6);

                Flight f7 = new Flight();
                f7.setFlightNumber("6E210");
                f7.setSource("BOM");
                f7.setDestination("HYD");
                f7.setDepartureTime("02:00 PM");
                f7.setArrivalTime("04:00 PM");
                f7.setPrice(4200);
                f7.setAirline(indigo);
                flightRepository.save(f7);

                // BLR to CCU flights
                Flight f8 = new Flight();
                f8.setFlightNumber("UK871");
                f8.setSource("BLR");
                f8.setDestination("CCU");
                f8.setDepartureTime("05:00 AM");
                f8.setArrivalTime("07:30 AM");
                f8.setPrice(5700);
                f8.setAirline(vistara);
                flightRepository.save(f8);

                Flight f9 = new Flight();
                f9.setFlightNumber("6E215");
                f9.setSource("BLR");
                f9.setDestination("CCU");
                f9.setDepartureTime("09:00 AM");
                f9.setArrivalTime("11:30 AM");
                f9.setPrice(5200);
                f9.setAirline(indigo);
                flightRepository.save(f9);
            }
        }
        System.out.println("=== DataInitializer completed ===");
        System.out.println("Final flights count: " + flightRepository.count());
    }
}
