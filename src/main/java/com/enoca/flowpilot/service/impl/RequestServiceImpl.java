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
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.EmployeeService;
import com.enoca.flowpilot.service.RequestService;
import com.enoca.flowpilot.service.policy.ApprovalPolicy;
import com.enoca.flowpilot.service.policy.ApprovalPolicyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

        // 1. Sadece DRAFT olarak kaydet
        Request request = Request.builder()
                .employee(employee)
                .requestType(dto.getRequestType())
                .priority(dto.getPriority() != null ? dto.getPriority() : RequestPriority.MEDIUM)
                .status(RequestStatus.DRAFT)
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

        // 3. Draft oluşturulduğunda sadece tarihçe kaydı at
        approvalService.recordHistory(savedRequest, employee.getId(), ApprovalAction.CREATED, "Talep taslak (DRAFT) olarak oluşturuldu.");

        return mapToDto(savedRequest);
    }

    @Override
    @Transactional
    public RequestResponseDto submitRequest(Long requestId) {
        Request request = requestRepository.findByIdWithDetailsAndSteps(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Yalnızca DRAFT durumundaki talepler onay sürecine sunulabilir.");
        }

        // 1. Statüyü IN_APPROVAL yap
        request.setStatus(RequestStatus.IN_APPROVAL);

        // 2. Registry üzerinden policy'yi bul ve adımları üret
        ApprovalPolicy policy = policyRegistry.getPolicy(request.getRequestType());
        policy.generateApprovalSteps(request);

        // 3. Tarihçe kaydı
        approvalService.recordHistory(request, request.getEmployee().getId(), ApprovalAction.CREATED, "Talep onay sürecine sunuldu.");

        return mapToDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponseDto getRequestDetails(Long requestId) {
        Request request = requestRepository.findByIdWithDetailsAndSteps(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));
        return mapToDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getMyRequests(Long employeeId) {
        return requestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private RequestResponseDto mapToDto(Request request) {
        Map<String, String> detailMap = request.getDetails() != null
                ? request.getDetails().stream().collect(Collectors.toMap(RequestDetail::getKey, RequestDetail::getValue, (v1, v2) -> v1))
                : Map.of();

        return RequestResponseDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getName())
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .priority(request.getPriority())
                .createdAt(request.getCreatedAt())
                .details(detailMap)
                .build();
    }
}