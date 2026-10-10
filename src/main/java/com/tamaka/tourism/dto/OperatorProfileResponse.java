package com.tamaka.tourism.dto;

import lombok.*;

public record OperatorProfileResponse(
    Long id,
    String businessName,
    String businessNit,
    String businessAddress,
    String businessPhone,
    String accreditationText,
    String accreditationDocumentUrl,
    Boolean isApproved
) {}