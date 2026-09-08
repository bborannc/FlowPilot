package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.core.enums.RequestType;
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
}