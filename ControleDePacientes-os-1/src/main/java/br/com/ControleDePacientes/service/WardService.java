package br.com.ControleDePacientes.service;

import br.com.ControleDePacientes.dto.SpecialtyBedStatsDTO;
import br.com.ControleDePacientes.dto.SpecialtyRoomStatsDTO;
import br.com.ControleDePacientes.dto.WardCreateRequestDTO;
import br.com.ControleDePacientes.dto.WardResponseDTO;
import br.com.ControleDePacientes.enums.BedStatus;
import br.com.ControleDePacientes.enums.RoomStatus;
import br.com.ControleDePacientes.enums.SpecialtyEnum;
import br.com.ControleDePacientes.model.BedModel;
import br.com.ControleDePacientes.model.HospitalModel;
import br.com.ControleDePacientes.model.RoomModel;
import br.com.ControleDePacientes.model.WardModel;
import br.com.ControleDePacientes.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WardService {
    @Autowired private WardRepository wardRepository;

    @Lazy
    @Autowired private HospitalService hospitalService;
    @Autowired private RoomService roomService;
    @Autowired private BedService bedService;

    @Transactional
    public WardModel createWardWithRoomsAndBeds(WardCreateRequestDTO dto){ //Cria as Alas com Quartos e leitos prontos, Graças ao hashset
        if (dto.getHospitalId() == null
                || dto.getSpecialty() == null
                || dto.getNumberOfRooms() <= 0
                || dto.getBedsPerRoom() <= 0){
            throw new IllegalArgumentException("Dados inválidos para a criação da Ala.");
        }

        HospitalModel hospital = this.hospitalService.findHospitalById(dto.getHospitalId());

        WardModel newWard = new WardModel();
        newWard.setSpecialty(dto.getSpecialty());
        newWard.setHospital(hospital);
        WardModel savedWard = this.wardRepository.save(newWard);

        String prefix = savedWard.getSpecialty().getPrefix(); //Prefixo para nomenclatura
        String specialtyName = savedWard.getSpecialty().name(); //Nome da especialidade para consulta no BD

        List<Integer> existingNumbersList = this.roomService.findExistingRoomNumbers(hospital.getId(), specialtyName);

        Set<Integer> existingNumbersSet = new HashSet<>(existingNumbersList); //Pega os números resgatados e coloca-os em um hashset, para uma busca rápida e eficaz

        int nextRoomNumber = 1;
        for (int i = 0; i < dto.getNumberOfRooms(); i++) {
            while (existingNumbersSet.contains(nextRoomNumber)) { //Encontra o próximo número vago
                nextRoomNumber++;
            }

            //Cria o quarto baseado nesse número resgatado lá em cima
            RoomModel newRoom = new RoomModel();
            String roomCode = prefix + nextRoomNumber;
            newRoom.setCode(roomCode);
            newRoom.setStatus(RoomStatus.ACTIVE);
            newRoom.setWard(savedWard);
            RoomModel savedRoom = this.roomService.save(newRoom);

            //Adiciona no HashSet (para não ser adicionado o mesmo várias vezes)
            existingNumbersSet.add(nextRoomNumber);

            //Cria os leitos normalmente.
            for (int j = 1; j <= dto.getBedsPerRoom(); j++) {
                BedModel newBed = new BedModel();
                String bedCode = roomCode + "-" + j;
                newBed.setCode(bedCode);
                newBed.setStatus(BedStatus.AVAILABLE);
                newBed.setRoom(savedRoom);
                newBed.setPatient(null);
                bedService.save(newBed);
            }
        }
        return savedWard;
    }

    @Transactional(readOnly = true)
    public List<WardResponseDTO> findAllWards() {
        List<WardModel> wards = this.wardRepository.findAll();
        return wards.stream().map(WardResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WardResponseDTO findWardById(Long id) {
        return this.wardRepository.findById(id).map(WardResponseDTO::new)
                .orElseThrow(() -> new RuntimeException("Ward não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<SpecialtyBedStatsDTO> getBedStatsBySpecialty(){
        return this.wardRepository.getBedStatsBySpecialty();
    }

    @Transactional(readOnly = true)
    public boolean existsByHospitalId(Long hospitalId){
        return this.wardRepository.existsByHospitalId(hospitalId);
    }

    @Transactional(readOnly = true)
    public List<SpecialtyRoomStatsDTO> getRoomStatsBySpecialty() {
        List<WardModel> wardsWithDetails = this.wardRepository.findAllWithRoomsAndBeds();
        Map<SpecialtyEnum, RoomStatsAggregator> statsMap = new HashMap<>();

        for (WardModel ward : wardsWithDetails) {
            SpecialtyEnum specialty = ward.getSpecialty();
            RoomStatsAggregator aggregator = statsMap.computeIfAbsent(specialty, k -> new RoomStatsAggregator());

            for (RoomModel room : ward.getRooms()) {
                aggregator.totalRooms++;

                boolean isRoomOccupiedByPatient = false;
                if (room.getBeds() != null && !room.getBeds().isEmpty()) {
                    for (BedModel bed : room.getBeds()) {
                        if (bed.getStatus() == BedStatus.OCCUPIED) { //
                            isRoomOccupiedByPatient = true;
                            break;
                        }
                    }
                }

                if (isRoomOccupiedByPatient) {
                    aggregator.occupiedRooms++;
                } else {
                    aggregator.freeRooms++;
                }
            }
        }

        List<SpecialtyRoomStatsDTO> result = new ArrayList<>();
        for (Map.Entry<SpecialtyEnum, RoomStatsAggregator> entry : statsMap.entrySet()) {
            result.add(new SpecialtyRoomStatsDTO(
                    entry.getKey(),
                    entry.getValue().totalRooms,
                    entry.getValue().freeRooms,
                    entry.getValue().occupiedRooms
            ));
        }
        return result;
    }

    private static class RoomStatsAggregator {
        long totalRooms = 0;
        long freeRooms = 0;
        long occupiedRooms = 0;
    }


}