package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private static final long hourInMillis = 3600000L;
    private static final double periodDiscountedInHour = (29.9 * 60000) / hourInMillis;

    @BeforeAll
    public static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    // ######################################################
    // ############### TEST PARKING TYPE: CAR ###############
    // ######################################################
    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - hourInMillis );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double duration = calculateTicketDurationToBeCharge(ticket);
        double expectedFare = duration * Fare.CAR_RATE_PER_HOUR;

        assertNotEquals(0.0, ticket.getPrice());
        assertEquals(expectedFare, ticket.getPrice());

        System.out.printf("Ticket expected price: €%.2f. Actual price: €%.2f %n", expectedFare, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithDiscount(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - hourInMillis );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, true);

        double undiscountedPrice = calculateTicketDurationToBeCharge(ticket) * Fare.CAR_RATE_PER_HOUR;
        double discountedReceived = undiscountedPrice * 0.95;
        double discountInPercent = (undiscountedPrice - ticket.getPrice()) / undiscountedPrice;
        double roundedDiscountInPercent = Math.round(discountInPercent * 100.0) / 100.0;

        assertEquals(discountedReceived, ticket.getPrice());
        assertEquals(0.05, roundedDiscountInPercent); // Recurrent user get a 5% discount

        System.out.printf("\nTicket price without discount: €%.2f %nTicket price with discount: €%.2f %n", undiscountedPrice, discountedReceived);
        System.out.println("Discount received: " + (roundedDiscountInPercent * 100) + "%");
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000)); // 29-minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals( ticket.getPrice(), 0.0);
        System.out.printf("Ticket price: €%.2f %n", ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) ); // 45-minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double duration = calculateTicketDurationToBeCharge(ticket);
        double expectedFare = duration * Fare.CAR_RATE_PER_HOUR;

        assertNotEquals(0.0, ticket.getPrice());
        assertEquals(expectedFare, ticket.getPrice());

        System.out.printf("Ticket expected price: €%.2f. Actual price: €%.2f %n", expectedFare, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  25 * 60 * 60 * 1000) ); // 24-hours parking time should give 24 * parking fares per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double duration = calculateTicketDurationToBeCharge(ticket);
        double expectedFare = duration * Fare.CAR_RATE_PER_HOUR;

        assertNotEquals(0.0, ticket.getPrice());
        assertEquals(expectedFare, ticket.getPrice());

        System.out.printf("Ticket expected price: €%.2f. Actual price: €%.2f %n", expectedFare, ticket.getPrice());
    }

    // ######################################################
    // ############## TEST PARKING TYPE: Bike ###############
    // ######################################################
    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double duration = calculateTicketDurationToBeCharge(ticket);
        double expectedFare = duration * Fare.BIKE_RATE_PER_HOUR;

        assertNotEquals(0.0, ticket.getPrice());
        assertEquals(expectedFare, ticket.getPrice());

        System.out.printf("Ticket expected price: €%.2f. Actual price: €%.2f %n", expectedFare, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithDiscount(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, true);

        double undiscountedPrice = calculateTicketDurationToBeCharge(ticket) * Fare.BIKE_RATE_PER_HOUR;
        double discountedReceived = undiscountedPrice * 0.95;
        double discountInPercent = (undiscountedPrice - ticket.getPrice()) / undiscountedPrice;
        double roundedDiscountInPercent = Math.round(discountInPercent * 100.0) / 100.0;

        assertEquals(discountedReceived, ticket.getPrice());
        assertEquals(0.05, roundedDiscountInPercent); // Recurrent user get a 5% discount

        System.out.printf("\nTicket price without discount: €%.2f %nTicket price with discount: €%.2f %n", undiscountedPrice, discountedReceived);
        System.out.println("Discount received: " + (roundedDiscountInPercent * 100) + "%");
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000)); // 29-minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals( ticket.getPrice(), 0.0);
        System.out.printf("Ticket price: €%.2f %n", ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) ); // 45-minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double duration = calculateTicketDurationToBeCharge(ticket);
        double expectedFare = duration * Fare.BIKE_RATE_PER_HOUR;

        assertNotEquals(0.0, ticket.getPrice());
        assertEquals(expectedFare, ticket.getPrice());

        System.out.printf("Ticket expected price: €%.2f. Actual price: €%.2f %n", expectedFare, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (25 * 60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double duration = calculateTicketDurationToBeCharge(ticket);
        double expectedFare = duration * Fare.BIKE_RATE_PER_HOUR;

        assertNotEquals(0.0, ticket.getPrice());
        assertEquals(expectedFare, ticket.getPrice());

        System.out.printf("Ticket expected price: €%.2f. Actual price: €%.2f %n", expectedFare, ticket.getPrice());
    }

    // ######################################################
    // ############ TEST PARKING TYPE: UNKNOWN ##############
    // ######################################################
    @Test
    public void calculateFareUnknownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    /**
     * Calculate the duration between a ticket entry time and exit time.
     * Needs to be greater than 30 minutes.
     *
     * @param ticket the ticket needs to have an entry and exit time set.
     * @return the duration
     */
    private double calculateTicketDurationToBeCharge(Ticket ticket) {
        long inMillis = ticket.getInTime().getTime();
        long outMillis = ticket.getOutTime().getTime();
        double duration = (double) (outMillis - inMillis) / hourInMillis;
        return duration - periodDiscountedInHour;
    }
}
