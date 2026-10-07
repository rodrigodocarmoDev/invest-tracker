package net.weg.capture.entity.project.projectpackage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.weg.capture.entity.AuditableEntity;
import net.weg.capture.entity.Identifiable;
import net.weg.capture.entity.project.Project;
import net.weg.capture.entity.project.ProjectChild;
import net.weg.capture.entity.project.ProjectFollowup;
import net.weg.capture.entity.selectoptions.project.Contractor;
import net.weg.capture.entity.selectoptions.project.EndUser;
import net.weg.capture.entity.selectoptions.project.projectpackage.PackageStatus;
import net.weg.capture.handler.interceptor.AuditInterceptor;
import net.weg.capture.mapper.serializer.IdItemSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditInterceptor.class)
@Table(name = "project_packages", uniqueConstraints = @UniqueConstraint(columnNames = { "name",
        "project_id" }), indexes = { @Index(name = "idx_project_package_project", columnList = "project_id"),
                @Index(name = "idx_project_package_contractor", columnList = "contractor_id"),
                @Index(name = "idx_project_package_end_user", columnList = "end_user_id"),
                @Index(name = "idx_project_package_package_status", columnList = "package_status_id") })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectPackage extends AuditableEntity<ProjectPackage> implements ProjectChild, Identifiable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_packages_seq")
    @SequenceGenerator(name = "project_packages_seq", sequenceName = "project_packages_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name")
    private String name;

    private String currency;
    private BigDecimal estimatedCapexAmount;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate estimatedDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "project_packages_project"))
    @JsonSerialize(using = IdItemSerializer.class)
    private Project project;

    // Os três campos abaixo (contractor, endUser e purchaser) referem-se a uma única informação:
    // a empresa envolvida no projeto. O campo contractor representa a empresa contratada para executar o projeto,
    // o campo endUser representa a empresa que está contratando a empresa contratada, e o campo company representa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", foreignKey = @ForeignKey(name = "idx_project_packages_contractor"))
    @JsonSerialize(using = IdItemSerializer.class)
    private Contractor contractor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_user_id", foreignKey = @ForeignKey(name = "idx_project_packages_end_user"))
    @JsonSerialize(using = IdItemSerializer.class)
    private EndUser endUser;

    private String purchaser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_status_id", foreignKey = @ForeignKey(name = "idx_project_packages_package_status"))
    @JsonSerialize(using = IdItemSerializer.class)
    private PackageStatus packageStatus;

    private Boolean qualified;

    @Column(length = 64)
    private String statusNote;

    @Column(precision = 5)
    @DecimalMin(value = "0.0", message = "Probability must be at least 0")
    @DecimalMax(value = "100.0", message = "Probability must be at most 100")
    private Float probability;

    @ManyToMany(mappedBy = "mentionedPackages", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ProjectFollowup> mentionedInFollowups = new LinkedHashSet<>();
}
