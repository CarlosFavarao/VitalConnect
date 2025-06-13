package br.com.ControleDePacientes.controller;

import br.com.ControleDePacientes.dto.*;
import br.com.ControleDePacientes.enums.SpecialtyEnum;
import br.com.ControleDePacientes.model.AdmissionLogModel;
import br.com.ControleDePacientes.service.AdmissionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm")
public class AdmissionLogController {
    @Autowired
    private AdmissionLogService admissionLogService;

    @PostMapping //Internar
    public AdmissionResponseDTO admitPatient(@RequestBody AdmissionRequestDTO admissionRequest) {
        return this.admissionLogService.admitPatient(admissionRequest);
    }

    @GetMapping("/currently-admitted") //Exibir os pacientes admitidos, filtrando por Ala e nome. ALém de exibir dias internados de cada paciente. Tudo isso usando query nativa!!!
    public ResponseEntity<Map<SpecialtyEnum, List<CurrentlyAdmittedPatientDTO>>> getAdmittedPatientsReportWithTreeMap() {
        Map<SpecialtyEnum, List<CurrentlyAdmittedPatientDTO>> report = admissionLogService.getCurrentlyAdmittedPatients();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/history/patient/{patientId}") //Exibir histórico do paciente
    public Page<LogDTO> getAdmissionHistoryByPatientId(@PathVariable Long patientId, Pageable pageable){
        return this.admissionLogService.getAdmissionHistoryByPatientId(patientId, pageable);
    }

    @GetMapping("/history/bed/{bedId}")
    public Page<BedHistoryDTO> getBedAdmissionHistory(@PathVariable Long bedId, Pageable pageable) {
        return this.admissionLogService.getBedAdmissionHistory(bedId, pageable);
    }

    @PutMapping("/discharge/{patientId}") //Dar alta
    public AdmissionLogModel dischargePatient(@PathVariable Long patientId){
        return this.admissionLogService.dischargePatient(patientId);
    }
}
