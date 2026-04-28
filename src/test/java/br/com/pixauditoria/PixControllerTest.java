package br.com.pixauditoria;

import br.com.pixauditoria.config.SecurityConfig;
import br.com.pixauditoria.controller.PixController;
import br.com.pixauditoria.service.PixService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
}