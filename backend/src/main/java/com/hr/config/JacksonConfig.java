package com.hr.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 全局 Jackson 时间格式配置
 *
 * 知识点（重要，面试常问）：
 * 1. spring.jackson.date-format 只对 java.util.Date 生效，对 LocalDateTime / LocalDate 无效，
 *    所以你会看到接口返回 "2026-09-15T16:24:42.3355002" 这种带 T 和小数秒的 ISO 格式。
 * 2. 正确做法：给 LocalDateTime 等类型单独注册序列化器（本类做法）。
 * 3. 备选方案：在实体字段上打 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")。
 *    全局配置胜在一处生效、不用每个字段都加注解；@JsonFormat 胜在可以单字段定制。
 *    企业项目通常是「全局配置兜底 + 个别字段用注解覆盖」。
 *
 * 注意：项目已引入 spring-boot-starter-web，它自带 jackson-datatype-jsr310，
 * 所以下面的 Serializer/Deserializer 类可以直接用，无需额外加依赖。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 序列化：Java 对象 -> JSON
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
            builder.serializerByType(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));

            // 反序列化：JSON -> Java 对象（前端提交 "2026-09-15 09:00:00" 也能正确解析）
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
            builder.deserializerByType(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));

            // java.util.Date 也用同一套格式
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
        };
    }
}
