package com.lc.settlement.service;

import com.lc.settlement.dto.LenderDto;
import com.lc.settlement.entity.Lender;
import com.lc.settlement.repository.LenderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LenderService {

    private final LenderRepository repo;

    public List<LenderDto> listAll() {
        return repo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public LenderDto getById(Long id) {
        Lender l = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lender not found: " + id));
        return toDto(l);
    }

    private LenderDto toDto(Lender l) {
        return LenderDto.builder()
                .id(l.getId())
                .name(l.getName())
                .code(l.getCode())
                .contactEmail(l.getContactEmail())
                .contactPhone(l.getContactPhone())
                .address(l.getAddress())
                .build();
    }
}
