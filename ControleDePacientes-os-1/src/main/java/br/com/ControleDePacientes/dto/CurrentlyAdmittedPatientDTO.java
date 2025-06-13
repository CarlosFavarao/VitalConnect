package br.com.ControleDePacientes.dto;

import br.com.ControleDePacientes.enums.SpecialtyEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentlyAdmittedPatientDTO {

    private Long hospitalId;
    private String hospitalName;
    private SpecialtyEnum specialty;
    private String roomCode;
    private String patientName;
    private LocalDateTime admissionDate;
    private long daysAdmitted;

}