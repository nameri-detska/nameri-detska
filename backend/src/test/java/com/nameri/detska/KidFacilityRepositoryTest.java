package com.nameri.detska;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class KidFacilityRepositoryTest {

    @Test
    void shouldMapAllFields() throws Exception {
        UUID id = UUID.randomUUID();
        ResultSet rs = mockResultSetWithRow(id, "ДГ №1", "KINDERGARTEN", "MUNICIPAL", "гр. София, ул. А", 42.7, 23.3);
        when(rs.next()).thenReturn(true, false);
        KidFacilityRepository repository = repositoryWithResultSet(rs);

        List<KidFacility> facilities = repository.findAll();

        assertEquals(1, facilities.size());
        KidFacility facility = facilities.get(0);
        assertEquals("ДГ №1", facility.getName());
        assertEquals(KidFacilityType.KINDERGARTEN, facility.getKidFacilityType());
        assertEquals(KidFacilityOwnershipType.MUNICIPAL, facility.getKidFacilityOwnershipType());
        assertEquals("гр. София, ул. А", facility.getAddress());
        assertEquals(42.7, facility.getLatitude(), 0.0001);
        assertEquals(23.3, facility.getLongitude(), 0.0001);
        assertNotNull(facility.getId());
    }

    @Test
    void shouldMapAllFacilityTypes() throws Exception {
        assertEnumMapping("KINDERGARTEN", KidFacilityType.KINDERGARTEN);
        assertEnumMapping("KINDERGARTEN_WITH_NURSERY", KidFacilityType.KINDERGARTEN_WITH_NURSERY);
        assertEnumMapping("NURSERY", KidFacilityType.NURSERY);
    }

    @Test
    void shouldMapAllOwnershipTypes() throws Exception {
        assertOwnershipMapping("MUNICIPAL", KidFacilityOwnershipType.MUNICIPAL);
        assertOwnershipMapping("PRIVATE_SRZI", KidFacilityOwnershipType.PRIVATE_SRZI);
        assertOwnershipMapping("PRIVATE_MON", KidFacilityOwnershipType.PRIVATE_MON);
    }

    @Test
    void shouldReturnEmptyListForEmptyResultSet() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        KidFacilityRepository repository = repositoryWithResultSet(rs);

        List<KidFacility> facilities = repository.findAll();

        assertTrue(facilities.isEmpty());
    }

    @Test
    void shouldThrowRuntimeExceptionOnSqlException() throws Exception {
        DataSource dataSource = Mockito.mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        KidFacilityRepository repository = new KidFacilityRepository(dataSource);

        RuntimeException exception = assertThrows(RuntimeException.class, repository::findAll);
        assertTrue(exception.getMessage().contains("Failed to fetch facilities"));
    }

    private void assertEnumMapping(String dbValue, KidFacilityType expected) throws Exception {
        ResultSet rs = mockResultSetWithRow(UUID.randomUUID(), "Test", dbValue, "MUNICIPAL", "addr", 0.0, 0.0);
        when(rs.next()).thenReturn(true, false);

        KidFacilityRepository repository = repositoryWithResultSet(rs);
        List<KidFacility> facilities = repository.findAll();

        assertEquals(expected, facilities.get(0).getKidFacilityType());
    }

    private void assertOwnershipMapping(String dbValue, KidFacilityOwnershipType expected) throws Exception {
        ResultSet rs = mockResultSetWithRow(UUID.randomUUID(), "Test", "KINDERGARTEN", dbValue, "addr", 0.0, 0.0);
        when(rs.next()).thenReturn(true, false);

        KidFacilityRepository repository = repositoryWithResultSet(rs);
        List<KidFacility> facilities = repository.findAll();

        assertEquals(expected, facilities.get(0).getKidFacilityOwnershipType());
    }

    private static KidFacilityRepository repositoryWithResultSet(ResultSet rs) throws SQLException {
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = Mockito.mock(Connection.class);
        when(conn.prepareStatement(Mockito.anyString())).thenReturn(ps);

        DataSource dataSource = Mockito.mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(conn);

        return new KidFacilityRepository(dataSource);
    }

    private static ResultSet mockResultSetWithRow(
        UUID id, String name, String kidFacilityType,
        String kidFacilityOwnershipType, String address, double latitude, double longitude
    ) throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.getObject("id", UUID.class)).thenReturn(id);
        when(rs.getString("name")).thenReturn(name);
        when(rs.getString("kid_facility_type")).thenReturn(kidFacilityType);
        when(rs.getString("kid_facility_ownership_type")).thenReturn(kidFacilityOwnershipType);
        when(rs.getString("address")).thenReturn(address);
        when(rs.getDouble("latitude")).thenReturn(latitude);
        when(rs.getDouble("longitude")).thenReturn(longitude);
        return rs;
    }
}
