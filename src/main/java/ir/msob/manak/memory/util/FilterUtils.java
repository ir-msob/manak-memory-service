package ir.msob.manak.memory.util;

import ir.msob.jima.core.commons.filter.Param;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FilterUtils {

    private FilterUtils() {
    }

    /**
     * Build filter expression from map of key -> Param
     */
    public static Filter.Expression buildFilterFromParams(Map<String, Param<?>> params) {

        if (params == null || params.isEmpty()) {
            return null;
        }

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<FilterExpressionBuilder.Op> ops = new ArrayList<>();

        params.forEach((key, param) -> {
            if (param == null) return;

            // --- supported operators ---
            if (param.getEq() != null) ops.add(b.eq(key, param.getEq()));
            if (param.getNe() != null) ops.add(b.ne(key, param.getNe()));
            if (param.getGt() != null) ops.add(b.gt(key, param.getGt()));
            if (param.getGte() != null) ops.add(b.gte(key, param.getGte()));
            if (param.getLt() != null) ops.add(b.lt(key, param.getLt()));
            if (param.getLte() != null) ops.add(b.lte(key, param.getLte()));

            if (param.getIn() != null && !param.getIn().isEmpty()) {
                ops.add(b.in(key, param.getIn().toArray()));
            }

            if (param.getNin() != null && !param.getNin().isEmpty()) {
                ops.add(b.nin(key, param.getNin().toArray()));
            }

            // regex / exists intentionally ignored
        });

        // No filters → return null
        if (ops.isEmpty()) return null;

        // combine with AND
        FilterExpressionBuilder.Op combined = ops.getFirst();
        for (int i = 1; i < ops.size(); i++) {
            combined = b.and(combined, ops.get(i));
        }

        return b.group(combined).build();
    }
}
