package com.example.staffassign.service;

import com.example.staffassign.dto.ProjectCsvDto;
import com.example.staffassign.dto.ProjectPlanCsvDto;
import com.example.staffassign.entity.ProjectMaster;
import com.example.staffassign.entity.ProjectPlan;
import com.example.staffassign.entity.ProjectStatusHistory;
import com.example.staffassign.exception.BusinessException;
import com.example.staffassign.repository.ProjectMasterMapper;
import com.example.staffassign.repository.ProjectPlanMapper;
import com.example.staffassign.repository.ProjectStatusHistoryMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectMasterMapper projectMasterMapper;
    private final ProjectPlanMapper projectPlanMapper;
    private final ProjectStatusHistoryMapper projectStatusHistoryMapper;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectMasterMapper projectMasterMapper, ProjectPlanMapper projectPlanMapper, ProjectStatusHistoryMapper projectStatusHistoryMapper, AuditLogService auditLogService) {
        this.projectMasterMapper = projectMasterMapper;
        this.projectPlanMapper = projectPlanMapper;
        this.projectStatusHistoryMapper = projectStatusHistoryMapper;
        this.auditLogService = auditLogService;
    }

    // Project Master
    public List<ProjectMaster> findAll() {
        return projectMasterMapper.findAll();
    }

    public ProjectMaster findById(Long id) {
        return projectMasterMapper.findById(id).orElse(null);
    }

    public void create(ProjectMaster project) {
        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus("進行中");
        }
        validateRequiredFieldsForStatus(project);
        projectMasterMapper.insert(project);
        if (project.getId() != null) {
            auditLogService.log("PROJECT_CREATE", "PROJECT", String.valueOf(project.getId()), project.getName());
        }
    }

    public void update(Long id, ProjectMaster project) {
        ProjectMaster existing = projectMasterMapper.findById(id).orElse(null);
        if (existing == null) {
            throw new BusinessException("対象のプロジェクトが存在しません。");
        }
        String oldStatus = existing.getStatus();
        String newStatus = project.getStatus();
        if (newStatus == null || newStatus.isBlank()) {
            newStatus = oldStatus != null ? oldStatus : "進行中";
            project.setStatus(newStatus);
        }
        if (!Objects.equals(oldStatus, newStatus)) {
            validateStatusTransition(oldStatus, newStatus);
            if (project.getStatusChangeReason() == null || project.getStatusChangeReason().isBlank()) {
                throw new BusinessException("ステータスを変更する場合、ステータス変更理由は必須です。");
            }
        }
        validateRequiredFieldsForStatus(project);
        project.setId(id);
        projectMasterMapper.update(project);
        if (!Objects.equals(oldStatus, newStatus)) {
            ProjectStatusHistory history = new ProjectStatusHistory();
            history.setProjectId(id);
            history.setOldStatus(oldStatus);
            history.setNewStatus(newStatus);
            history.setReason(project.getStatusChangeReason());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                history.setChangedBy(auth.getName());
            }
            history.setChangedAt(LocalDateTime.now());
            projectStatusHistoryMapper.insert(history);
        }
        auditLogService.log("PROJECT_UPDATE", "PROJECT", String.valueOf(id), project.getName());
    }

    public void delete(Long id) {
        projectMasterMapper.delete(id);
        auditLogService.log("PROJECT_DELETE", "PROJECT", String.valueOf(id), null);
    }

    @Transactional
    public void importCsv(Reader reader) {
        List<ProjectCsvDto> dtos = new CsvToBeanBuilder<ProjectCsvDto>(reader)
                .withType(ProjectCsvDto.class)
                .build()
                .parse();

        for (ProjectCsvDto dto : dtos) {
            ProjectMaster entity = new ProjectMaster();
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setCustomerId(dto.getCustomerId());
            entity.setEndUser(dto.getEndUser());
            entity.setStartYm(dto.getStartYm());
            entity.setEndYm(dto.getEndYm());
            entity.setParentId(dto.getParentId());
            entity.setRevenue(dto.getRevenue());
            entity.setPmId(dto.getPmId());
            entity.setPriority(dto.getPriority());
            entity.setProjectType(dto.getProjectType());
            entity.setStatus(dto.getStatus());

            if (dto.getId() != null && projectMasterMapper.findById(dto.getId()).isPresent()) {
                entity.setId(dto.getId());
                projectMasterMapper.update(entity);
            } else {
                projectMasterMapper.insert(entity);
            }
        }
    }

    public void exportCsv(Writer writer) throws Exception {
        List<ProjectMaster> list = findAll();
        List<ProjectCsvDto> dtos = list.stream().map(e -> {
            ProjectCsvDto dto = new ProjectCsvDto();
            dto.setId(e.getId());
            dto.setName(e.getName());
            dto.setDescription(e.getDescription());
            dto.setCustomerId(e.getCustomerId());
            dto.setEndUser(e.getEndUser());
            dto.setStartYm(e.getStartYm());
            dto.setEndYm(e.getEndYm());
            dto.setParentId(e.getParentId());
            dto.setRevenue(e.getRevenue());
            dto.setPmId(e.getPmId());
            dto.setPriority(e.getPriority());
            dto.setProjectType(e.getProjectType());
            dto.setStatus(e.getStatus());
            return dto;
        }).collect(Collectors.toList());

        new StatefulBeanToCsvBuilder<ProjectCsvDto>(writer)
                .build()
                .write(dtos);
    }

    // Project Plan
    public List<ProjectPlan> findPlansByProjectId(Long projectId) {
        return projectPlanMapper.findByProjectId(projectId);
    }

    @Transactional
    public void createPlan(ProjectPlan plan) {
        Integer maxEndYm = projectPlanMapper.findMaxEndYmByProjectId(plan.getProjectId());
        if (maxEndYm != null) {
            if (plan.getStartYm() <= maxEndYm) {
                throw new BusinessException("開始年月は前回の終了年月(" + maxEndYm + ")より後の日付を指定してください。");
            }
        }
        projectPlanMapper.insert(plan);
    }
    
    public void deletePlan(Long id) {
        projectPlanMapper.delete(id);
    }

    @Transactional
    public void importPlanCsv(Reader reader) {
        // CSVファイルを読み込み、DTOリストに変換
        List<ProjectPlanCsvDto> dtos = new CsvToBeanBuilder<ProjectPlanCsvDto>(reader)
                .withType(ProjectPlanCsvDto.class)
                .build()
                .parse();

        for (ProjectPlanCsvDto dto : dtos) {
            ProjectPlan entity = new ProjectPlan();
            entity.setProjectId(dto.getProjectId());
            entity.setPlanVersion(dto.getPlanVersion());
            entity.setStartYm(dto.getStartYm());
            entity.setEndYm(dto.getEndYm());
            entity.setResourceCount(dto.getResourceCount());
            entity.setProductionAmount(dto.getProductionAmount());

            // 連続性チェック: 同一プロジェクトの既存計画の終了年月より後であることを確認
            // Bulk importであってもデータの整合性を保証するため、都度チェックを行う
            Integer maxEndYm = projectPlanMapper.findMaxEndYmByProjectId(dto.getProjectId());
            if (maxEndYm != null) {
                // 自身の更新の場合は、自分自身を除外してチェックする必要があるが、
                // ここでは簡易的に「開始年月が既存の最大終了年月以下」の場合はエラーとする（新規追加または更新時の整合性）
                // ※厳密な更新ロジックはID判定等が必要だが、CSVインポートは通常新規または洗い替えを想定
                if (dto.getStartYm() <= maxEndYm) {
                    // ID指定があり、かつ既存レコードの更新である場合、
                    // そのレコード自体がmaxEndYmを持っている可能性があるため、チェックをスキップまたは詳細化する必要がある。
                    // 今回は要件に従い、単純な連続性チェックを適用する（エラー時はExceptionをスローしてロールバック）
                    
                    // ただし、自分自身の更新の場合はチェックを緩和するロジックを入れる
                    boolean isSelfUpdate = false;
                    if (dto.getId() != null) {
                         ProjectPlan existing = projectPlanMapper.findById(dto.getId()).orElse(null);
                         if (existing != null && existing.getEndYm().equals(maxEndYm)) {
                             // 自分が最新のレコードであれば、開始年月の変更は許容される可能性があるが、
                             // ここでは「期間の重複・逆転」を防ぐため、
                             // 「開始年月 > (自分以外の最大終了年月)」のチェックが本来必要。
                             // 簡易実装として、Start <= MaxEnd の場合は警告またはエラー。
                             // ここでは厳密にエラーとする。
                         }
                    }
                    
                    // 既存データがある場合のみチェック（初回登録時はmaxEndYmはnullまたは関係ない）
                     throw new BusinessException("インポートエラー: プロジェクトID " + dto.getProjectId() + 
                             " - 開始年月 (" + dto.getStartYm() + ") は前回の終了年月 (" + maxEndYm + ") より後の日付である必要があります。");
                }
            }

            if (dto.getId() != null) {
                entity.setId(dto.getId());
                projectPlanMapper.update(entity);
            } else {
                 projectPlanMapper.insert(entity);
            }
        }
    }

    public void exportPlanCsv(Writer writer) throws Exception {
        List<ProjectPlan> list = projectPlanMapper.findAll();
        List<ProjectPlanCsvDto> dtos = list.stream().map(e -> {
            ProjectPlanCsvDto dto = new ProjectPlanCsvDto();
            dto.setId(e.getId());
            dto.setProjectId(e.getProjectId());
            dto.setPlanVersion(e.getPlanVersion());
            dto.setStartYm(e.getStartYm());
            dto.setEndYm(e.getEndYm());
            dto.setResourceCount(e.getResourceCount());
            dto.setProductionAmount(e.getProductionAmount());
            return dto;
        }).collect(Collectors.toList());

        new StatefulBeanToCsvBuilder<ProjectPlanCsvDto>(writer)
                .build()
                .write(dtos);
    }

    public List<ProjectStatusHistory> findStatusHistory(Long projectId) {
        return projectStatusHistoryMapper.findByProjectId(projectId);
    }

    private void validateStatusTransition(String oldStatus, String newStatus) {
        if (oldStatus == null || Objects.equals(oldStatus, newStatus)) {
            return;
        }
        if (oldStatus.equals("提案中")) {
            if (newStatus.equals("進行中") || newStatus.equals("中断")) {
                return;
            }
        } else if (oldStatus.equals("進行中")) {
            if (newStatus.equals("完了") || newStatus.equals("中断")) {
                return;
            }
        } else if (oldStatus.equals("中断")) {
            if (newStatus.equals("進行中")) {
                return;
            }
        }
        throw new BusinessException("このステータスへの変更は許可されていません。");
    }

    private void validateRequiredFieldsForStatus(ProjectMaster project) {
        String status = project.getStatus();
        if (status == null || status.isBlank()) {
            return;
        }
        if (status.equals("提案中")) {
            if (isBlank(project.getName()) || isBlank(project.getCustomerId())) {
                throw new BusinessException("ステータスが「提案中」の場合、プロジェクト名と顧客は必須です。");
            }
        } else if (status.equals("進行中")) {
            if (isBlank(project.getName()) || isBlank(project.getCustomerId()) || project.getStartYm() == null || project.getRevenue() == null) {
                throw new BusinessException("ステータスが「進行中」の場合、プロジェクト名、顧客、開始年月、売上は必須です。終了年月は任意項目です。");
            }
        } else if (status.equals("完了")) {
            if (isBlank(project.getName()) || isBlank(project.getCustomerId()) || project.getStartYm() == null || project.getEndYm() == null) {
                throw new BusinessException("ステータスが「完了」の場合、プロジェクト名、顧客、開始年月、終了年月は必須です。");
            }
        } else if (status.equals("中断")) {
            if (isBlank(project.getName()) || isBlank(project.getCustomerId())) {
                throw new BusinessException("ステータスが「中断」の場合、プロジェクト名と顧客は必須です。");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
