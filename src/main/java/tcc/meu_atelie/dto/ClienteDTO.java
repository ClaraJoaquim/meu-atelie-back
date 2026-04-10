package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClienteDTO {

    private String nome;
    private String cpf;
    private String cnpj;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String observacoes;
    private Long canalId;
    private EnderecoDTO endereco;
}