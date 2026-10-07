package net.weg.capture.dto.project.projectpackage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.weg.capture.dto.audit.AuditableEntityDTO;
import net.weg.capture.dto.project.QuarterDateDTO;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageDTO extends AuditableEntityDTO implements Serializable {

    private Long id;
    private String name;
    private String currency;
    private BigDecimal estimatedCapexAmount;
    private String status;
    private QuarterDateDTO estimatedDate;
    private String description;
    private String projectId;
    private Long relatedQuotes;

    private String purchaser;
    private String statusNote;
    private Boolean qualified;

    private Long contractorId;
    private Long endUserId;

    private PackageStatusDTO packageStatusDTO;

    private Float probability;
}
