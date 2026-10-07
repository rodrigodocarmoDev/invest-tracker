package net.weg.capture.service.project.projectpackage;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ForbiddenException;
import net.weg.capture.constant.errors.project.PackageErrors;
import net.weg.capture.constant.errors.project.ProjectErrors;
import net.weg.capture.constant.errors.selectoptions.PackageStatusErrors;
import net.weg.capture.constant.errors.selectoptions.project.ContractorErrors;
import net.weg.capture.constant.errors.selectoptions.project.EndUserErrors;
import net.weg.capture.dto.counter.CounterDTO;
import net.weg.capture.dto.project.form.PackageForm;
import net.weg.capture.dto.project.projectpackage.MinifiedPackageDTO;
import net.weg.capture.dto.project.projectpackage.PackageDTO;
import net.weg.capture.dto.project.projectpackage.PackageSummaryDTO;
import net.weg.capture.dto.project.projectpackage.ProjectPackageSimpleDTO;
import net.weg.capture.entity.project.Project;
import net.weg.capture.entity.project.projectpackage.ProjectPackage;
import net.weg.capture.entity.quotation.QuotationStatusEnum;
import net.weg.capture.entity.selectoptions.project.projectpackage.PackageStatus;
import net.weg.capture.entity.selectoptions.project.projectpackage.PackageStatusRole;
import net.weg.capture.mapper.project.PackageModelMapper;
import net.weg.capture.repository.project.ProjectRepository;
import net.weg.capture.repository.project.projectpackage.ProjectPackageRepository;
import net.weg.capture.repository.selectoptions.project.ContractorRepository;
import net.weg.capture.repository.selectoptions.project.EndUserRepository;
import net.weg.capture.repository.selectoptions.project.PackageStatusRepository;
import net.weg.capture.service.businessunit.FactoryService;
import net.weg.capture.service.cache.strategy.project.projectpackage.ProjectPackageCacheService;
import net.weg.capture.service.privilege.UserPrivilegeService;
import net.weg.capture.service.project.ProjectChildService;
import net.weg.capture.utils.CollectionUtils;
import net.weg.capture.utils.FiscalDateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import net.weg.capture.dto.kafka.CaptureKafkaEventDTO;
import net.weg.capture.dto.kafka.KafkaEventAction;
import net.weg.capture.dto.kafka.PackageKafkaDTO;
import net.weg.capture.mapper.CaptureKafkaMapper;
import net.weg.capture.producer.CaptureKafkaProducer;
import org.jboss.logging.Logger;
import software.amazon.awssdk.utils.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectPackageService extends ProjectChildService<ProjectPackage> {
    private static final Logger LOGGER = Logger.getLogger(FactoryService.class);

    @Inject
    ProjectPackageRepository repository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    PackageModelMapper packageModelMapper;

    @Inject
    PackageStatusRepository packageStatusRepository;

    @Inject
    ContractorRepository contractorRepository;

    @Inject
    EndUserRepository endUserRepository;

    @Inject
    ProjectPackageCacheService cacheService;

    @Inject
    ProjectPackageRelevantBusinessUnitService projectPackageRelevantBusinessUnitService;

    @Inject
    UserPrivilegeService userPrivilegeService;

    @Inject
    CaptureKafkaProducer captureKafkaProducer;

    @Inject
    CaptureKafkaMapper captureKafkaMapper;

    private void sendPackageKafkaEvent(ProjectPackage pkg, KafkaEventAction action) {
        try {
            CaptureKafkaEventDTO<PackageKafkaDTO> event = captureKafkaMapper.mapToPackageKafkaEventDTO(pkg, action);
            String projectId = pkg.getProject() != null ? pkg.getProject().getId() : null;
            captureKafkaProducer.sendPackageEvent(projectId, event);
        } catch (Exception e) {
            LOGGER.error("Error preparing Kafka event for package ID=" + pkg.getId(), e);
        }
    }

    @Override
    public PanacheRepositoryBase<ProjectPackage, Long> getRepository() {
        return repository;
    }

    @Override
    public String getEntityName() {
        return "Project Package";
    }

    public void validatePackageForm(PackageForm form) {
        // Only mandatory field for package is the name
        if (form.getName() == null || form.getName().isEmpty()) {
            throw new ConstraintViolationException(PackageErrors.PACKAGE_NAME_REQUIRED, null);
        }
    }

    public void handleProjectPackageFields(PackageForm form, ProjectPackage aProjectPackage) {
        // packageStatusId is never null because it is validated in the validatePackageForm method
        PackageStatus packageStatus;
        if (form.getPackageStatusId() == null) {
            packageStatus = packageStatusRepository.getOpenStatus();
        } else {
            packageStatus = packageStatusRepository.findByIdOptional(form.getPackageStatusId())
                    .orElseThrow(() -> new EntityNotFoundException(PackageStatusErrors.PACKAGE_STATUS_NOT_FOUND));
        }

        aProjectPackage.setPackageStatus(packageStatus);

        if (form.getContractorId() != null) {
            aProjectPackage.setContractor(contractorRepository.findByIdOptional(form.getContractorId())
                    .orElseThrow(() -> new EntityNotFoundException(ContractorErrors.CONTRACTOR_NOT_FOUND)));
            aProjectPackage.setEndUser(null);
            aProjectPackage.setPurchaser(null);
        } else if (form.getEndUserId() != null) {
            aProjectPackage.setEndUser(endUserRepository.findByIdOptional(form.getEndUserId())
                    .orElseThrow(() -> new EntityNotFoundException(EndUserErrors.END_USER_NOT_FOUND)));
            aProjectPackage.setContractor(null);
            aProjectPackage.setPurchaser(null);
        } else {
            aProjectPackage.setPurchaser(form.getPurchaser());
            aProjectPackage.setContractor(null);
            aProjectPackage.setEndUser(null);
        }

        aProjectPackage.setName(form.getName().trim());

        if (StringUtils.isNotBlank(form.getEstimatedValue())) {
            aProjectPackage.setEstimatedCapexAmount(BigDecimal.valueOf(Double.parseDouble(form.getEstimatedValue())));
        }
        if (form.getYear() != null && form.getQuarter() != null) {
            aProjectPackage
                    .setEstimatedDate(FiscalDateUtils.getDateFromYearAndQuarter(form.getYear(), form.getQuarter()));
        }
        aProjectPackage.setDescription(form.getDescription());
        aProjectPackage.setQualified(form.getQualified());
        aProjectPackage.setStatusNote(form.getStatusNote());

        if (form.getProbability() != null) {
            aProjectPackage.setProbability(form.getProbability());
        }
    }

    @WithSpan
    @Transactional
    public PackageDTO create(String projectId, PackageForm form) {
        if (userPrivilegeService.isNothingButUser()) {
            throw new ForbiddenException(PackageErrors.PACKAGE_FORBIDDEN);
        }
        ProjectPackage aProjectPackage = new ProjectPackage();

        Project project = projectRepository.findByIdOptional(projectId).orElseThrow(EntityNotFoundException::new);

        validatePackageForm(form);

        aProjectPackage.setProject(project);
        aProjectPackage.setCurrency("USD");

        handleProjectPackageFields(form, aProjectPackage);

        ProjectPackage persistedProjectPackage = persist(aProjectPackage);
        cacheService.invalidateListAll();

        sendPackageKafkaEvent(persistedProjectPackage, KafkaEventAction.CREATED);

        return packageModelMapper.mapPackageToDTO(persistedProjectPackage);
    }

    @WithSpan
    @Transactional
    public List<PackageDTO> createInBatch(String projectId, List<PackageForm> packageForms) {
        if (userPrivilegeService.isNothingButUser()) {
            throw new ForbiddenException(PackageErrors.PACKAGE_FORBIDDEN);
        }
        Project project = projectRepository.findByIdOptional(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ProjectErrors.PROJECT_NOT_FOUND));

        if (packageForms == null || packageForms.isEmpty()) {
            return Collections.emptyList();
        }
        List<ProjectPackage> existingPackages = repository.findAllByProjectId(projectId);

        Set<String> existingNames = existingPackages.stream().map(ProjectPackage::getName).filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        Map<String, PackageForm> nameToFormMap = new HashMap<>();
        List<PackageForm> validForms = packageForms.stream().filter(Objects::nonNull).filter(form -> {
            String name = form.getName();
            return name != null && !existingNames.contains(name.trim().toLowerCase(Locale.ROOT));
        }).filter(form -> {
            String name = form.getName().trim().toLowerCase(Locale.ROOT);
            return nameToFormMap.putIfAbsent(name, form) == null;
        }).toList();

        if (validForms.isEmpty()) {
            return Collections.emptyList();
        }

        validForms.forEach(this::validatePackageForm);

        List<ProjectPackage> validPackages = validForms.stream().map(form -> {
            ProjectPackage pkg = new ProjectPackage();
            pkg.setProject(project);
            pkg.setCurrency("USD");
            handleProjectPackageFields(form, pkg);
            return pkg;
        }).collect(Collectors.toList());

        repository.persist(validPackages);
        cacheService.invalidateListAll();

        validPackages.forEach(pkg -> sendPackageKafkaEvent(pkg, KafkaEventAction.CREATED));

        return packageModelMapper.mapPackagesToDTOs(validPackages);
    }

    @WithSpan
    public PackageDTO findById(Long id) {
        ProjectPackage aProjectPackage = repository.findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException(PackageErrors.PACKAGE_NOT_FOUND));
        return packageModelMapper.mapPackageToDTO(aProjectPackage);
    }

    @WithSpan
    @Transactional
    public PackageDTO update(Long id, PackageForm form) {
        if (userPrivilegeService.isNothingButUser()) {
            throw new ForbiddenException(PackageErrors.PACKAGE_FORBIDDEN);
        }
        ProjectPackage projectPackage = repository.findById(id);
        if (projectPackage == null) {
            throw new EntityNotFoundException(PackageErrors.PACKAGE_NOT_FOUND);
        }

        validatePackageForm(form);

        handleProjectPackageFields(form, projectPackage);

        ProjectPackage persistedProjectPackage = persist(projectPackage);
        cacheService.invalidateById(id);

        sendPackageKafkaEvent(persistedProjectPackage, KafkaEventAction.UPDATED);

        return packageModelMapper.mapPackageToDTO(persistedProjectPackage);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (userPrivilegeService.isNothingButUser()) {
            throw new ForbiddenException(PackageErrors.PACKAGE_FORBIDDEN);
        }
        ProjectPackage projectPackage = repository.findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException(PackageErrors.PACKAGE_NOT_FOUND));

        projectPackageRelevantBusinessUnitService.deleteAllByPackageId(id);
        super.delete(id);
        cacheService.invalidateById(id);

        sendPackageKafkaEvent(projectPackage, KafkaEventAction.DELETED);
    }

    @WithSpan
    public List<MinifiedPackageDTO> listAll() {
        List<MinifiedPackageDTO> packagesCache = cacheService.listAll();
        if (CollectionUtils.isNotEmpty(packagesCache)) {
            return packagesCache;
        }
        return listAllFromDataBase();
    }

    @WithSpan
    public List<MinifiedPackageDTO> listAllFromDataBase() {
        List<MinifiedPackageDTO> packages = repository.listAllMinified();
        if (!packages.isEmpty()) {
            cacheService.cacheAll(packages);
        }

        return packages;
    }

    @WithSpan
    public Optional<MinifiedPackageDTO> findMinifiedById(Long packageId) {
        return listAll().stream().filter(p -> p.getId().equals(packageId)).findFirst();
    }

    @WithSpan
    public List<MinifiedPackageDTO> listAllPackagesQualifiedOrWithAnyQuotation() {
        List<MinifiedPackageDTO> packagesCache = cacheService.listAllPackagesQualifiedOrWithAnyQuotation();
        if (CollectionUtils.isNotEmpty(packagesCache)) {
            return packagesCache;
        }
        return listAllPackagesQualifiedOrWithAnyQuotationFromDataBase();
    }

    public List<MinifiedPackageDTO> listAllPackagesQualifiedOrWithAnyQuotationFromDataBase() {
        List<MinifiedPackageDTO> packages = repository.listPackagesQualifiedOrWithAnyQuotation();
        if (!packages.isEmpty()) {
            cacheService.cacheAllPackagesQualifiedOrWithAnyQuotation(packages);
        }
        return packages;
    }

    @WithSpan
    public List<MinifiedPackageDTO> listAllPackagesWithAnyQuotation() {
        List<MinifiedPackageDTO> packagesCache = cacheService.listAllPackagesWithAnyQuotation();
        if (CollectionUtils.isNotEmpty(packagesCache)) {
            return packagesCache;
        }
        return listAllPackagesWithAnyQuotationFromDataBase();
    }

    public List<MinifiedPackageDTO> listAllPackagesWithAnyQuotationFromDataBase() {
        List<MinifiedPackageDTO> packages = repository.listPackagesWithAnyQuotation();
        if (!packages.isEmpty()) {
            cacheService.cacheAllPackagesWithAnyQuotation(packages);
        }
        return packages;
    }

    public List<PackageDTO> findAllByProjectId(String projectId) {
        List<ProjectPackage> projectPackages = repository.findAllByProjectId(projectId);
        return projectPackages.stream().map(p -> packageModelMapper.mapPackageToDTO(p)).toList();
    }

    public List<PackageDTO> findAllByProjectIdWithQuoteCount(String projectId) {
        List<ProjectPackage> projectPackages = repository.findAllByProjectId(projectId);
        return packageModelMapper.mapAllPackageToDtoWithQuoteCount(projectPackages);
    }

    @Transactional
    public void updatePackageStatus(ProjectPackage projectPackage, QuotationStatusEnum quotationStatusEnum) {
        if (userPrivilegeService.isNothingButUser()) {
            throw new ForbiddenException(PackageErrors.PACKAGE_FORBIDDEN);
        }
        PackageStatusRole role = quotationStatusEnum.equals(QuotationStatusEnum.WEG_WON) ? PackageStatusRole.BLUE
                : PackageStatusRole.RED;
        Optional<PackageStatus> status = packageStatusRepository.findByRoleOptional(role);
        if (status.isEmpty()) {
            throw new EntityNotFoundException(PackageStatusErrors.PACKAGE_STATUS_NOT_FOUND);
        }
        projectPackage.setPackageStatus(status.get());
        projectPackage.setEstimatedDate(LocalDate.now());
        persist(projectPackage);
        cacheService.invalidateById(projectPackage.getId());

        sendPackageKafkaEvent(projectPackage, KafkaEventAction.UPDATED);
    }

    @WithSpan
    public List<CounterDTO> countRelatedQuotes(List<Long> packageIds) {
        return repository.countRelatedQuotes(packageIds);
    }

    public List<PackageSummaryDTO> findAllSummaryByProjectId(String projectId) {
        List<ProjectPackage> projectPackages = repository.findAllByProjectId(projectId);
        return packageModelMapper.mapAllPackagesToPackageSummaryDTO(projectPackages);
    }

    public List<ProjectPackageSimpleDTO> findSimpleSummaryByProjectId(String projectId, String filter) {
        return repository.findSimpleSummaryByProjectIdAndNameFilter(projectId, filter);
    }
}
