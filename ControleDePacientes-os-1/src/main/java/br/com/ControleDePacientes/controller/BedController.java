package br.com.ControleDePacientes.controller;

import br.com.ControleDePacientes.dto.AvailableBedDTO;
import br.com.ControleDePacientes.dto.BedResponseDTO;
import br.com.ControleDePacientes.enums.SpecialtyEnum;
import br.com.ControleDePacientes.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/beds")
public class BedController {
    @Autowired
    private BedService bedService;

    @GetMapping
    public List<BedResponseDTO> findAllBeds(){
        return this.bedService.findAllBeds();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BedResponseDTO> findBedById(@PathVariable Long id){
        return this.bedService.findByBedId(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{hospitalId}/available")
    public ResponseEntity<Map<SpecialtyEnum, List<AvailableBedDTO>>> getAvailableBedsByHospital( //Usa o treeMap para agrupar os quartos por especialidade
            @PathVariable Long hospitalId) {
        Map<SpecialtyEnum, List<AvailableBedDTO>> availableBeds = bedService.findAvailableBedsByHospital(hospitalId);
        return ResponseEntity.ok(availableBeds);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<AvailableBedDTO>> getAvailableBeds(Pageable pageable) {
        Page<AvailableBedDTO> availableBedsPage = this.bedService.findAvailableBeds(pageable);
        return ResponseEntity.ok(availableBedsPage);
    }

    @GetMapping("/available/{hospitalId}/{specialtyName}")
    public ResponseEntity<Page<AvailableBedDTO>> getAvailableBedsByHospitalIdAndSpecialty(@PathVariable Long hospitalId, @PathVariable String specialtyName, Pageable pageable){
        Page<AvailableBedDTO> bedsPage = this.bedService.findAvailableBedsByHospitalIdAndSpecialty(hospitalId, specialtyName, pageable);
        return ResponseEntity.ok(bedsPage);
    }
}
