package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Objects;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public void saveTicket(Ticket ticket){

        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET))
        {
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            //ps.setInt(1,ticket.getId());
            ps.setInt(1,ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)?null: (new Timestamp(ticket.getOutTime().getTime())) );
            ps.execute();
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }
    }

    public Ticket getTicket(String vehicleRegNumber) {
        Ticket ticket = null;
        ResultSet rs = null;
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET))
        {
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
        }
        return ticket;
    }

    public int getTotalTicket() {
        int result= 0;
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.GET_TOTAL_TICKET);
             ResultSet rs = ps.executeQuery())
        {
            if(rs.next()){
                result = rs.getInt(1);
            }
        } catch (Exception ex){
            logger.error("Error fetching ticket: {} ", ex.getMessage());
        }
        return result;
    }

    public void updateTicket(Ticket ticket) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET))
        {
            ps.setTimestamp(1, new Timestamp(ticket.getInTime().getTime()));
            ps.setInt(2,ticket.getId());
            ps.execute();
        } catch (Exception ex) {
            logger.error("Error saving ticket info",ex);
        }
    }

    public boolean updateTicketAfterParkingLotExit(Ticket ticket) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET_AFTER_EXIT))
        {
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }
        return false;
    }

    /**
     * Count the number of tickets saved in a database under a car registration number.
     *
     * @param vehicleRegNumber Car registration number to search for.
     * @return the number of tickets or -1 if none found.
     */
    public int getNbTicket(String vehicleRegNumber) {
        int count = -1;
        ResultSet rs = null;

        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.GET_NB_TICKET))
        {
            ps.setString(1, vehicleRegNumber);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            logger.info("Number of ticket for car with registration number %s is %d".formatted(vehicleRegNumber, count));
        } catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
        }
        return count;
    }

    public boolean isRecurrentUser(String vehicleRegNumber) {
        try (Connection con = dataBaseConfig.getConnection()){
            String result = "";
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_VEHICLE_REG_NUMBER);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                result = rs.getString(1);
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);

            return Objects.equals(result, vehicleRegNumber);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }
        return false;
    }
}
