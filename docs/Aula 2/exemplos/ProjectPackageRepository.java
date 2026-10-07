package net.weg.capture.repository.project.projectpackage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import net.weg.capture.dto.counter.CounterDTO;
import net.weg.capture.dto.project.projectpackage.MinifiedPackageDTO;
import net.weg.capture.dto.project.projectpackage.ProjectPackageSimpleDTO;
import net.weg.capture.dto.report.eop.EOPPackageDetailsRowDTO;
import net.weg.capture.dto.report.eop.EOPPackageTooltipRowDTO;
import net.weg.capture.entity.common.repository.Param;
import net.weg.capture.entity.project.projectpackage.ProjectPackage;
import net.weg.capture.repository.project.ProjectChildRepository;
import net.weg.capture.utils.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ProjectPackageRepository implements ProjectChildRepository<ProjectPackage, Long> {

    // Panache já dá isso; mantém assinatura
    @Override
    public List<ProjectPackage> findAllByProjectId(String projectId) {
        return list("project.id", projectId);
    }

    public List<EOPPackageTooltipRowDTO> findEopTooltipPackages(String projectId) {
        String hql = "SELECT NEW net.weg.capture.dto.report.eop.EOPPackageTooltipRowDTO("
                + "pp.id, pp.name, pp.estimatedCapexAmount, pp.currency) " + "FROM ProjectPackage pp "
                + "WHERE pp.project.id = :projectId " + "ORDER BY pp.estimatedDate, pp.name";

        return getEntityManager().createQuery(hql, EOPPackageTooltipRowDTO.class).setParameter("projectId", projectId)
                .getResultList();
    }

    public List<EOPPackageDetailsRowDTO> findEopDetailsPackages(String projectId) {
        String hql = "SELECT NEW net.weg.capture.dto.report.eop.EOPPackageDetailsRowDTO("
                + "pp.id, pp.name, pp.estimatedCapexAmount, pp.currency, pp.qualified, st.text, "
                + "(SELECT COUNT(q) FROM Quotation q WHERE q.projectPackage = pp)) " + "FROM ProjectPackage pp "
                + "LEFT JOIN pp.packageStatus st " + "WHERE pp.project.id = :projectId "
                + "ORDER BY pp.estimatedDate, pp.name";

        return getEntityManager().createQuery(hql, EOPPackageDetailsRowDTO.class).setParameter("projectId", projectId)
                .getResultList();
    }

    @Override
    public void deleteAllByProjectId(String projectId) {
        List<ProjectPackage> packagesToDelete = list("project.id", projectId);
        for (ProjectPackage p : packagesToDelete) {
            delete(p);
        }
    }

    /** JPQL em vez de native SQL */
    public Long sumPackagesCapex() {
        // assumindo campo BigDecimal em ProjectPackage: estimatedCapexAmount
        BigDecimal sum = getEntityManager()
                .createQuery("select coalesce(sum(p.estimatedCapexAmount), 0) from ProjectPackage p", BigDecimal.class)
                .getSingleResult();
        return sum.longValue();
    }

    /** Pacotes que possuem ao menos 1 cotação */
    public List<MinifiedPackageDTO> listPackagesWithAnyQuotation() {
        String jpql = baseSelect() + " where exists (select 1 from Quotation q where q.projectPackage.id = p.id) "
                + " order by p.updatedAt desc";
        return queryDTO(jpql);
    }

    /** Pacote dono de uma quotation específica */
    public MinifiedPackageDTO findPackageByQuotationId(Long quotationId) {
        String jpql = baseSelect()
                + " where exists (select 1 from Quotation q where q.id = :qid and q.projectPackage.id = p.id)";
        List<MinifiedPackageDTO> r = queryDTO(jpql, Param.of("qid", quotationId));
        return r.isEmpty() ? null : r.getFirst();
    }

    /** Pacote por ID (minificado) */
    public MinifiedPackageDTO findPackageById(Long packageId) {
        String jpql = baseSelect() + " where p.id = :pid";
        List<MinifiedPackageDTO> r = queryDTO(jpql, Param.of("pid", packageId));
        return r.isEmpty() ? null : r.getFirst();
    }

    /** Todos os pacotes minificados */
    public List<MinifiedPackageDTO> listAllMinified() {
        String jpql = baseSelect() + " order by p.updatedAt desc";
        return queryDTO(jpql);
    }

    /** Contagem de cotações por pacote (já era JPQL) */
    public List<CounterDTO> countRelatedQuotes(List<Long> packageIds) {
        if (CollectionUtils.isEmpty(packageIds)) {
            return Collections.emptyList();
        }
        String jpql = "select new net.weg.capture.dto.counter.CounterDTO(q.projectPackage.id, count(q.id)) "
                + "from Quotation q " + "where q.projectPackage.id in :ids " + "group by q.projectPackage.id";
        return getEntityManager().createQuery(jpql, CounterDTO.class).setParameter("ids", packageIds).getResultList();
    }

    /** Pacotes qualificados OU com qualquer cotação */
    public List<MinifiedPackageDTO> listPackagesQualifiedOrWithAnyQuotation() {
        String jpql = baseSelect()
                + " where (p.qualified = true or exists (select 1 from Quotation q where q.projectPackage.id = p.id)) "
                + " order by p.updatedAt desc";
        return queryDTO(jpql);
    }

    public List<ProjectPackageSimpleDTO> findSimpleSummaryByProjectIdAndNameFilter(String projectId, String filter) {
        StringBuilder jpql = new StringBuilder(
                "select new net.weg.capture.dto.project.projectpackage.ProjectPackageSimpleDTO("
                        + " p.id, p.name, st.text, coalesce(eu.text, c.text, p.purchaser)) " + "from ProjectPackage p "
                        + "left join p.packageStatus st " + "left join p.endUser eu " + "left join p.contractor c "
                        + "where p.project.id = :projectId");

        TypedQuery<ProjectPackageSimpleDTO> query;
        if (filter != null && !filter.isBlank()) {
            jpql.append(" and lower(p.name) like :filter");
            jpql.append(" order by p.name asc");
            query = getEntityManager().createQuery(jpql.toString(), ProjectPackageSimpleDTO.class)
                    .setParameter("projectId", projectId)
                    .setParameter("filter", "%" + filter.trim().toLowerCase(Locale.ROOT) + "%");
        } else {
            jpql.append(" order by p.name asc");
            query = getEntityManager().createQuery(jpql.toString(), ProjectPackageSimpleDTO.class)
                    .setParameter("projectId", projectId);
        }

        return query.getResultList();
    }

    // ---------- Helpers ----------

    /**
     * Usa constructor expression em JPQL para projetar direto no DTO. Ajuste nomes dos campos conforme suas entidades:
     * - ProjectPackage: id, name, currency, estimatedCapexAmount, estimatedDate, project.id, statusNote, qualified,
     * purchaser, createdAt, updatedAt, createdBy, updatedBy - Relacionamentos: p.packageStatus st (text, role),
     * p.endUser eu (text), p.contractor c (text)
     */
    private String baseSelect() {
        return "select new net.weg.capture.dto.project.projectpackage.MinifiedPackageDTO("
                + " p.id, p.name, p.currency, p.estimatedCapexAmount, p.estimatedDate, p.project.id,"
                + " p.statusNote, p.qualified, p.purchaser," + " eu.text, c.text, st.text, st.role,"
                + " p.createdAt, p.updatedAt, p.createdBy, p.updatedBy, p.probability, p.description " + ") "
                + "from ProjectPackage p " + "left join p.packageStatus st " + "left join p.endUser eu "
                + "left join p.contractor c ";
    }

    public List<MinifiedPackageDTO> queryDTO(String jpql, Param... params) {
        TypedQuery<MinifiedPackageDTO> q = getEntityManager().createQuery(jpql, MinifiedPackageDTO.class);
        if (params != null) {
            for (Param p : params)
                q.setParameter(p.name(), p.value());
        }
        return q.getResultList();
    }

    public List<MinifiedPackageDTO> findAllByIds(List<Long> packageIds) {
        if (CollectionUtils.isEmpty(packageIds)) {
            return Collections.emptyList();
        }
        String jpql = baseSelect() + " where p.id in :ids";
        return queryDTO(jpql, Param.of("ids", packageIds));
    }

}