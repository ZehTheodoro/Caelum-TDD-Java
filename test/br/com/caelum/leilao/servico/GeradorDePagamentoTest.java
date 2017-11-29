package br.com.caelum.leilao.servico;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;


@RunWith(MockitoJUnitRunner.class)
public class GeradorDePagamentoTest {

	private Usuario jose;
	private Usuario renato;
	
	private Leilao leilao;
	
	private Calendar segunda;
	private Calendar sabado;
	private Calendar domingo;
	
	
	@Mock
	RepositorioDeLeiloes leiloes;
	@Mock
	RepositorioDePagamentos pagamentos;
	@Mock
	Avaliador avaliador;
	
	@Mock
	Relogio relogio;
	
	@Captor
	ArgumentCaptor<Pagamento> argumento;
	
	
	
	
	@Before
	public void setUp() {
		this.jose = new Usuario("Jose");
		this.renato = new Usuario("Renato");
		
		leilao = new CriadorDeLeilao().para("TV")
				.lance(jose, 150)
				.lance(renato, 300)
				.constroi();
		
		segunda = new GregorianCalendar(2017, Calendar.NOVEMBER, 27);
		sabado = new GregorianCalendar(2017, Calendar.NOVEMBER, 25);
		domingo = new GregorianCalendar(2017, Calendar.NOVEMBER, 26);
		
		
	}

	@Test
	public void deveGerarPagmentoParaUmLeilaoEncerrado(){
					
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		when(avaliador.getMaiorLance()).thenReturn(300.0);
		when(relogio.hoje()).thenReturn(segunda);
		
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador, relogio);
		
		gerador.gera();
				
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertThat(pagamentoGerado.getValor(), equalTo(300.0));
			
		
	}
	
	@Test
	public void deveEmpurrarParaOPriximoDiaUtil(){
			
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		when(relogio.hoje()).thenReturn(domingo);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador, relogio);
		gerador.gera();
		
		verify(pagamentos).salva(argumento.capture());
		
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertThat(pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK), equalTo(Calendar.MONDAY));
		
	}

}
