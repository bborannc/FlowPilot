package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.dto.request.CreateRequestDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;
import com.enoca.flowpilot.exception.ResourceNotFoundException;
import com.enoca.flowpilot.mapper.RequestMapper;
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.EmployeeService;
import com.enoca.flowpilot.service.RequestService;
import com.enoca.flowpilot.service.policy.ApprovalPolicy;
import com.enoca.flowpilot.service.policy.ApprovalPolicyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EmployeeService employeeService;
    private final ApprovalService approvalService;
    private final ApprovalPolicyRegistry policyRegistry;

    @Override
    @Transactional
    public RequestResponseDto createDraft(CreateRequestDto dto) {
        Employee employee = employeeService.getById(dto.getEmployeeId());

        // 1. Sadece DRAFT olarak başlat
        Request request = Request.builder()
                .employee(employee)
                .requestType(dto.getRequestType())
                .priority(dto.getPriority() != null ? dto.getPriority() : RequestPriority.MEDIUM)
                .status(RequestStatus.DRAFT)
                .details(new ArrayList<>()) // NullPointerException koruması
                .build();

        // 2. Detayları ekle
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            dto.getDetails().forEach((key, value) -> {
                RequestDetail detail = RequestDetail.builder()
                        .request(request)
                        .key(key)
                        .value(value)
                        .build();
                request.getDetails().add(detail);
            });
        }

        Request savedRequest = requestRepository.save(request);

        // 3. Draft oluşturulduğunda tarihçe kaydı at
        approvalService.recordHistory(savedRequest, employee.getId(), ApprovalAction.CREATED, "Talep taslak (DRAFT) olarak oluşturuldu.");

        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public RequestResponseDto submitRequest(Long requestId) {
        Request request = requestRepository.findByIdWithDetailsAndSteps(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Yalnızca DRAFT (Taslak) durumundaki talepler onaya sunulabilir.");
        }

        ApprovalPolicy policy = policyRegistry.getPolicy(request.getRequestType());

        // 1. Submit öncesi detay validasyonu
        policy.validateDetails(request);

        // 2. Politika motoru onay adımlarını üretir
        policy.applyPolicy(request);

        request.setStatus(RequestStatus.IN_APPROVAL);
        Request savedRequest = requestRepository.save(request);

        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponseDto getRequestDetails(Long requestId) {
        Request request = requestRepository.findByIdWithDetailsAndSteps(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));
        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getMyRequests(Long employeeId) {
        return requestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }
}