package com.tamaka.tourism.dto;

import lombok.*;

public record RegisterAccreditationRequest(
    String accreditationText,
    String accreditationDocumentUrl
) {}