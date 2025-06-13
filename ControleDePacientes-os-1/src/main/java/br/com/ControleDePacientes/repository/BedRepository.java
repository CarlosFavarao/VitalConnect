package br.com.ControleDePacientes.repository;

import br.com.ControleDePacientes.model.BedModel;
import br.com.ControleDePacientes.projections.AvailableBedProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<BedModel, Long> {

    // Em BedRepository.java
    @Query(
            nativeQuery = true,
            value = "select " +
                    "    h.id as hospitalId, h.name as hospitalName, w.specialty as specialty, " +
                    "    r.id as roomId, r.code as roomCode, b.id as bedId, b.code as bedCode " +
                    "from beds b " +
                    "join rooms r on b.room_id = r.id " +
                    "join wards w on r.ward_id = w.id " +
                    "join hospitals h on w.hospital_id = h.id " +
                    "where b.patient_id is null and w.hospital_id = :hospitalId"
    )
    List<AvailableBedProjection> findAvailableBedsByHospital(@Param("hospitalId") Long hospitalId);

    @Query("SELECT b FROM BedModel b WHERE b.room.ward.hospital.id = :hospitalId")
    List<BedModel> findBedsByHospitalId(@Param("hospitalId") Long hospitalId);

    //Mostra camas disponíveis
    @Query(nativeQuery = true, value =
            "select " +
                    "h.id as hospitalId, " +
                    "h.name as hospitalName, " +
                    "w.specialty as specialty, " +
                    "b.id as bedId, " +
                    "b.code as bedCode, " +
                    "b.status as bedStatus, " +
                    "r.id as roomId, " +
                    "r.code as roomCode " +
                    "from beds b " +
                    "join rooms r on b.room_id = r.id " +
                    "join wards w on r.ward_id = w.id " +
                    "join hospitals h on w.hospital_id = h.id " +
                    "where b.status = 'AVAILABLE' " +
                    "order by w.specialty;")
    Page<AvailableBedProjection> findAvailableBeds(Pageable pageable);

    @Query(nativeQuery = true, value =
    "select " +
            "h.id as hospitalId, " +
            "h.name as hospitalName, " +
            "w.specialty as specialty, " +
            "r.id as roomId, " +
            "r.code as roomCode, " +
            "b.id as bedId, " +
            "b.code as bedCode " +
            "from beds b " +
            "join rooms r on b.room_id = r.id " +
            "join wards w on r.ward_id = w.id " +
            "join hospitals h on w.hospital_id = h.id " +
            "where b.patient_id is null " +
            "and w.hospital_id = :hospitalId " +
            "and w.specialty = :specialtyName",
    countQuery = "select count(b.id) " +
            "from beds b " +
            "join rooms r on b.room_id = r.id " +
            "join wards w on r.ward_id = w.id " +
            "where b.patient_id is null and w.hospital_id = :hospitalId and w.specialty = :specialtyName")
    Page<AvailableBedProjection> findAvailableBedsByHospitalIdAndSpecialty(@RequestParam("hospitalId") Long hospitalId, @RequestParam("specialtyName") String specialtyName, Pageable pageable);
}
