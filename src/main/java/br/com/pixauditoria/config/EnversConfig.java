package br.com.pixauditoria.config;

import org.hibernate.envers.configuration.EnversSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import java.util.Map;
import org.springframework.context.annotation.Configuration;

/*
 * Configuração explícita do Hibernate Envers.
 * Move as propriedades do Envers que estavam em application.properties
 * para código Java, tornando-as visíveis, testáveis e centralizadas.
 */

@Configuration
public class EnversConfig {

    @Bean
    public HibernatePropertiesCustomizer enversPropertiesCustomizer() {
        return (Map<String, Object> hibernateProperties) -> {
            hibernateProperties.put(EnversSettings.AUDIT_TABLE_SUFFIX, "_aud");
            hibernateProperties.put(EnversSettings.STORE_DATA_AT_DELETE, true);

            // Nome do campo de número de revisão na tabela _aud
            //hibernateProperties.put(EnversSettings.REVISION_FIELD_NAME, "rev_id");

            // Nome do campo de tipo de operação (0=INSERT, 1=UPDATE, 2=DELETE)
            //hibernateProperties.put(EnversSettings.REVISION_TYPE_FIELD_NAME, "rev_type");
        };
    }
}