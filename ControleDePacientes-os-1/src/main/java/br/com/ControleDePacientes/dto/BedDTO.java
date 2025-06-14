package br.com.ControleDePacientes.dto;

import br.com.ControleDePacientes.enums.BedStatus;
import br.com.ControleDePacientes.enums.SpecialtyEnum;
import br.com.ControleDePacientes.model.BedModel;
import br.com.ControleDePacientes.projections.AvailableBedProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BedDTO {
    private Long hospitalId;
    private String hospitalName;
    private SpecialtyEnum specialty;
    private Long bedId;
    private String bedCode;
    private Long roomId;
    private String roomCode;
    private BedStatus bedStatus;

    public static BedDTO fromProjection(AvailableBedProjection projection) {
        return new BedDTO(
                projection.getHospitalId(),
                projection.getHospitalName(),
                SpecialtyEnum.valueOf(projection.getSpecialty()),
                projection.getBedId(),
                projection.getBedCode(),
                projection.getRoomId(),
                projection.getRoomCode(),
                BedStatus.valueOf(projection.getBedStatus())
        );
    }

    public static BedDTO fromEntity(BedModel bed) {
        return new BedDTO(
                bed.getRoom().getWard().getHospital().getId(),
                bed.getRoom().getWard().getHospital().getName(),
                bed.getRoom().getWard().getSpecialty(),
                bed.getId(),
                bed.getCode(),
                bed.getRoom().getId(),
                bed.getRoom().getCode(),
                bed.getStatus()
        );
    }
}