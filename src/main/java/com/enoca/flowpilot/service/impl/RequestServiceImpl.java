package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.dto.request.CreateRequestDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;
import com.enoca.flowpilot.exception.ResourceNotFoundException;
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.EmployeeService;
import com.enoca.flowpilot.service.RequestService;
import com.enoca.flowpilot.service.policy.ApprovalPolicy;
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
    private final List<ApprovalPolicy> approvalPolicies; // Spring tüm kuralları buraya otomatik enjekte eder (IoC)

    @Override
    @Transactional
    public Request createAndSubmitRequest(Long employeeId, RequestType type, RequestPriority priority, Map<String, String> details) {
        Employee employee = employeeService.getById(employeeId);

        // 1. Talebi PENDING statüsünde oluştur
        Request request = Request.builder()
                .employee(employee)
                .requestType(type)
                .priority(priority)
                .status(RequestStatus.PENDING)
                .build();

        // 2. Dinamik detayları bağla
        if (details != null && !details.isEmpty()) {
            details.forEach((key, value) -> {
                RequestDetail detail = RequestDetail.builder()
                        .request(request)
                        .key(key)
                        .value(value)
                        .build();
                request.getDetails().add(detail);
            });
        }

        Request savedRequest = requestRepository.save(request);

        // 3. İlgili kural motorunu (Policy) bul ve onay adımlarını dinamik üret
        ApprovalPolicy policy = approvalPolicies.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Desteklenmeyen talep tipi: " + type));

        policy.generateApprovalSteps(savedRequest);

        // 4. Tarihçeye talep oluşturma logunu ekle
        approvalService.recordHistory(savedRequest, employeeId, ApprovalAction.CREATED, "Talep oluşturuldu ve onaya sunuldu.");

        return savedRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public Request getRequestById(Long id) {
        return requestRepository.findByIdWithDetailsAndSteps(id)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Request> getRequestsByEmployee(Long employeeId) {
        return requestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Override
    @Transactional
    public RequestResponseDto createAndSubmit(CreateRequestDto dto) {
        Employee employee = employeeService.getById(dto.getEmployeeId());

        Request request = Request.builder()
                .employee(employee)
                .requestType(dto.getRequestType())
                .priority(dto.getPriority() != null ? dto.getPriority() : RequestPriority.MEDIUM)
                .status(RequestStatus.PENDING)
                .build();

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

        // Kural motorunu bul ve adımları oluştur (Strategy Pattern)
        ApprovalPolicy policy = approvalPolicies.stream()
                .filter(p -> p.supports(savedRequest.getRequestType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Desteklenmeyen talep tipi: " + savedRequest.getRequestType()));

        policy.generateApprovalSteps(savedRequest);

        // Aksiyon tarihçesini kaydet
        approvalService.recordHistory(savedRequest, employee.getId(), ApprovalAction.CREATED, "Talep oluşturuldu ve onay akışı başlatıldı.");

        return mapToDto(savedRequest);
    }

    @Override
    @Transactional
    public RequestResponseDto submitRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + requestId));

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Sadece taslak (DRAFT) durumundaki talepler submit edilebilir.");
        }

        request.setStatus(RequestStatus.PENDING);

        ApprovalPolicy policy = approvalPolicies.stream()
                .filter(p -> p.supports(request.getRequestType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Desteklenmeyen talep tipi: " + request.getRequestType()));

        policy.generateApprovalSteps(request);

        approvalService.recordHistory(request, request.getEmployee().getId(), ApprovalAction.CREATED, "Taslak talep onay akışına sunuldu.");

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

    // Entity -> DTO Dönüştürücü (Mapping)
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