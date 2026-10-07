package net.weg.capture.mapper.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.weg.capture.dto.counter.CounterDTO;
import net.weg.capture.dto.project.QuarterDateDTO;
import net.weg.capture.dto.project.projectpackage.PackageDTO;
import net.weg.capture.dto.project.projectpackage.PackageStatusDTO;
import net.weg.capture.dto.project.projectpackage.PackageSummaryDTO;
import net.weg.capture.entity.project.projectpackage.ProjectPackage;
import net.weg.capture.service.project.projectpackage.ProjectPackageService;
import net.weg.capture.utils.CollectionUtils;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class PackageModelMapper {

    @Inject
    ProjectPackageService projectPackageService;

    private final ModelMapper modelMapper = new ModelMapper();

    public PackageDTO mapPackageToDTO(ProjectPackage aProjectPackage) {
        PackageDTO response = modelMapper.map(aProjectPackage, PackageDTO.class);
        if (aProjectPackage.getEstimatedDate() != null) {
            response.setEstimatedDate(new QuarterDateDTO(aProjectPackage.getEstimatedDate()));
        }

        if (aProjectPackage.getPackageStatus() != null) {
            response.setPackageStatusDTO(modelMapper.map(aProjectPackage.getPackageStatus(), PackageStatusDTO.class));
        }

        if (aProjectPackage.getContractor() != null) {
            response.setContractorId(aProjectPackage.getContractor().getId());
            response.setPurchaser(aProjectPackage.getContractor().getText());
        } else if (aProjectPackage.getEndUser() != null) {
            response.setEndUserId(aProjectPackage.getEndUser().getId());
            response.setPurchaser(aProjectPackage.getEndUser().getText());
        }

        return response;
    }

    public List<PackageDTO> mapAllPackageToDtoWithQuoteCount(List<ProjectPackage> projectPackages) {
        if (projectPackages == null || projectPackages.isEmpty())
            return List.of();

        List<Long> ids = projectPackages.stream().map(ProjectPackage::getId).toList();

        Map<Long, Long> countById = projectPackageService.countRelatedQuotes(ids).stream()
                .collect(Collectors.toMap(CounterDTO::getId, CounterDTO::getCount, Long::sum));

        return projectPackages.stream().map(pkg -> {
            PackageDTO dto = mapPackageToDTO(pkg);
            dto.setRelatedQuotes(countById.getOrDefault(dto.getId(), 0L));
            return dto;
        }).toList();
    }

    public List<PackageDTO> mapPackagesToDTOs(List<ProjectPackage> projectPackages) {
        return projectPackages.stream().map(this::mapPackageToDTO).toList();
    }

    public List<PackageSummaryDTO> mapAllPackagesToPackageSummaryDTO(List<ProjectPackage> projectPackages) {
        if (CollectionUtils.isEmpty(projectPackages)) {
            return Collections.emptyList();
        }

        return projectPackages.stream().map(this::convertToSummaryDTO).toList();
    }

    private PackageSummaryDTO convertToSummaryDTO(ProjectPackage pkg) {
        PackageSummaryDTO dto = modelMapper.map(pkg, PackageSummaryDTO.class);

        if (pkg.getPurchaser() != null) {
            dto.setPurchaser(pkg.getPurchaser());
        } else if (pkg.getContractor() != null) {
            dto.setPurchaser(pkg.getContractor().getText());
        } else if (pkg.getEndUser() != null) {
            dto.setPurchaser(pkg.getEndUser().getText());
        }

        return dto;
    }
}
