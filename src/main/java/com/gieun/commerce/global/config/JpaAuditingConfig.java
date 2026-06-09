package com.gieun.commerce.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // 엔티티의 생성일/수정일/생성자/수정자를 자동으로 채워주는 기능
public class JpaAuditingConfig {

}
