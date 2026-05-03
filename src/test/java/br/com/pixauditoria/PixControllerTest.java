package br.com.pixauditoria;

import br.com.pixauditoria.config.SecurityConfig;
import br.com.pixauditoria.controller.PixController;
import br.com.pixauditoria.service.PixService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PixController.class)
@Import(SecurityConfig.class) // carrega o csrf().disable() e anyRequest().permitAll()
class PixControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PixService pixService;

    @Test
    void deveRetornar400SeDadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/pix/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chaveDestino\":\"x\",\"valor\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/pix/transferencias/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void deveBuscarHistorico() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/pix/transferencias/" + id + "/historico"))
                .andExpect(status().isOk());
    }

    @Test
    void deveCancelar() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/pix/transferencias/" + id + "/cancelar"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListar() throws Exception {
        mockMvc.perform(get("/api/pix/transferencias").param("chave", "abc"))
                .andExpect(status().isOk());
    }
}