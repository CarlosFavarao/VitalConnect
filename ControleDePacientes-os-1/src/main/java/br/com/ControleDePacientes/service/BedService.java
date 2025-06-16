package br.com.ControleDePacientes.service;

import br.com.ControleDePacientes.dto.BedDTO;
import br.com.ControleDePacientes.dto.BedResponseDTO;
import br.com.ControleDePacientes.enums.SpecialtyEnum;
import br.com.ControleDePacientes.model.BedModel;
import br.com.ControleDePacientes.projections.AvailableBedProjection;
import br.com.ControleDePacientes.repository.BedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BedService {
    @Autowired
    private BedRepository bedRepository;

    @Transactional
    public BedModel save(BedModel bed){
        return this.bedRepository.save(bed);
    }

    @Transactional(readOnly = true)
    public BedModel findById(Long id){
        return this.bedRepository.findById(id).orElseThrow(() -> new RuntimeException("Cama não encontrada."));
    }

    @Transactional(readOnly = true)
    public Map<String, List<BedDTO>> findAllBeds() {
        List<BedModel> beds = bedRepository.findAllBeds();

        Map<String, List<BedDTO>> groupedByHospital = new TreeMap<>();

        for (BedModel bed : beds) {
            BedDTO dto = BedDTO.fromEntity(bed);
            groupedByHospital.computeIfAbsent(dto.getHospitalName(), k -> new ArrayList<>()).add(dto);
        }

        return groupedByHospital;
    }

    @Transactional(readOnly = true)
    public Optional<BedResponseDTO> findByBedId(Long id){
        return this.bedRepository.findById(id).map(BedResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public Map<SpecialtyEnum, List<BedDTO>> findAvailableBedsByHospital(Long hospitalId) {
        List<AvailableBedProjection> projections = bedRepository.findAvailableBedsByHospital(hospitalId);

        Map<SpecialtyEnum, List<BedDTO>> groupedBeds = new TreeMap<>();

        for (AvailableBedProjection projection : projections) {
            BedDTO dto = BedDTO.fromProjection(projection);
            groupedBeds.computeIfAbsent(dto.getSpecialty(), k -> new ArrayList<>()).add(dto);
        }

        for (List<BedDTO> bedList : groupedBeds.values()) {
            bedList.sort(Comparator.comparing(BedDTO::getRoomCode).thenComparing(BedDTO::getBedCode));
        }

        return groupedBeds;
    }

    @Transactional(readOnly = true)
    public List<BedResponseDTO> findBedsByHospitalId(Long hospitalId){
        List<BedModel> beds = this.bedRepository.findBedsByHospitalId(hospitalId);

        return beds.stream().map(BedResponseDTO::new).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<BedDTO> findAvailableBedsByHospitalIdAndSpecialty(Long hospitalId, String specialtyName, Pageable pageable){
        Page<AvailableBedProjection> projectionPage = this.bedRepository.findAvailableBedsByHospitalIdAndSpecialty(hospitalId, specialtyName, pageable);

        return projectionPage.map(BedDTO::fromProjection);
    }
}
