package com.smarthomes.dao;

import com.smarthomes.models.Address;
import com.smarthomes.util.MySQLDataStoreUtilities;

import java.sql.SQLException;
import java.util.List;

public class AddressDAO {
    public List<Address> getAddressesByUserId(int userId) throws SQLException {
        return MySQLDataStoreUtilities.getAddressesByUserId(userId);
    }

    public Address getAddressByIdAndUserId(int addressId, int userId) throws SQLException {
        return MySQLDataStoreUtilities.getAddressByIdAndUserId(addressId, userId);
    }

    public void createAddress(Address address) throws SQLException {
        MySQLDataStoreUtilities.createAddress(address);
    }

    public void updateAddress(Address address) throws SQLException {
        MySQLDataStoreUtilities.updateAddress(address);
    }

    public void deleteAddress(int addressId, int userId) throws SQLException {
        MySQLDataStoreUtilities.deleteAddress(addressId, userId);
    }
}