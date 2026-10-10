package com.tamaka.tourism.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operator_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_nit", nullable = false, unique = true)
    private String businessNit;

    @Column(name = "business_address")
    private String businessAddress;

    @Column(name = "business_phone")
    private String businessPhone;

    @Column(name = "accreditation_text", columnDefinition = "TEXT")
    private String accreditationText;

    @Column(name = "accreditation_document_url")
    private String accreditationDocumentUrl;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;
}