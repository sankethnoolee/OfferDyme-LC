package com.lc.settlement.service;

import com.lc.settlement.dto.FieldAgentDto;
import com.lc.settlement.entity.FieldAgent;
import com.lc.settlement.repository.FieldAgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FieldAgentService {

    private final FieldAgentRepository repo;

    public List<FieldAgentDto> listAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public FieldAgentDto getById(Long id) {
        FieldAgent a = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + id));
        return toDto(a);
    }

    private FieldAgentDto toDto(FieldAgent a) {
        return FieldAgentDto.builder()
                .id(a.getId())
                .name(a.getName())
                .employeeCode(a.getEmployeeCode())
                .email(a.getEmail())
                .region(a.getRegion())
                .lenderName(a.getLender() == null ? null : a.getLender().getName())
                .build();
    }
}
