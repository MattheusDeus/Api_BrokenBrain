package com.brokenbrain.reabnext;

import com.brokenbrain.reabnext.gpt.model.GPT;
import com.brokenbrain.reabnext.gpt.service.GptService;
import com.brokenbrain.reabnext.paciente.model.Paciente;
import com.brokenbrain.reabnext.treino.model.Treino;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;


public class ReabNEXT {

    public static void main(String[] args) {

        // - Instanciando EntityManager e setando banco de dados
        EntityManagerFactory factory = Persistence.createEntityManagerFactory( "oracle-home" );
        EntityManager manager = factory.createEntityManager();

        // - Instanciando Paciente(usuario) com Nome, Data de nascimento, Peso, Altura e Descrição da deficiência
        var usuario = getPaciente();

        // - Instanciando Treino com quantidade de dias de treino, data de início e data de término(com base na qtd de dias de treino)
        // - e descrição da deficiência. Esses dados são usados no input da API da OpenAI
        var treino = getTreino( usuario );

        // - Instanciando GPT e setando o treino.
        var gpt = new GPT( treino );

        // - getInputGptPrompt é o método que gera a String que será colocada como input na API da OpenAI para Gerar o treino
        String prompt = gpt.getInputGptPrompt();

        // - Instanciando service, passando o prompt de input e recebendo output através do método gerarTreino
        var teinoGerado = getTeinoGerado( prompt );

        //Salva o treino Gerado no Banco de Dados se existir proposta de treino do chat GPT
        if(Objects.nonNull( teinoGerado ) && ! teinoGerado.isEmpty()){

            persistir( manager, teinoGerado, gpt, factory );
        }

        manager.close();
        factory.close();
    }

    private static void persistir(EntityManager manager, Map<String, Object> teinoGerado, GPT gpt, EntityManagerFactory factory) {
        var orientacaoDeTreino = new ArrayList<String>();
        if (Objects.nonNull( teinoGerado )) {
            teinoGerado.forEach( (k, v) -> {
                System.out.println( "Chave: " + k + "Valor: " + v );
                orientacaoDeTreino.add( (String) v );
            } );
            manager.getTransaction().begin();
            gpt.setOutputGpt( orientacaoDeTreino.toString() );
            manager.persist( gpt );
            manager.getTransaction().commit();
        }

    }

    private static Map<String, Object> getTeinoGerado(String prompt) {
        GptService service = new GptService();
        service.setPROMPT( prompt );
        var treinoGerado = service.gerarTreino();
        return treinoGerado;
    }

    private static Treino getTreino(Paciente usuario) {
        var treino = new Treino();
        treino.setPaciente( usuario )
                .setQtdDias( 5 )
                .setDtInicio( LocalDateTime.now().plusDays( 1 ) )
                .setDtFim( LocalDateTime.now().plusDays( 6 ) )
                .setDescDeficiencia( usuario.getDescDeficiencia() );
        return treino;
    }

    private static Paciente getPaciente() {
        var usuario = new Paciente();
        usuario.setNome( "Alvaro" );
        usuario.setDtNasc( LocalDate.now().minusYears( 38 ) );
        usuario.setPeso( 58 );
        usuario.setAltura( 1.77f );
        usuario.setDescDeficiencia( "Braco esquerdo amputado na altura do ombro" );
        return usuario;
    }

}
