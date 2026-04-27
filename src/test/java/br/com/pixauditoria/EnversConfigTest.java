package br.com.pixauditoria;

import br.com.pixauditoria.config.EnversConfig;
import org.hibernate.envers.configuration.EnversSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste unitário da EnversConfig.
 * Não sobe o contexto Spring — instancia a classe diretamente,
 * tornando o teste mais rápido e sem dependências externas.
 */
class EnversConfigTest {

    private EnversConfig enversConfig;
    private Map<String, Object> hibernateProperties;

    @BeforeEach
    void setUp() {
        enversConfig = new EnversConfig();
        hibernateProperties = new HashMap<>();
        enversConfig.enversPropertiesCustomizer().customize(hibernateProperties);
    }

    @Test
    void deveConfigurarSufixoDeTabelaDeAuditoria() {
        assertEquals("_aud", hibernateProperties.get(EnversSettings.AUDIT_TABLE_SUFFIX),
                "O sufixo da tabela de auditoria deve ser '_aud'");
    }

    @Test
    void deveConfigurarStoreDataAtDelete() {
        assertEquals(true, hibernateProperties.get(EnversSettings.STORE_DATA_AT_DELETE),
                "store_data_at_delete deve ser true para preservar estado antes de DELETE");
    }

    @Test
    void deveConfigurarNomeDosCamposDeRevisao() {
        assertEquals("rev_id", hibernateProperties.get(EnversSettings.REVISION_FIELD_NAME),
                "Nome do campo de revisão deve ser 'rev_id'");
        assertEquals("rev_type", hibernateProperties.get(EnversSettings.REVISION_TYPE_FIELD_NAME),
                "Nome do campo de tipo de revisão deve ser 'rev_type'");
    }

    @Test
    void deveTerTodasAsQuatroPropriedadesConfiguradas() {
        assertEquals(4, hibernateProperties.size(),
                "Exatamente 4 propriedades do Envers devem ser configuradas");
    }
}