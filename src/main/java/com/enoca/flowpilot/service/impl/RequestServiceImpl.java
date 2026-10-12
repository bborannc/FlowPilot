package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.RequestDetail;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.core.enums.RequestPriority;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.core.exceptions.BusinessRuleException;
import com.enoca.flowpilot.core.exceptions.ResourceNotFoundException;
import com.enoca.flowpilot.dto.request.CreateRequestDto;
import com.enoca.flowpilot.dto.response.RequestResponseDto;
import com.enoca.flowpilot.mapper.RequestMapper;
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public RequestResponseDto createDraft(CreateRequestDto dto) {

        // Giriş yapan kullanıcı JWT üzerinden bulunur.
        Long currentEmployeeId = currentUserService.getCurrentEmployeeId();

        // Talebin sahibi istemciden değil, doğrulanmış kullanıcıdan alınır.
        Employee employee = employeeService.getById(currentEmployeeId);

        Request request = Request.builder()
                .employee(employee)
                .requestType(dto.getRequestType())
                .priority(dto.getPriority() != null
                        ? dto.getPriority()
                        : RequestPriority.MEDIUM)
                .status(RequestStatus.DRAFT)
                .details(new ArrayList<>())
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

        approvalService.recordHistory(
                savedRequest,
                employee.getId(),
                ApprovalAction.CREATED,
                "Talep taslak (DRAFT) olarak oluşturuldu."
        );

        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public RequestResponseDto submitRequest(Long requestId) {

        // 1. Giriş yapan kullanıcıyı JWT üzerinden belirle.
        Long currentEmployeeId = currentUserService.getCurrentEmployeeId();

        // 2. Talebi bul.
        Request request = requestRepository.findByIdWithDetailsAndSteps(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Talep bulunamadı: " + requestId
                        )
                );

        // 3. Talebin sahibi ile giriş yapan kullanıcıyı karşılaştır.
        if (!request.getEmployee().getId().equals(currentEmployeeId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Bu talebi onaya sunma yetkiniz yok."
            );
        }

        // 4. Yalnızca DRAFT talepler onaya sunulabilir.
        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Yalnızca taslak (DRAFT) durumundaki talepler onaya sunulabilir."
            );
        }

        // 5. Talep türüne ait onay politikasını bul.
        ApprovalPolicy policy = policyRegistry.getPolicy(request.getRequestType());

        if (policy == null) {
            throw new BusinessRuleException(
                    "Belirtilen talep türü için tanımlı bir onay politikası bulunamadı: "
                            + request.getRequestType()
            );
        }

        // 6. Talep detaylarını doğrula ve onay adımlarını oluştur.
        policy.validateDetails(request);
        policy.applyPolicy(request);

        request.setStatus(RequestStatus.IN_APPROVAL);

        Request savedRequest = requestRepository.save(request);

        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponseDto getRequestDetails(Long requestId) {

        // Giriş yapan kullanıcının ID'si JWT üzerinden alınır.
        Long currentEmployeeId = currentUserService.getCurrentEmployeeId();

        // Talep bulunamazsa mevcut 404 mekanizması çalışır.
        Request request = requestRepository.findByIdWithDetailsAndSteps(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Talep bulunamadı: " + requestId
                        )
                );

        // Talep sahibinin ID'si ile giriş yapan kullanıcının ID'si karşılaştırılır.
        if (!request.getEmployee().getId().equals(currentEmployeeId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Bu talebi görüntüleme yetkiniz yok."
            );
        }

        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getMyRequests() {

        Long employeeId = currentUserService.getCurrentEmployeeId();

        List<Request> requests =
                requestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);

        return requests.stream()
                .map(RequestMapper::toDto)
                .toList();
    }
}