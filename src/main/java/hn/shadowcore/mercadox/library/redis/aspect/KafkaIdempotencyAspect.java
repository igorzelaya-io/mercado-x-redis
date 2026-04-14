package hn.shadowcore.mercadox.library.redis.aspect;

import hn.shadowcore.mercadox.library.entity.response.EventDto;
import hn.shadowcore.mercadox.library.redis.util.RedisIdempotencyChecker;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class KafkaIdempotencyAspect {

    private final RedisIdempotencyChecker idempotencyChecker;

    @Around("@annotation(hn.shadowcore.mercadox.context.utils.annotations.KafkaIdempotent)")
    public Object checkIdempotency(ProceedingJoinPoint pjp) throws Throwable {
        EventDto<?> dto = null;

        Object[] args = pjp.getArgs();

        for (Object arg : args) {
            if (arg instanceof ConsumerRecord<?, ?> consumerRecord) {
                Object value = consumerRecord.value();
                if (value instanceof EventDto<?> typedDto) {
                    dto = typedDto;
                    break;
                }

            } else if (arg instanceof EventDto<?> directDto) {
                dto = directDto;
                break;
            }
        }
        if (!Objects.nonNull(dto) ||
                !Objects.nonNull(dto.getEventId())
                || idempotencyChecker.isDuplicate(dto.getEventId())) {
            return null;
        }
        return pjp.proceed();
    }

}

