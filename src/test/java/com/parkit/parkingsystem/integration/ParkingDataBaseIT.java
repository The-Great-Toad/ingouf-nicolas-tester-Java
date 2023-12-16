package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingService parkingService;
    private static final String carRegistrationNumber = "ABCDEF";
    private static int totalParkingSpots;
    private static int totalAvailableSpots;
    private static int totalTickets;
    private static Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

//    @Test
//    public void clearDatabaseEntries() {
//        dataBasePrepareService.clearDataBaseEntries();
//    }

    @BeforeEach
    public void setUpPerTest() {
        System.out.println("##### START OF TEST #####");

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(carRegistrationNumber);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        dataBasePrepareService.clearDataBaseEntries();
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        totalParkingSpots = parkingSpotDAO.getTotalParkingSpotNumber();
        totalAvailableSpots = parkingSpotDAO.getTotalAvailableParkingSpotNumber();
        totalTickets = ticketDAO.getTotalTicket();
    }

    @AfterAll
    public static void tearDown(){
        dataBasePrepareService = new DataBasePrepareService();
        System.out.println("##### END OF TEST #####");
    }

    /**
     * Check that a ticket is actually saved in DB and the Parking table is updated with availability.
     */
    @Test
    public void testParkingACar(){

        checkParkingTable(true);
        checkTicketInDatabase(true);

        parkingService.processIncomingVehicle();

        updateTicketInTimeForTest();

        checkParkingTable(false);
        checkTicketInDatabase(false);
    }

    /**
     * Check that the fare generated and out time are populated correctly in the database.
     */
    @Test
    public void testParkingLotExit(){

        testParkingACar();
        parkingService.processExitingVehicle();

        checkUpdatedTicketInDatabaseAfterCarExit();
    }

    /**
     * Check ticket discounted price for recurring customer.
     */
    @Test
    public void testParkingLotExitRecurringUser() {
        System.out.println("### First visit ###");
        parkingService.processIncomingVehicle();
        updateTicketInTimeForTest();
        parkingService.processExitingVehicle();

        System.out.println("### Second visit, discount should apply ###");
        parkingService.processIncomingVehicle();
        updateTicketInTimeForTest();
        parkingService.processExitingVehicle();

        totalTickets = ticketDAO.getNbTicket(carRegistrationNumber);

        System.out.println("### Recurring customer ###");
        assertTrue(totalTickets > 1);
        assertEquals(2, totalTickets);
        System.out.printf("Total ticket number for car registration \"%s\" is: %d\n", carRegistrationNumber, totalTickets);
    }

    /**
     * Verify Parking table in a database before or after car entrance.
     *
     * @param isBeforeCarEntrance boolean
     */
    private void checkParkingTable(boolean isBeforeCarEntrance) {

        if (isBeforeCarEntrance) {
            System.out.println("### Before car entrance ###");
            assertEquals(totalParkingSpots, totalAvailableSpots);
        } else {
            totalAvailableSpots = parkingSpotDAO.getTotalAvailableParkingSpotNumber();
            System.out.println("### After car entrance ###");
            assertEquals(totalParkingSpots - 1, totalAvailableSpots);
        }
        System.out.printf("Total parking spots: %d\nTotal available spots: %d\n", totalParkingSpots, totalAvailableSpots);
    }

    /**
     * Verify the presence of the ticket in the database.
     *
     * @param isBeforeCarEntrance boolean
     */
    private void checkTicketInDatabase(boolean isBeforeCarEntrance) {

        if (isBeforeCarEntrance) {
            assertEquals(0, totalTickets);
        } else {
            totalTickets = ticketDAO.getTotalTicket();
            assertEquals(1, totalTickets);
            assertNotNull(ticket);
        }
        System.out.printf("Total tickets: %d\n", totalTickets);
    }

    /**
     * Verify ticket's price & exit time after vehicle parking lot exit.
     */
    private void checkUpdatedTicketInDatabaseAfterCarExit() {
        ticket = ticketDAO.getTicket(carRegistrationNumber);

        System.out.println("### After car exit ###");
        assertNotEquals(0, ticket.getPrice());
        System.out.printf("Ticket's fare is: $%.2f\n", ticket.getPrice());
        assertNotNull(ticket.getOutTime());
        System.out.printf("Ticket exit time is: %s\n", ticket.getOutTime());
    }

    /**
     * Update ticket's intime for test purposes.
     */
    private void updateTicketInTimeForTest() {
        ticket = ticketDAO.getTicket(carRegistrationNumber);
        Date inTime = new Date();
        inTime.setTime(new Date().getTime() - 6000000);
        ticket.setInTime(inTime);
        ticketDAO.updateTicket(ticket);
    }
}
