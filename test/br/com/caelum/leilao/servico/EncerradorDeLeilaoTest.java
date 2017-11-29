package br.com.caelum.leilao.servico;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Usuario;

public class EncerradorDeLeilaoTest {
	
	private Usuario renato;
	private Usuario jose;
	
	@Before
	public void setUp(){
		renato = new Usuario("Renato");
		jose = new Usuario("Jose");
	}

	@Test
	public void garanteQueDeveEncerrarLeilaoComMaisDeUmaSemana() {
		
		Leilao leilao = new CriadorDeLeilao().para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 20))
				.lance(jose, 100)
				.lance(renato, 200)
				.constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(1));
		
		assertThat(leilao.isEncerrado(), equalTo(true));
		
		verify(daoFalso).atualiza(leilao);
		
		
		
	}
	
	@Test
	public void garanteQueNaoDeveEncerrarComMenosDeUmaSemana(){
		
		Leilao leilao = new CriadorDeLeilao().para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 27))
				.lance(jose, 100)
				.lance(renato, 200)
				.constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(0));
		
		assertThat(leilao.isEncerrado(), equalTo(false));
		
		verify(daoFalso, never()).atualiza(leilao);
		
	}
	
	@Test
	public void garanteQueDeveEncerrarComMaisDeUmaSemanaENaoEncerrarComMenosDeUmaSemana(){
		
		Leilao leilao = new CriadorDeLeilao().para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 27))
				.lance(jose, 100)
				.lance(renato, 200)
				.constroi();
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 19))
				.lance(jose, 200)
				.lance(renato, 500)
				.constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao, leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, enviadorDeEmail);
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(1));
		
		assertThat(leilao.isEncerrado(), equalTo(false));
		assertThat(leilao1.isEncerrado(), equalTo(true));
		
		verify(daoFalso, never()).atualiza(leilao);
		verify(daoFalso, times(1)).atualiza(leilao1);
		
	}
	
	@Test
	public void garanteQueNaFalhaDeEmailContinuaEnviandoEmail(){
		
		Leilao leilao = new CriadorDeLeilao().para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 1))
				.lance(jose, 100)
				.lance(renato, 200)
				.constroi();
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV")
				.naData(new GregorianCalendar(2017, Calendar.NOVEMBER, 2))
				.lance(jose, 200)
				.lance(renato, 500)
				.constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail enviadorDeEmail = mock(EnviadorDeEmail.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao, leilao1));
		
		doThrow(new RuntimeException()).when(enviadorDeEmail).envia(leilao1);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, enviadorDeEmail);
		encerrador.encerra();
		
		assertThat(encerrador.getTotalEncerrados(), equalTo(2));
		
		assertThat(leilao.isEncerrado(), equalTo(true));
		assertThat(leilao1.isEncerrado(), equalTo(true));
		
		verify(daoFalso, times(1)).atualiza(leilao);
		verify(daoFalso, times(1)).atualiza(leilao1);
		
		verify(enviadorDeEmail, times(1)).notifica(leilao1);
		verify(enviadorDeEmail, never()).notifica(leilao);
		
	}
	
	

}
