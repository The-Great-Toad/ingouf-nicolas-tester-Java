package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @InjectMocks
    private ParkingService parkingService;
    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    private static Ticket ticket;
    private static final String carRegistrationNumber = "ABCDEF";


    @BeforeEach
    public void setUpPerTest() {
        try {
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(carRegistrationNumber);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);

        parkingService.processIncomingVehicle();

        verify(inputReaderUtil, times(1)).readSelection();
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTest() {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(carRegistrationNumber);

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        when(ticketDAO.updateTicketAfterParkingLotExit(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    /**
     * Exécution du test dans le cas où la méthode updateTicket() de ticketDAO renvoie false
     * lors de l’appel de processExitingVehicle()
     */
    @Test
    public void processExitingVehicleTestUnableUpdate() {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(carRegistrationNumber);

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        when(ticketDAO.updateTicketAfterParkingLotExit(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getNbTicket(anyString());
    }

    /**
     * Test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour résultat
     * l’obtention d’un spot dont l’ID est 1 et qui est disponible.
     */
    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        verify(inputReaderUtil, times(1)).readSelection();
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        Assertions.assertEquals(result, new ParkingSpot(1, ParkingType.CAR, true));
    }

    /**
     * Test de l’appel de la méthode getNextParkingNumberIfAvailable()
     * avec pour résultat aucun spot disponible (la méthode renvoie null).
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        verify(inputReaderUtil, times(1)).readSelection();
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        Assertions.assertNull(result);
    }

    /**
     * Test de l’appel de la méthode getNextParkingNumberIfAvailable()
     * avec pour résultat aucun spot (la méthode renvoie null) car l’argument saisi
     * par l’utilisateur concernant le type de véhicule est erroné.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument () {
        when(inputReaderUtil.readSelection()).thenReturn(4);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        verify(inputReaderUtil, times(1)).readSelection();
        Assertions.assertNull(result);
    }

}
