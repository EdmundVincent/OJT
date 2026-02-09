package com.example.staffassign.service;

import com.example.staffassign.repository.ReportMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final ReportMapper reportMapper;

    public ReportService(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    /**
     * 要員稼働率レポート取得
     * 指定された期間・稼働率範囲に基づいて要員の稼働状況を集計する
     */
    public List<Map<String, Object>> getResourceAllocation(Map<String, Object> params) {
        return reportMapper.getResourceAllocation(params);
    }

    /**
     * 予算不一致プロジェクトレポート取得
     * プロジェクト計画の生産額合計とプロジェクトマスタの売上を比較し、不一致のものを抽出する
     */
    public List<Map<String, Object>> getBudgetMismatch(Map<String, Object> params) {
        return reportMapper.getBudgetMismatch(params);
    }

    /**
     * プロジェクトコスト分析レポート取得
     * アサイン情報（単金 * 割当比）から実績コストを算出し、利益（売上 - コスト）を計算する
     */
    public List<Map<String, Object>> getProjectCost(Map<String, Object> params) {
        return reportMapper.getProjectCost(params);
    }

    public void exportCsv(List<Map<String, Object>> data, java.io.Writer writer) throws Exception {
        if (data.isEmpty()) {
            return;
        }

        // Get headers from first row
        java.util.Set<String> headers = data.get(0).keySet();
        com.opencsv.CSVWriter csvWriter = new com.opencsv.CSVWriter(writer);

        // Write header
        csvWriter.writeNext(headers.toArray(new String[0]));

        // Write data
        for (Map<String, Object> row : data) {
            String[] values = new String[headers.size()];
            int i = 0;
            for (String header : headers) {
                Object value = row.get(header);
                values[i++] = value != null ? value.toString() : "";
            }
            csvWriter.writeNext(values);
        }
    }
}
