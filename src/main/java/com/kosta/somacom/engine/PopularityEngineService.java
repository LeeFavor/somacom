package com.kosta.somacom.engine;

import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.score.PopularityScoreId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopularityEngineService {

    /**
     * 주문 아이템 목록을 분석하여 함께 구매된 상품 조합의 빈도수를 계산합니다.
     * @param allOrderItems 모든 주문 아이템
     * @return (A, B) 조합을 Key로, 빈도수를 Value로 갖는 Map
     */
    public Map<PopularityScoreId, Long> calculateScores(List<OrderItem> allOrderItems) {
        // 1. orderId로 주문 아이템들을 그룹화
        Map<Long, List<String>> itemsByOrder = allOrderItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getOrder().getId(),
                        Collectors.mapping(item -> item.getProduct().getBaseSpec().getId(), Collectors.toList())
                ));

        Map<PopularityScoreId, Long> frequencyMap = new HashMap<>();

        // 2. 각 주문별로 상품 조합 생성 및 빈도수 계산
        for (List<String> specIdsInOrder : itemsByOrder.values()) {
            // 한 주문에 2개 이상의 부품이 있어야 조합이 의미 있음
            if (specIdsInOrder.size() < 2) continue;

            List<String> distinctSpecIds = new ArrayList<>(specIdsInOrder.stream().distinct().collect(Collectors.toList()));

            for (int i = 0; i < distinctSpecIds.size(); i++) {
                for (int j = i + 1; j < distinctSpecIds.size(); j++) {
                    List<String> pair = new ArrayList<>(List.of(distinctSpecIds.get(i), distinctSpecIds.get(j)));
                    Collections.sort(pair); // 항상 ID가 작은 것이 A가 되도록 정렬 (A,B 와 B,A 중복 방지)

                    PopularityScoreId scoreId = new PopularityScoreId(pair.get(0), pair.get(1));
                    frequencyMap.put(scoreId, frequencyMap.getOrDefault(scoreId, 0L) + 1);
                }
            }
        }
        return frequencyMap;
    }
}