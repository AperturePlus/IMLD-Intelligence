package xenosoft.imldintelligence.module.careplan.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Web 饮食管理 DTO 分类目录。
 *
 * <p>该门面只返回医生工作站制定膳食方案所需的最小字段，避免暴露身份证号、手机号等高敏字段。</p>
 */
public final class DietApiDtos {
    private DietApiDtos() {
    }

    public static final class Response {
        private Response() {
        }

        public record DietPatientsResponse(
                List<DietPatientItem> items
        ) {
        }

        public record DietPatientItem(
                String id,
                String name,
                String gender,
                int age,
                String avatar,
                String disease,
                String compliance
        ) {
        }

        public record DietPlanResponse(
                List<DietTargetItem> targets,
                DietFoodsItem foods,
                List<MealPlanItem> mealPlan
        ) {
        }

        public record DietTargetItem(
                String label,
                String value,
                String unit,
                String color,
                String desc
        ) {
        }

        public record DietFoodsItem(
                List<String> red,
                List<String> yellow,
                List<String> green
        ) {
        }

        public record MealPlanItem(
                String type,
                String time,
                String menu,
                String nutrition
        ) {
        }

        public record RegenerateDietPlanResponse(
                List<MealPlanItem> mealPlan,
                OffsetDateTime regeneratedAt
        ) {
        }

        public record PushDietPlanResponse(
                boolean delivered,
                String patientId,
                OffsetDateTime deliveredAt
        ) {
        }
    }
}
