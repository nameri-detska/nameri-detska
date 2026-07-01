package com.nameri.detska;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class KidFacilityRepository {

    private final DataSource dataSource;

    @Inject
    public KidFacilityRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<KidFacility> findAll() {
        String sql = """
            SELECT id, name, kid_facility_type, kid_facility_ownership_type, address, latitude, longitude
            FROM kid_facility
            """;

        try (
            var conn = dataSource.getConnection();
            var stmt = conn.prepareStatement(sql);
            var rs = stmt.executeQuery()
        ) {

            List<KidFacility> facilities = new ArrayList<>();
            while (rs.next()) {
                facilities.add(map(rs));
            }
            return facilities;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch facilities", e);
        }
    }

    private KidFacility map(ResultSet rs) throws SQLException {
        return KidFacility.builder()
            .id(rs.getObject("id", UUID.class))
            .name(rs.getString("name"))
            .kidFacilityType(KidFacilityType.valueOf(rs.getString("kid_facility_type")))
            .kidFacilityOwnershipType(KidFacilityOwnershipType.valueOf(rs.getString("kid_facility_ownership_type")))
            .address(rs.getString("address"))
            .latitude(rs.getDouble("latitude"))
            .longitude(rs.getDouble("longitude"))
            .build();
    }
}
