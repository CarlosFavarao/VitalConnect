package br.com.ControleDePacientes.service;

import br.com.ControleDePacientes.dto.*;
import br.com.ControleDePacientes.enums.BedStatus;
import br.com.ControleDePacientes.enums.SpecialtyEnum;
import br.com.ControleDePacientes.model.*;
import br.com.ControleDePacientes.projections.LogProjection;
import br.com.ControleDePacientes.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdmissionLogService {

    @Autowired private PatientService patientService;
    @Autowired private BedService bedService;
    @Autowired private AdmissionLogRepository admissionLogRepository;

    @Transactional
    public AdmissionResponseDTO admitPatient(AdmissionRequestDTO admissionRequest){
        if (admissionRequest.getPatientId() == null || admissionRequest.getBedId() == null) {
            throw new IllegalArgumentException("ID do Paciente e Id do Leito são obrigatórios.");
        }

        //Verifica se o paciente já está internado
        this.admissionLogRepository.findActiveAdmissionByPatientId(admissionRequest.getPatientId())
                .ifPresent(admission -> {
                    throw new IllegalStateException("Paciente já possui uma internação ativa.");
                });

        //Busca o paciente o leito para internação (logo abaixo busca o leito também)
        PatientModel patient = this.patientService.findById(admissionRequest.getPatientId());
        BedModel bed = this.bedService.findById(admissionRequest.getBedId());

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalStateException("O leito " + bed.getCode() + " não está disponível.");
        }

        bed.setPatient(patient);
        bed.setStatus(BedStatus.OCCUPIED);
        this.bedService.save(bed);

        AdmissionLogModel admissionLog = new AdmissionLogModel();
        admissionLog.setPatient(patient);
        admissionLog.setBed(bed);
        admissionLog.setAdmissionDate(LocalDateTime.now());
        admissionLog.setDischargeDate(null);

        AdmissionLogModel savedLog = this.admissionLogRepository.save(admissionLog);
        return new AdmissionResponseDTO(savedLog);
    }

    //Dar alta
    @Transactional
    public AdmissionLogModel dischargePatient(Long patientId){
        AdmissionLogModel activeAdmission = this.admissionLogRepository.findActiveAdmissionByPatientId(patientId).orElseThrow(() -> new RuntimeException("Internação não encontrada"));

        activeAdmission.setDischargeDate(LocalDateTime.now());
        AdmissionLogModel updatedAdmissionLog = this.admissionLogRepository.save(activeAdmission);

        BedModel bed = activeAdmission.getBed();
        bed.setPatient(null);
        bed.setStatus(BedStatus.AVAILABLE);
        this.bedService.save(bed);

        return updatedAdmissionLog;
    }

    @Transactional(readOnly = true)
    public Map<SpecialtyEnum, List<CurrentlyAdmittedPatientDTO>> getCurrentlyAdmittedPatients() {
        List<AdmissionLogModel> activeAdmissions = admissionLogRepository.findActiveAdmissions();

        Map<SpecialtyEnum, List<CurrentlyAdmittedPatientDTO>> reportMap = new TreeMap<>();

        for (AdmissionLogModel log : activeAdmissions) {
            WardModel ward = log.getBed().getRoom().getWard();
            HospitalModel hospital = ward.getHospital();
            RoomModel room = log.getBed().getRoom();
            PatientModel patient = log.getPatient();

            long daysAdmitted = ChronoUnit.DAYS.between(log.getAdmissionDate(), LocalDateTime.now());

            CurrentlyAdmittedPatientDTO dto = new CurrentlyAdmittedPatientDTO(
                    hospital.getId(),
                    hospital.getName(),
                    ward.getSpecialty(),
                    room.getCode(),
                    patient.getName(),
                    log.getAdmissionDate(),
                    daysAdmitted
            );

            reportMap.computeIfAbsent(dto.getSpecialty(), k -> new ArrayList<>()).add(dto);
        }

        for (List<CurrentlyAdmittedPatientDTO> patientList : reportMap.values()) {
            patientList.sort(Comparator.comparing(CurrentlyAdmittedPatientDTO::getPatientName));
        }

        return reportMap;
    }

    @Transactional(readOnly = true)
    public Page<LogDTO> getAdmissionHistoryByPatientId(Long patientId, Pageable pageable){
        Page<LogProjection> page = this.admissionLogRepository.findAdmissionHistoryByPatientId(patientId, pageable);
        return page.map(LogDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<BedHistoryDTO> getBedAdmissionHistory(Long bedId, Pageable pageable){
        return this.admissionLogRepository.findBedAdmissionHistoryById(bedId, pageable);
    }
}
