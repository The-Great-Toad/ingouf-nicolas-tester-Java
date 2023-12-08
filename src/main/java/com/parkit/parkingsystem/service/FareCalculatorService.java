package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private static final double hourInMillis = 3600000.0;

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            System.out.println("Exit time before entry time: " + ticket.getOutTime().before(ticket.getInTime()));
            throw new IllegalArgumentException("Out time provided is incorrect: " + ticket.getOutTime().toString());
        }

        long inMillis = ticket.getInTime().getTime();
        long outMillis = ticket.getOutTime().getTime();

        double durationInHour = (outMillis - inMillis) / hourInMillis;

        if (durationInHour < 0.5) {
            ticket.setPrice(0.0); // Free parking under 30min
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR -> ticket.setPrice(durationInHour * Fare.CAR_RATE_PER_HOUR);
                case BIKE -> ticket.setPrice(durationInHour * Fare.BIKE_RATE_PER_HOUR);
                default -> throw new IllegalArgumentException("Unknown Parking Type");
            }
        }

        if (discount) {
            System.out.println("You have received a 5% discount on your exit fare.");
            ticket.setPrice(ticket.getPrice() * 0.95); // Apply a 5% discount
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}