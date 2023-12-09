package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private static final double hourInMillis = 3600000.0;
    private static final double periodDiscountedInHour = (29.9 * 60000) / hourInMillis;

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            System.out.println("Exit time before entry time: " + ticket.getOutTime().before(ticket.getInTime()));
            throw new IllegalArgumentException("Out time provided is incorrect: " + ticket.getOutTime().toString());
        }

        System.out.println("Ticket entry date: " + ticket.getInTime());
        System.out.printf("Ticket exit date: %s %n%n", ticket.getOutTime());

        /* Problème du calcul du prix provenant sur le temps du ticket */
        int inHour = ticket.getInTime().getHours();
        int outHour = ticket.getOutTime().getHours();
        int duration = outHour - inHour;
        System.out.printf("inhour: %d %noutHout: %d %nduration: %d %n%n", inHour, outHour, duration);

        /* Solution apportée */
        long inMillis = ticket.getInTime().getTime();
        long outMillis = ticket.getOutTime().getTime();
        double durationInHour = (outMillis - inMillis) / hourInMillis;
        System.out.printf("inMillis: %d %noutMillis: %d %ndurationInHour: %.2f %n%n", inMillis, outMillis, durationInHour);

        // Check for a free of charge period
        if (durationInHour < 0.5) {
            System.out.printf("duration in minutes: %d %n%n", (int)(durationInHour * 60));
            ticket.setPrice(0.0); // Free parking under 30min
        } else {
            durationInHour -= periodDiscountedInHour; // subtract period free of charge
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR -> ticket.setPrice(durationInHour * Fare.CAR_RATE_PER_HOUR);
                case BIKE -> ticket.setPrice(durationInHour * Fare.BIKE_RATE_PER_HOUR);
                default -> throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
        // Check for discount to apply
        if (discount) {
            System.out.println("You have received a 5% discount on your exit fare.");
            ticket.setPrice(ticket.getPrice() * 0.95); // Apply a 5% discount
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}